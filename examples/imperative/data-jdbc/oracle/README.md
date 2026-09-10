# Helidon Data JDBC Imperative with Oracle Database

This example shows how to execute Oracle Database statements directly with the Helidon Data `JdbcClient`. Application
code creates each statement, binds its positional parameters, selects a mapper, and invokes the terminal operation.

The configuration defines an Oracle Universal Connection Pool (UCP) data source named `example`. The HTTP service uses
a standalone client configured with that data source name. The application does not publish the client in the Service
Registry.

Use this example when you want JDBC operations to remain explicit in application code. To generate implementations from
annotated repository interfaces, see the
[declarative Oracle Database example](../../../declarative/data-jdbc/oracle).

## What the Example Demonstrates

The application covers:

- list, optional, scalar, insert, update, and delete JDBC operations;
- positional parameter binding, including binding one value to multiple positions;
- mapping joined rows to a `Pokemon` containing a nested `Type`;
- selecting either the standard or explicit row mapper;
- retrieving a database-generated identifier; and
- looking up a type before inserting or updating a Pokemon through separate JDBC operations.

## Client Construction and Transaction Behavior

The HTTP service constructs its client using the configured data source name:

```java
JdbcClient jdbcClient = JdbcClient.create(builder -> builder.dataSourceName("example"));
```

The client remains standalone, and its JDBC operations do not participate in `Tx.transaction`. The configured UCP data
source remains externally managed. As a result, the type lookup and mutation in each insert or update flow run as
separate JDBC operations.

## Prerequisites

To build and run the example, you need:

- A JDK
- Maven
- A running Oracle Database

You can use an existing database or start the optional Docker container described below. Docker is also required to run
the database-backed tests.

## Database Configuration

The data source settings are in `src/main/resources/application.yaml`. By default, the application connects to the
`FREEPDB1` pluggable database at `localhost:1521` with username `pokemon` and password `changeit`. If you use another
database, update `data.url` and the UCP credentials before starting the application.

The application does not create or migrate the schema, so initialize the database before starting the application.

The default credentials are intended only for this local demo. You can use an existing Oracle Database or start the
optional local container described below. With an existing database, provision the configured user and run
`etc/schema.sql` as that user.

## Optional Local Oracle Container

If you want to use a local Oracle Database container for the demo, run the following command from the
`examples/imperative/data-jdbc/oracle` directory:

```shell
docker run --name oracle \
       -p 1521:1521 \
       -e ORACLE_PWD='oracle123' \
       -v "$PWD/etc/setup-user.sql:/opt/oracle/scripts/startup/01-setup-user.sql:ro" \
       -v "$PWD/etc:/opt/helidon:ro" \
       -d container-registry.oracle.com/database/free:23.26.3.0-lite
```

The first volume makes the demo user provisioning script available in the container's startup directory. The second
volume makes `etc/schema.sql` and its SQL*Plus wrapper available inside the container.

Follow the startup log and wait for the database to complete startup and setup-user.sql run successfully:

```shell
docker logs -f oracle
```

## Demo User Provisioning

As part of the optional container setup, `etc/setup-user.sql` creates the `pokemon` user in `FREEPDB1` if it does not
already exist. Oracle Database uses an administrator connection only while running this provisioning script. The demo
application and its sample schema connect as `pokemon`, not as `SYS` or `SYSTEM`.

The script grants the permissions required by the demo and gives `pokemon` a limited quota on a dedicated
`POKEMON_DATA` tablespace.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemon` schema, including
> all their data, before recreating and populating them. It does not modify tables in other schemas.

When using the optional container, run the schema script as the demo user:

```shell
docker exec oracle \
       sqlplus -s pokemon/changeit@//localhost:1521/FREEPDB1 \
       @/opt/helidon/run-schema.sql
```

The mounted `run-schema.sql` wrapper stops on the first SQL error and executes `etc/schema.sql`. The schema script
recreates the sample tables and their foreign key, inserts the Pokemon types and Pokemon, and commits the sample data.

Use your normal provisioning and credential-management practices for any environment beyond this local demo.

## Build and Run

From `examples/imperative/data-jdbc/oracle`, build the application and run its tests:

```shell
mvn package
```

To build the application without running tests:

```shell
mvn package -DskipTests
```

This example uses Helidon APIs marked as preview. Maven passes `-Ahelidon.api.preview=ignore` to the compiler, so the
Java sources do not need preview-warning suppression annotations.

Start the packaged application:

```shell
java '-Dhelidon.serialFilter.pattern=oracle.sql.converter.*' \
     -jar target/helidon-examples-imperative-data-jdbc-oracle.jar
```

Oracle JDBC reads bundled character-set conversion data using Java serialization. The system property permits the
Oracle converter package while Helidon's serialization filter continues to reject other classes by default.

The test suite uses Testcontainers to start
`container-registry.oracle.com/database/free:23.26.3.0-lite`, the same image shown above. It provisions the `pokemon`
user, initializes the database with the same `etc/schema.sql`, and exercises the documented query and mutation
endpoints through its Oracle UCP data source. Testcontainers manages this database, so the optional local container is
not needed for tests. When Docker is unavailable, JUnit skips the container-backed test class.

The application listens on `http://localhost:8080/pokemon`.

After application startup, UCP initializes the connection pool when the first database connection is requested. The
first invocation of an endpoint that accesses the database may therefore take longer while UCP creates the initial
pooled connections. Subsequent invocations reuse pooled connections.

## Try the Application

List all Pokemon:

```shell
curl http://localhost:8080/pokemon/all
```

List Pokemon having the `Normal` type:

```shell
curl http://localhost:8080/pokemon/type/Normal
```

Search for a Pokemon whose name or type is `Normal`:

```shell
curl http://localhost:8080/pokemon/search/Normal
```

The same `term` value is bound to both positional parameters.

Retrieve `Meowth` by name:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Retrieve `Meowth` with the explicitly selected row mapper:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The explicit mapper returns `"EXPLICIT: Meowth"`, which makes the selected mapper visible in the response.

Retrieve `Meowth` by type and name:

```shell
curl http://localhost:8080/pokemon/search/Normal/Meowth
```

Count all Pokemon:

```shell
curl http://localhost:8080/pokemon/count
```

Insert a Pokemon. The response is a JSON object containing the generated identifier, name, and type:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

The type lookup and insert are separate JDBC operations. The schema starts generated identifiers at `20`, so the JSON
object returned by the first insert into a freshly initialized database contains that ID.

Update the inserted Pokemon's name and type. Use the identifier returned by `POST`; the first identifier is `20` in a
freshly initialized database:

```shell
curl -i -X PUT \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmeleon","type":"Fire"}' \
     http://localhost:8080/pokemon/20
```

`PUT /pokemon/{id}` updates the row selected by the path identifier and returns its new JSON representation. The body
supplies the new nonblank `name` and an existing Pokemon `type`; it does not need an `id`. A missing row returns HTTP
`404`. The standalone client performs the type lookup and update as separate JDBC operations.

Delete the updated Pokemon:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

## Stop Oracle Database

To stop the Oracle Database container:

```shell
docker stop oracle
```

To delete the stopped container:

```shell
docker rm oracle
```

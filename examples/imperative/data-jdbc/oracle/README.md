# Helidon Data JDBC Imperative using Oracle Database

This example demonstrates imperative use of the Helidon Data JDBC provider with Oracle Database. It is the
imperative counterpart of `examples/declarative/data-jdbc` and uses the same Pokemon schema, SQL statements, database
method names, row mappers, HTTP paths, and JSON representation.

The configuration defines an Oracle Universal Connection Pool (UCP) data source named `example`. The HTTP service uses
a standalone client configured with that data-source name. The application does not publish the client in the Service
Registry.

The sample demonstrates:

- list, optional, scalar, insert, and delete JDBC operations;
- positional parameter binding, including binding one value to multiple positions;
- mapping joined rows to a `Pokemon` containing a nested `Type`;
- selecting either the normal or alternate row mapper;
- retrieving a database-generated identifier; and
- looking up a type and inserting a Pokemon through separate JDBC operations.

## Client Construction

The HTTP service constructs its client using the configured data-source name:

```java
JdbcClient jdbcClient = JdbcClient.create(builder -> builder.dataSource("example"));
```

The client remains standalone, and its JDBC operations do not participate in `Tx.transaction`. The configured UCP data
source remains externally managed.

Before running the application, you must run `etc/schema.sql` against the database to create and populate the sample
tables. The application does not create its own schema.

The application connects to the `FREEPDB1` pluggable database with username `pokemon` and password `changeit`. These
credentials are part of the example and are not intended for use outside a local demo.

The container and volume instructions below are one convenient way to prepare a database and run the SQL script. They
are provided to make the demo easy to try and are not recommendations for configuring or securing a production
environment. You can instead use an existing Oracle Database and run the scripts with the database tools and account
management process appropriate for that environment.

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

Follow the startup log and wait for the database to become ready:

```shell
docker logs -f oracle
```

## Demo User Provisioning

As part of the optional container setup, `etc/setup-user.sql` creates the `pokemon` user in `FREEPDB1` if it does not
already exist. Oracle Database uses an administrator connection only while running this provisioning script. The demo
application and its sample schema connect as `pokemon`, not as `SYS` or `SYSTEM`.

The script grants the permissions required by the demo and gives `pokemon` a limited quota on a dedicated
`POKEMON_DATA` tablespace.

The data-source settings are in `src/main/resources/application.yaml`. If Oracle Database runs on a different host or
port, update `data.url`. Update the username and password there if you use different credentials.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemon` schema, including
> all their data, before recreating and populating them. It does not modify tables in other schemas.

Running `etc/schema.sql` is a prerequisite for the demo. When using the optional container setup above, this single
command runs the script as the demo user:

```shell
docker exec oracle \
       sqlplus -s pokemon/changeit@//localhost:1521/FREEPDB1 \
       @/opt/helidon/run-schema.sql
```

The mounted `run-schema.sql` wrapper stops on the first SQL error and executes `etc/schema.sql`. The schema script drops
the sample tables, recreates them and their foreign key, inserts the Pokemon types and sample Pokemon, and commits the
sample data. If you use a different Oracle Database setup, run `etc/schema.sql` there as the user the application will
use before starting the application.

The container, volume mounts, demo credentials, and SQL*Plus commands in this section are conveniences for running the
example. Production database provisioning, credential management, storage, and security policies are outside the scope
of this README.

## Build and Run

From `examples/imperative/data-jdbc/oracle`, build the application:

```shell
mvn package
```

Start the packaged application:

```shell
java '-Dhelidon.serialFilter.pattern=oracle.sql.converter.*' \
     -jar target/helidon-examples-imperative-data-jdbc-oracle.jar
```

Oracle JDBC reads bundled character-set conversion data using Java serialization. The system property permits the
Oracle converter package while Helidon's serialization filter continues to reject other classes by default.

The application listens on `http://localhost:8080/pokemon`.

## Test with H2

Run the tests without starting Oracle Database:

```shell
mvn test
```

The test configuration runs UCP with H2 in Oracle compatibility mode and uses the same `etc/schema.sql` used by Oracle
Database. The test does not require a running Oracle Database container.

## Invoke the Endpoints

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

Retrieve `Meowth` with the alternate row mapper:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The alternate mapper returns the recognizable name `"LOW-WEIGHT EXPLICIT: Meowth"`.

Retrieve `Meowth` by type and name:

```shell
curl http://localhost:8080/pokemon/search/Normal/Meowth
```

Count all Pokemon:

```shell
curl http://localhost:8080/pokemon/count
```

Insert a Pokemon and return a JSON object containing its generated identifier, name, and type:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

The type lookup and insert are separate JDBC operations. The schema starts generated identifiers at `20`, so the JSON
object returned by the first insert into a freshly initialized database contains that ID.

Delete the inserted Pokemon:

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

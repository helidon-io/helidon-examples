# Helidon Data JDBC Imperative with MySQL

This example shows how to execute MySQL statements directly with the Helidon Data `JdbcClient`. Application code creates
each statement, binds its positional parameters, selects a mapper, and invokes the terminal operation.

`Main` constructs a standalone `JdbcClient` from the MySQL connection properties in `application.yaml` and passes it to
`PokemonService`, which owns the imperative HTTP handlers and JDBC operations. MySQL Connector/J supplies the JDBC
driver. The application does not publish this client in the Service Registry.

Use this example when you want JDBC operations to remain explicit in application code. To generate implementations from
annotated repository interfaces, see the [declarative MySQL example](../../../declarative/data-jdbc/mysql).

## What the Example Demonstrates

The application covers:

- list, optional, scalar, insert, update, and delete JDBC operations
- positional parameter binding, including binding one value to multiple positions
- mapping joined rows to a `Pokemon` containing a nested `Type`
- selecting either the standard or explicit row mapper
- retrieving a MySQL-generated identifier
- looking up a type before inserting or updating a Pokemon through separate JDBC operations

## Client Construction and Transaction Behavior

The example uses the public builder with direct connection properties:

```java
JdbcClient jdbcClient = JdbcClient.builder()
        .connection(connection -> connection
                .url(url)
                .username(username)
                .password(password)
                .jdbcDriverClassName("com.mysql.cj.jdbc.Driver"))
        .build();
```

The directly constructed client is standalone. Each terminal operation owns its connection and does not participate in
`Tx.transaction`. As a result, the type lookup and mutation in each insert or update flow run as separate JDBC
operations.

## Database Configuration

The connection settings are under `app.database` in `src/main/resources/application.yaml`. By default, the application
connects to the `pokemons` database at `localhost:3306` with username `user` and password `changeit`. If you use another
database, update the URL and credentials before starting the application.

The application does not create or migrate the schema, so initialize the database before starting the application.

The default credentials are intended only for this local demo. You can use an existing MySQL database or start the
optional local container described below. With an existing database, create the configured database and user, then run
`etc/schema.sql` as that user.

## Optional Local MySQL Container

If you want to use a local MySQL container for the demo, run the following command from the
`examples/imperative/data-jdbc/mysql` directory:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.1
```

Follow the startup log and wait for MySQL to accept connections:

```shell
docker logs -f mysql
```

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database,
> including all their data, before recreating and populating them. It does not modify tables in other databases.

When using the optional container, run the schema script as the demo user:

```shell
docker exec -i mysql \
       sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --user=user pokemons' \
       < etc/schema.sql
```

The command reads the password from the container's `MYSQL_PASSWORD` environment variable and sends `etc/schema.sql` to
the MySQL client. The script recreates the sample tables and their foreign key, inserts the Pokemon types and Pokemon,
and commits the sample data.

Use your normal provisioning and credential-management practices for any environment beyond this local demo.

## Build and Run

Use JDK 26 and Maven 3.8.0 or newer.

From `examples/imperative/data-jdbc/mysql`, build the application and run its tests:

```shell
mvn package
```

To build the application without running tests:

```shell
mvn package -DskipTests
```

> **Note**
> Helidon Data JDBC is incubating. This example opts in with
> `-Ahelidon.api.incubating=ignore` in [`pom.xml`](pom.xml). An application can instead scope the opt in to its source
> with `@SuppressWarnings(Api.SUPPRESS_INCUBATING)`.

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-mysql.jar
```

The test suite uses Testcontainers to start
`container-registry.oracle.com/mysql/community-server:9.7.1`, the same image shown above. It creates the `pokemons`
database, initializes it with the same `etc/schema.sql`, and exercises the documented query and mutation endpoints
through its MySQL connection. Testcontainers manages this database, so the optional local container is not needed for
tests. When Docker is unavailable, JUnit skips the container-backed test class.

The application listens on `http://localhost:8080/pokemon`.

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

## Stop MySQL

To stop the MySQL container:

```shell
docker stop mysql
```

To delete the stopped container:

```shell
docker rm mysql
```

# Helidon Data JDBC Imperative with PostgreSQL

This example shows how to execute PostgreSQL statements directly with the Helidon Data `JdbcClient`. Application code
creates each statement, binds its positional parameters, selects a mapper, and invokes the terminal operation.

The configuration defines a HikariCP data source named `example` and a registry-managed JDBC client named `pokemon`.
The Service Registry injects that client into `PokemonService`. The client uses the configured data source and
PostgreSQL JDBC driver.

Use this example when you want JDBC operations to remain explicit in application code while using a registry-managed
client for local transactions. To generate implementations from annotated repository interfaces, see the
[declarative PostgreSQL example](../../../declarative/data-jdbc/postgres).

## What the Example Demonstrates

The application covers:

- list, optional, scalar, insert, update, and delete JDBC operations;
- positional parameter binding, including binding one value to multiple positions;
- mapping joined rows to a `Pokemon` containing a nested `Type`;
- selecting either the standard or explicit row mapper;
- retrieving a database-generated identifier; and
- looking up a type before inserting or updating a Pokemon in one local JDBC transaction.

## Client Construction and Transaction Behavior

The application client is configured under `data.clients.jdbc`:

```yaml
data:
  clients:
    jdbc:
      - name: "pokemon"
        data-source: "example"
```

`PokemonService` selects both the JDBC provider and the named client:

```java
@Service.Inject
PokemonService(@Data.ProviderType("jdbc")
               @Service.Named("pokemon")
               JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
}
```

The named client participates in `Tx.transaction`. `PokemonService` wraps each type lookup and its corresponding insert
or update in one local transaction.

## Prerequisites

To build and run the example, you need:

- A JDK
- Maven
- A running PostgreSQL database

You can use an existing database or start the optional Docker container described below. Docker is also required to run
the database-backed tests.

## Database Configuration

The data source and client settings are in `src/main/resources/application.yaml`. By default, the application connects
to the `pokemons` database at `localhost:5432` with username `user` and password `pgsql123`. If you use another
database, update `data.url` and the HikariCP credentials before starting the application.

The application does not create or migrate the schema, so initialize the database before starting the application.

The default credentials are intended only for this local demo. You can use an existing PostgreSQL database or start the
optional local container described below. With an existing database, create the configured database and user, then run
`etc/schema.sql` as that user.

## Optional Local PostgreSQL Container

Like the DbClient PostgreSQL example, the local image installs PostgreSQL Server on Oracle Linux 9 and adds a
standalone entrypoint. If you want to use this image for the demo, build it from the
`examples/imperative/data-jdbc/postgres` directory:

```shell
docker build etc/docker -t helidon-postgres
```

Then start the container:

```shell
docker run --name postgres \
       -p 5432:5432 \
       -e POSTGRES_DB='pokemons' \
       -e POSTGRES_USER='user' \
       -e POSTGRES_PASSWORD='pgsql123' \
       -d helidon-postgres
```

Follow the startup log and wait for PostgreSQL to accept connections:

```shell
docker logs -f postgres
```

The JDBC URL disables quoting of `RETURNING` identifiers so PostgreSQL folds the shared generated-key column name `ID`
in the same way as the unquoted schema and application SQL.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database,
> including all their data, before recreating and populating them. It does not modify tables in other databases.

When using the optional container, run the schema script as the demo user:

```shell
docker exec -i postgres \
       psql --username=user --dbname=pokemons \
       < etc/schema.sql
```

The command sends `etc/schema.sql` to the PostgreSQL client in the container. The script recreates the sample tables and
their foreign key, inserts the Pokemon types and Pokemon, and commits the sample data.

Use your normal provisioning and credential-management practices for any environment beyond this local demo.

## Build and Run

From `examples/imperative/data-jdbc/postgres`, build the application and run its tests:

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
java -jar target/helidon-examples-imperative-data-jdbc-postgres.jar
```

The Maven test suite uses Testcontainers to build the same `etc/docker/Dockerfile` shown above, whose base image is
`container-registry.oracle.com/os/oraclelinux:9-slim`, and start the PostgreSQL server installed by that Dockerfile. It
creates the `pokemons` database, initializes it with the same `etc/schema.sql`, and exercises the documented query and
mutation endpoints through its PostgreSQL connection. Testcontainers manages this database, so the optional local
container is not needed for tests. When Docker is unavailable, JUnit skips the container-backed test class.

The registry-managed `pokemon` client handles application operations. The application listens on
`http://localhost:8080/pokemon`.

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

The registry-managed client performs the type lookup and insert in one local JDBC transaction. The schema starts
generated identifiers at `20`, so the JSON object returned by the first insert into a freshly initialized database
contains that ID.

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
`404`. The registry-managed client performs the type lookup and update in one local JDBC transaction.

Delete the updated Pokemon:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

## Stop PostgreSQL

To stop the PostgreSQL container:

```shell
docker stop postgres
```

To delete the stopped container:

```shell
docker rm postgres
```

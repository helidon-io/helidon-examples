# Helidon Data JDBC Declarative with PostgreSQL

This example shows how to use Helidon Data declarative repositories with PostgreSQL. Helidon generates JDBC-backed
implementations of two repository interfaces:

- `PokemonRepository`
- `TypeRepository`

Repository methods declare SQL with `@Jdbc.Statement`. Helidon can infer query or update execution for most result
shapes. Methods with ambiguous primitive `int` or `long` results use `@Jdbc.Execution` to select the operation
explicitly.

Use this example to explore generated repository implementations. If you prefer to construct statements and call
`JdbcClient` directly, see the [imperative PostgreSQL example](../../../imperative/data-jdbc/postgres).

## Repository Shapes

`PokemonRepository` extends `Data.GenericRepository<Pokemon, Integer>`, declaring `Pokemon` as its entity type and
`Integer` as its identifier type. For a JDBC repository, `Data.GenericRepository` supplies metadata without adding CRUD
methods or generating SQL. Every operation remains defined by an explicit `@Jdbc.Statement`.

`TypeRepository` does not declare entity and identifier types at the repository level and extends no Data repository
interface. Together, the repositories demonstrate the two supported declarative JDBC shapes. Use
`Data.GenericRepository` when you need repository-level entity and identifier metadata, or use a standalone repository
interface when you do not. `Data.BasicRepository`, `Data.CrudRepository`, and `Data.PageableRepository` declare
operations that the JDBC repository provider does not support.

## What the Example Demonstrates

The repositories cover:

- generated JDBC repository implementations for list, optional, insert, update, and delete operations;
- named SQL parameter binding, repeated named markers, and positional binding in repository parameter declaration order;
- generated typed-null binding for reference parameters;
- generated mapping of a flat `Type` record;
- metadata-only `Data.GenericRepository<Pokemon, Integer>` inheritance alongside a repository that does not declare
  entity and identifier types at the repository level;
- marker form `@Jdbc.RowMapper` selection by the exact `JdbcClient.RowMapper<Pokemon>` service contract;
- Service Registry selection of the matching mapper with the highest `@Weight`;
- class-valued `@Jdbc.RowMapper(ExplicitPokemonRowMapper.class)` selection independently of service weight;
- generation of an inherited method declared by a parent repository contract;
- mapping one joined database row to a `Pokemon` containing a nested `Type`;
- staged generated-key retrieval for inserts and update-count handling for updates and deletes;
- explicit query selection for a primitive `long` count result; and
- local JDBC transactions that combine each type lookup with its insert or update.

`PokemonRepository` also extends the ordinary `PokemonLookup` interface. `PokemonLookup` declares
`findByName(String name)` and its JDBC annotations, and the generated `PokemonRepository` implementation includes that
inherited method.

## Prerequisites

To build and run the example, you need:

- A JDK
- Maven
- A running PostgreSQL database

You can use an existing database or start the optional Docker container described below. Docker is also required to run
the database-backed tests.

## Database Configuration

The application uses the PostgreSQL JDBC driver. Its settings are in `src/main/resources/application.yaml`. By default,
the application connects to the `pokemons` database at `localhost:5432` with username `user` and password `pgsql123`.
If you use another database, update `data.url` and the connection credentials before starting the application.

The configuration registers the JDBC client as `pokemon` with direct connection settings. Both repository interfaces
select that client with `@Jdbc.Client("pokemon")`.
The application does not create or migrate the schema, so initialize the database before starting the application.

The default credentials are intended only for this local demo. You can use an existing PostgreSQL database or start the
optional local container described below. With an existing database, create the configured database and user, then run
`etc/schema.sql` as that user.

## Optional Local PostgreSQL Container

Like the DbClient PostgreSQL example, the local image installs PostgreSQL Server on Oracle Linux 9 and adds a
standalone entrypoint. If you want to use this image for the demo, build it from the
`examples/declarative/data-jdbc/postgres` directory:

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

The JDBC URL disables quoting of `RETURNING` identifiers so PostgreSQL can fold the shared generated-key column name
`ID` in the same way as the unquoted schema and repository SQL.

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

From this directory, build the application and run its tests:

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
java -jar target/helidon-examples-declarative-data-jdbc-postgres.jar
```

The test suite uses Testcontainers to build the same `etc/docker/Dockerfile` shown above, whose base image is
`container-registry.oracle.com/os/oraclelinux:9-slim`, and start the PostgreSQL server installed by that Dockerfile. It
creates the `pokemons` database, initializes it with the same `etc/schema.sql`, and exercises the documented query and
mutation endpoints through its PostgreSQL connection. Testcontainers manages this database, so the optional local
container is not needed for tests. When Docker is unavailable, JUnit skips the container-backed test class.

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

Search for a Pokemon name or type with one repeated named parameter:

```shell
curl http://localhost:8080/pokemon/search/Normal
```

The repository uses `:term` twice. Generated code binds the same argument to both JDBC positions in marker encounter
order.

Retrieve `Meowth` by name:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Two services match the marker method's exact `JdbcClient.RowMapper<Pokemon>` contract:

- `ExplicitPokemonRowMapper` has weight `Weighted.DEFAULT_WEIGHT - 10`;
- `PokemonRowMapper` has weight `Weighted.DEFAULT_WEIGHT + 10`.

`ExplicitPokemonRowMapper` comes first alphabetically. The marker lookup nevertheless selects `PokemonRowMapper`
because Service Registry evaluates higher weight before service type name. The ordinary result therefore retains the
database name `"Meowth"`.

Select the lower-weight mapper explicitly:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The class-valued mapper annotation bypasses marker lookup and returns `"LOW-WEIGHT EXPLICIT: Meowth"`, which makes the
selected mapper visible in the response.

Retrieve `Meowth` by type and name:

```shell
curl http://localhost:8080/pokemon/search/Normal/Meowth
```

The search endpoint uses two positional `?` markers. The repository binds the `typeName` argument to position `1` and
the `name` argument to position `2`.

Count all Pokemon:

```shell
curl http://localhost:8080/pokemon/count
```

The count method uses `@Jdbc.Execution(QUERY)` because primitive `long` could otherwise mean either a scalar query or an
update count. The list method omits `@Jdbc.Execution` to demonstrate AUTO inference from its `List<Pokemon>` result.

Insert a Pokemon. The response is a JSON object containing the generated identifier, name, and type:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

`@Jdbc.GeneratedKeys("ID")` selects update execution without a separate `@Jdbc.Execution(UPDATE)` annotation. Generated
code adds the `ID` column through the staged generated-key builder before mapping the returned scalar.

The schema starts generated Pokemon identifiers at `20`, so the JSON object returned by the first insert into a fresh
database contains that ID.

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
`404`.

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

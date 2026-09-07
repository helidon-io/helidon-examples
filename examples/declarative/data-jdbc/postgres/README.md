# Helidon Data JDBC Declarative using PostgreSQL

This example uses Helidon Data to generate pure JDBC implementations of two declarative repository interfaces:

- `PokemonRepository`
- `TypeRepository`

Repository methods declare SQL with `@Jdbc.Statement`. Most result shapes let code generation infer query or update
execution. Primitive `int` and `long` results use `@Jdbc.Execution` when the method shape is ambiguous.

The sample validates:

- generated JDBC repository implementations for list, optional, insert, and delete operations;
- named SQL parameter binding, repeated named markers, and positional binding in repository parameter declaration order;
- generated typed-null binding for reference parameters;
- generated mapping of a flat `Type` record;
- marker form `@Jdbc.RowMapper` selection by the exact `JdbcClient.RowMapper<Pokemon>` service contract;
- Service Registry selection of the matching mapper with the highest `@Weight`;
- class-valued `@Jdbc.RowMapper(PokemonAlternateRowMapper.class)` selection independently of service weight;
- generation of an inherited method declared by a parent repository contract;
- mapping one joined database row to a `Pokemon` containing a nested `Type`;
- staged generated-key retrieval for inserts and update-count handling for deletes;
- explicit query selection for a primitive `long` count result; and
- one local JDBC transaction that looks up a type and inserts a Pokemon.

`PokemonRepository` extends the ordinary `PokemonLookup` interface. The parent declares `findByName(String name)` and
its JDBC annotations. The generated `PokemonRepository` implementation includes that inherited method.

The example uses the PostgreSQL JDBC driver. Before running the application, you must run `etc/schema.sql` against the
database to create and populate the sample tables. The application does not create its own schema.

For this demo, the application connects to the `pokemons` database with username `user` and password `pgsql123`. These
credentials are part of the example and are not intended for use outside a local demo.

The container instructions below are one convenient way to prepare a database and run the SQL script. They are provided
to make the demo easy to try; they are not recommendations for configuring or securing a production environment. You
can instead use an existing PostgreSQL database and run the script with the database tools and account management
process appropriate for that environment.

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

Before continuing, follow the startup log to make sure that the database has completed startup:

```shell
docker logs -f postgres
```

The JDBC URL disables quoting of `RETURNING` identifiers so PostgreSQL can fold the shared generated-key column name
`ID` in the same way as the unquoted schema and repository SQL.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database,
> including all their data, before recreating and populating them. It does not modify tables in other databases.

Running `etc/schema.sql` is a prerequisite for the demo. When using the optional container setup above, this single
command runs the local script as the demo user:

```shell
docker exec -i postgres \
       psql --username=user --dbname=pokemons \
       < etc/schema.sql
```

The command sends `etc/schema.sql` to the PostgreSQL client in the container. The script drops the sample tables,
recreates them and their foreign key, inserts the Pokemon types and sample Pokemon, and commits the sample data. If you
use a different PostgreSQL setup, run `etc/schema.sql` there as the user the application will use before starting the
application.

The container, demo credentials, and PostgreSQL commands in this section are conveniences for running the example.
Production database provisioning, credential management, storage, and security policies are outside the scope of this
README.

## Build and Run

Build the application from this directory:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc-postgres.jar
```

The Maven test suite uses H2 in PostgreSQL compatibility mode and runs the same `etc/schema.sql` used by PostgreSQL. The
test does not require a running PostgreSQL container.

The registry-managed JDBC client is named `pokemon` and uses the inline connection from `data.clients.jdbc`. Both
repository interfaces select it with `@Jdbc.Client("pokemon")`.

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

- `PokemonAlternateRowMapper` has weight `Weighted.DEFAULT_WEIGHT - 10`;
- `PokemonRowMapper` has weight `Weighted.DEFAULT_WEIGHT + 10`.

`PokemonAlternateRowMapper` comes first alphabetically. The marker lookup nevertheless selects `PokemonRowMapper`
because Service Registry evaluates higher weight before service type name. The ordinary result therefore retains the
database name `"Meowth"`.

Select the lower-weight mapper explicitly:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The class-valued mapper annotation ignores marker lookup ordering and returns the recognizable name
`"LOW-WEIGHT EXPLICIT: Meowth"`.

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

Insert a Pokemon and return a JSON object containing its generated identifier, name, and type:

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
Delete it with:

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

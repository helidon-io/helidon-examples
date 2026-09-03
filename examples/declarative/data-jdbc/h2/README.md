# Helidon Data JDBC Declarative using H2 Database

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
- generated mapping of a dedicated flat `PokemonSummary` record without `@Jdbc.RowMapper`;
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

The example uses an embedded, in-memory H2 database through HikariCP. No external database installation or container is
required.

## Database Migrations

The example uses embedded Flyway to manage its database schema and sample data. Everything needed by Flyway is packaged
with this sample under `src/main/resources/db/migration`:

- `V1__create_schema.sql` creates the `TYPE` and `POKEMON` tables and their foreign key;
- `V2__load_types.sql` inserts the Pokemon type reference data; and
- `V3__load_pokemon.sql` inserts the sample Pokemon.

Flyway records applied migrations and their checksums in `flyway_schema_history`. Calling the migration service again
validates the three scripts and performs no DDL or seed-data inserts when the schema is current. Normal migrations do
not drop the application tables. The logging configuration hides Flyway's routine progress messages while retaining
its warnings and errors; the application logs one summary containing the current schema version and the number of
migrations applied.

## Build and Run

Build the application from this directory:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc-h2.jar
```

Before the web server starts, embedded Flyway uses the configured `example` data source to apply the versioned SQL
migrations under `src/main/resources/db/migration`. Flyway creates its schema history table and applies the schema,
type, and Pokemon migrations on a new in-memory database. A subsequent migration in the same process validates the
history and does nothing when the database is current. The schema and sample data live only for the duration of the
process. The application listens on `http://localhost:8080/pokemon`.

## Try the Application

List all Pokemon:

```shell
curl http://localhost:8080/pokemon/all
```

The endpoint passes Java `null` to a nullable `String` repository parameter. The generated implementation binds typed
SQL `NULL` at both occurrences of `:typeName`, and the SQL disables the optional type filter.

List flat Pokemon summaries mapped directly from matching result-set column labels:

```shell
curl http://localhost:8080/pokemon/summaries
```

`PokemonRepository.listSummaries()` uses `SELECT *` and returns `List<PokemonSummary>` without declaring
`@Jdbc.RowMapper`. The generated repository maps the `ID` and `NAME` labels to the case-insensitively matching `id` and
`name` record components, ignores the additional `TYPE_ID` result column, and invokes the record's canonical
constructor.

List Pokemon having the `Normal` type:

```shell
curl http://localhost:8080/pokemon/type/Normal
```

This endpoint calls the same repository method with `"Normal"`, demonstrating its non-null binding path while applying
the type filter.

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

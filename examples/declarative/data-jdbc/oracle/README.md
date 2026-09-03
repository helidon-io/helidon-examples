# Helidon Data JDBC Declarative using Oracle Database

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

The example uses Oracle Database Free and HikariCP. The credentials below are intended only for local development.
Oracle JDBC deserializes internal character-conversion tables when preparing statements. The sample's
`META-INF/helidon/serial-config.properties` narrowly permits the two converter classes required for that operation;
other unlisted classes remain rejected by Helidon's deserialization filter.

## Database Migrations

The example uses the Flyway Maven plugin to manage its database schema and sample data separately from application
startup. Everything needed by Flyway is contained in this sample under `src/main/resources/db/migration`:

- `V1__create_schema.sql` creates the `TYPE` and `POKEMON` tables and their foreign key;
- `V2__load_types.sql` inserts the Pokemon type reference data; and
- `V3__load_pokemon.sql` inserts the sample Pokemon.

Flyway records applied migrations and their checksums in `flyway_schema_history`. Because the example connects as the
Oracle `SYSTEM` user, whose schema is already non-empty, the plugin baselines it at version 0 before applying the
example's migrations.

## Start Oracle Database

Run this command from the `examples/declarative/data-jdbc/oracle` directory.

```shell
docker run --name oracle \
       -p 1521:1521 \
       -e ORACLE_PWD='oracle123' \
       -d container-registry.oracle.com/database/free:latest-lite
```

Before starting the application, ensure that the Oracle Database container is running and ready to use.

The password used in this example is intended only for local development. Use a strong, unique password and update the
Docker command, the Flyway environment variables shown below, and `src/main/resources/application.yaml` with the new
value. For production deployments, provide credentials through external configuration or a secrets manager instead of
storing them in source control.

## Migrate the Database

The Flyway Maven plugin reads its database connection from Flyway's standard environment variables. Set them before
applying the database migrations:

```shell
export FLYWAY_URL='jdbc:oracle:thin:@localhost:1521/FREE'
export FLYWAY_USER='system'
export FLYWAY_PASSWORD='oracle123'

mvn flyway:migrate
```

The first invocation creates the Flyway schema history table, the example tables, and the sample data. Subsequent
invocations validate the migration checksums and apply only pending migrations. When the database is current, Flyway
performs no schema or data changes. The Flyway Maven goal is intentionally not bound to the Maven build lifecycle, so
`mvn package` does not require or modify a database. The Flyway connection values are deliberately absent from
`pom.xml`; CI systems should supply these environment variables from their secret store.

## Build and Run

Build the application from this directory:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc-oracle.jar
```

The application assumes that `mvn flyway:migrate` has already initialized or upgraded the database. It does not create
or modify the schema during startup.

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

## Stop Oracle Database

To stop the Oracle Database container:

```shell
docker stop oracle
```

To delete the stopped container:

```shell
docker rm oracle
```

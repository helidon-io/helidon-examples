# Helidon Data JDBC Declarative using MySQL Database

The Java application and repository sources are shared by all database variants from the sibling `common` directory.

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

The example uses MySQL and HikariCP. The credentials below are intended only for local development.

## Start MySQL

Run this command from the `examples/declarative/data-jdbc/mysql` directory.

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.1
```

Wait until `docker logs mysql` reports that the server is ready for connections before starting the application.

## Build and Run

Build the application from this directory:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc-mysql.jar
```

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

Search for a Pokémon name or type with one repeated named parameter:

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

Count all Pokémon:

```shell
curl http://localhost:8080/pokemon/count
```

The count method uses `@Jdbc.Execution(QUERY)` because primitive `long` could otherwise mean either a scalar query or an
update count. The list method omits `@Jdbc.Execution` to demonstrate AUTO inference from its `List<Pokemon>` result.

Insert a Pokemon and return its generated identifier:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

`@Jdbc.GeneratedKeys("ID")` selects update execution without a separate `@Jdbc.Execution(UPDATE)` annotation. Generated
code adds the `ID` column through the staged generated-key builder before mapping the returned scalar.

The schema starts generated Pokemon identifiers at `20`, so the first insert into a fresh database returns that ID.
Delete it with:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

# Helidon Data JDBC Declarative with PostgreSQL

This example shows how to use Helidon Data declarative repositories with PostgreSQL. The repository interfaces define
SQL statements and result mapping, and Helidon generates the JDBC implementations at build time. To work with
`JdbcClient` directly, see the [imperative PostgreSQL example](../../../imperative/data-jdbc/postgres).

## Run This Example

### Prerequisites

- JDK 26
- Maven 3.8.0 or newer
- Docker to run the local PostgreSQL container and the integration tests

Run all commands from `examples/declarative/data-jdbc/postgres`.

### 1. Start PostgreSQL

Build the PostgreSQL image:

```shell
docker build etc/docker -t helidon-postgres
```

Start the container:

```shell
docker run --name postgres \
       -p 5432:5432 \
       -e POSTGRES_DB='pokemons' \
       -e POSTGRES_USER='user' \
       -e POSTGRES_PASSWORD='pgsql123' \
       -d helidon-postgres
```

Follow the PostgreSQL container logs:

```shell
docker logs -f postgres
```

Wait until PostgreSQL reports that it is ready to accept connections. Then press Ctrl+C to stop following the logs. The
database container continues to run.

### 2. Initialize the Schema

> **Warning:** This command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database, including all
> their data, before recreating and populating them. It does not modify tables in other databases.

```shell
docker exec -i postgres \
       psql --username=user --dbname=pokemons \
       < etc/schema.sql
```

### 3. Build and Start the Application

Build the application and run the tests:

```shell
mvn package
```

To build the application without running the tests:

```shell
mvn package -DskipTests
```

See [Configuration and Tests](#configuration-and-tests) for details about the test environment.

After the build completes, start the application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc-postgres.jar
```

The packaged application connects to the local PostgreSQL database and listens on
`http://localhost:8080/pokemon`.

### 4. Verify the Application

In another terminal, retrieve a seeded Pokemon:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Expected response:

```json
{"id":5,"name":"Meowth","type":"Normal"}
```

## API

All paths are relative to `http://localhost:8080`.

By default, `curl` displays only the response body. Add `-i` to display the HTTP status and response headers. This is
useful for responses without a body, such as `404 Not Found`.

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/pokemon/all` | Lists all Pokemon ordered by name |
| `GET` | `/pokemon/type/{name}` | Lists Pokemon having the requested type |
| `GET` | `/pokemon/search/{term}` | Finds Pokemon by name or type using the same named parameter twice |
| `GET` | `/pokemon/get/{name}` | Returns a matching Pokemon or `404` when the name is not found |
| `GET` | `/pokemon/explicit-mapper/{name}` | Uses the row mapper selected by class |
| `GET` | `/pokemon/search/{type}/{name}` | Finds a Pokemon by type and name using positional parameters |
| `GET` | `/pokemon/count` | Returns the number of Pokemon rows |
| `POST` | `/pokemon` | Inserts a Pokemon and returns the inserted Pokemon with its generated identifier |
| `PUT` | `/pokemon/{id}` | Updates a Pokemon or returns `404` when the identifier is not found |
| `DELETE` | `/pokemon/{id}` | Deletes a Pokemon and returns the number of rows affected |

Run the following mutation examples in order against a freshly initialized database.

Insert a Pokemon:

```shell
curl -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

Expected response:

```json
{"id":20,"name":"Charmander","type":"Fire"}
```

Update the Pokemon returned by the insert request:

```shell
curl -X PUT \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmeleon","type":"Fire"}' \
     http://localhost:8080/pokemon/20
```

Expected response:

```json
{"id":20,"name":"Charmeleon","type":"Fire"}
```

Delete the Pokemon returned by the update request:

```shell
curl -X DELETE http://localhost:8080/pokemon/20
```

Expected response:

```text
Deleted: 1 values
```

## Source Tour

- [`PokemonRepository`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonRepository.java) defines
  the SQL operations for Pokemon.
- [`PokemonTypeRepository`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonTypeRepository.java)
  shows a repository that does not define entity metadata at the repository level.
- [`PokemonLookup`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonLookup.java) defines the
  inherited `findByName` operation.
- [`PokemonEndpoint`](src/main/java/io/helidon/examples/declarative/data/jdbc/PokemonEndpoint.java) provides the HTTP
  API and defines the transaction boundaries.
- [`application.yaml`](src/main/resources/application.yaml) defines the named JDBC client and its local demo
  credentials. Change these credentials before using the example outside a local development environment.
- [`schema.sql`](etc/schema.sql) creates the example schema and loads the sample data.

## What the Example Demonstrates

- generated implementations of annotated JDBC repository interfaces
- inferred query execution for methods that return a list, an optional value, or a record, including methods that use
  a row mapper
- explicit execution selection for ambiguous primitive count and update results
- named and positional parameter binding, including one named parameter used twice
- mapping joined rows to a `Pokemon` containing a nested `PokemonType`
- generated key retrieval for inserts
- local JDBC transactions that combine a type lookup with an insert or update
- inherited repository methods

### Repository Shapes and Execution Inference

`PokemonRepository` extends `Data.GenericRepository<Pokemon, Integer>`, which supplies entity and identifier metadata
at the repository level. It does not add CRUD operations or generate SQL. `PokemonTypeRepository` is a standalone
repository interface that does not provide this metadata. Every operation in both repositories declares its SQL with
`@Jdbc.Statement`.

Helidon infers query execution for methods that return `List`, `Optional`, or record types, including methods annotated
with `@Jdbc.RowMapper`. Primitive `int` and `long` results are ambiguous, so `count`, update, and delete operations
select `QUERY` or `UPDATE` explicitly. `@Jdbc.GeneratedKeys("ID")` identifies the insert as an update and maps the
generated identifier.

`PokemonTypeRepository.listNames()` maps the first selected column in each row to a `String`.
`PokemonTypeRepository.listTypes()` maps the selected columns in each row to a `PokemonType` record. Helidon provides
both mappings, so the methods do not declare `@Jdbc.RowMapper`.

Named parameters bind by Java parameter name. The search by name or type uses the `term` argument for both occurrences
of `:term`. The search by type and name uses positional `?` parameters in the order that the method parameters are
declared.

`PokemonRepository` extends `PokemonLookup`, so its generated implementation also includes the inherited
`findByName(String name)` operation.

### Transactions

`PokemonEndpoint` marks each type lookup and its corresponding insert or update with `@Tx.Required`. The named client
therefore performs each compound operation in one local JDBC transaction. The integration tests verify rollback after
a deliberate failure and recovery after a uniqueness constraint violation.

## Advanced Mapper Selection

The regular query methods use `@Jdbc.RowMapper` without specifying a mapper class. Two services implement the exact
`JdbcClient.RowMapper<Pokemon>` contract:

- [`PokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonRowMapper.java) has the
  higher service weight and maps the database value unchanged.
- [`ExplicitPokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/ExplicitPokemonRowMapper.java)
  has the lower service weight and prefixes the name to make its selection visible.

The marker lookup selects `PokemonRowMapper` because it has the higher weight. The explicit repository method uses
`@Jdbc.RowMapper(ExplicitPokemonRowMapper.class)` to select a mapper by class, regardless of its service weight.

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

Expected response:

```json
{"id":5,"name":"LOW-WEIGHT EXPLICIT: Meowth","type":"Normal"}
```

## Configuration and Tests

`application.yaml` registers a direct JDBC client named `pokemon`. Both repositories select this client with
`@Jdbc.Client("pokemon")`. By default, it connects to the `pokemons` database with username `user` and password
`pgsql123`. The PostgreSQL driver registers itself and is selected from the `jdbc:postgresql` URL.

The sample username and password are intended only for this local demo. They are not a recommendation for production
use. Configure credentials that are appropriate for your environment before deploying the application.

The URL disables quoting for `RETURNING` identifiers. This allows PostgreSQL to fold the generated key column name `ID`
in the same way as the unquoted schema and repository SQL. To use a different PostgreSQL instance, update `data.url`
and the credentials. Then create the database and user, and run `etc/schema.sql` as that user.

Run the tests without packaging:

```shell
mvn test
```

Testcontainers manages a temporary PostgreSQL database for the tests, including its creation, initialization, and
removal. Because this process requires Docker, JUnit skips the tests when Docker is not available.

> **Note**
> Helidon Data JDBC is incubating, and some APIs used by this example are preview. Source types opt in locally with
> `@SuppressWarnings` and the corresponding `Api.SUPPRESS_INCUBATING` or `Api.SUPPRESS_PREVIEW` constant.

## Stop PostgreSQL

Stop the container:

```shell
docker stop postgres
```

Remove the stopped container:

```shell
docker rm postgres
```

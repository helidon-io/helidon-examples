# Helidon Data JDBC Imperative with PostgreSQL

This example shows how to execute PostgreSQL statements directly with the Helidon Data `JdbcClient`. Application code
creates each statement, binds positional parameters, selects a mapper, and invokes the terminal operation. To define
data access as repository interfaces, see the
[Declarative PostgreSQL example](../../../declarative/data-jdbc/postgres).

This module separates the HTTP and JDBC responsibilities. `PokemonService` handles routing, validation, and responses.
`PokemonStore` contains the SQL, mapping, generated key, and transaction operations.

## Run This Example

### Prerequisites

- JDK 26
- Maven 3.8.0 or newer
- Docker to run the local PostgreSQL container and the integration tests

Run all commands from `examples/imperative/data-jdbc/postgres`.

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

The image installs PostgreSQL Server on Oracle Linux 9 and uses the standalone entry point supplied by this example.

Wait until PostgreSQL accepts connections:

```shell
docker logs -f postgres
```

### 2. Initialize the Schema

> **Warning:** This command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database, including all
> their data, before recreating and populating them. It does not modify tables in other databases.

Run the schema script as the demo user:

```shell
docker exec -i postgres \
       psql --username=user --dbname=pokemons \
       < etc/schema.sql
```

The command sends `etc/schema.sql` to the PostgreSQL client in the container. The script recreates the sample tables
and their foreign key, inserts the Pokemon types and Pokemon, and commits the sample data.

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
java -jar target/helidon-examples-imperative-data-jdbc-postgres.jar
```

The packaged application uses the `pokemon` client managed by Service Registry to connect to the local PostgreSQL
database. It listens on `http://localhost:8080/pokemon`.

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

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/pokemon/all` | Lists all Pokemon ordered by name |
| `GET` | `/pokemon/type/{name}` | Lists Pokemon having the requested type |
| `GET` | `/pokemon/search/{term}` | Finds Pokemon by name or type and binds the term to two positions |
| `GET` | `/pokemon/get/{name}` | Returns a matching Pokemon or `404` when the name is not found |
| `GET` | `/pokemon/search/{type}/{name}` | Finds a Pokemon by type and name using positional parameters |
| `GET` | `/pokemon/count` | Returns the number of Pokemon rows |
| `POST` | `/pokemon` | Inserts a Pokemon and returns its generated identifier |
| `PUT` | `/pokemon/{id}` | Updates a Pokemon or returns `404` when the identifier is not found |
| `DELETE` | `/pokemon/{id}` | Deletes a Pokemon and returns the number of rows affected |

Run the following mutation examples in order against a freshly initialized database. The first generated identifier is
`20`. If the database returns a different identifier, use that value in the update and delete requests.

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

The client managed by Service Registry performs the type lookup and insert in one local JDBC transaction.

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

The path supplies the Pokemon identifier. The request body supplies a name that is not blank and an existing Pokemon
type, so it does not need an `id`. The endpoint returns `404` when the identifier does not match a row. The registry
managed client performs the type lookup and update in one local JDBC transaction.

Delete the Pokemon returned by the update request:

```shell
curl -X DELETE http://localhost:8080/pokemon/20
```

Expected response:

```text
Deleted: 1 values
```

## Source Tour

- [`PokemonStore`](src/main/java/io/helidon/examples/imperative/data/jdbc/PokemonStore.java) contains the SQL,
  parameter binding, result mapping, generated key, and transaction operations.
- [`PokemonService`](src/main/java/io/helidon/examples/imperative/data/jdbc/PokemonService.java) defines the routes,
  validates requests, and converts between the database model and HTTP representation.
- [`Main`](src/main/java/io/helidon/examples/imperative/data/jdbc/Main.java) obtains the HTTP service from Service
  Registry and registers it with the web server.
- [`PokemonRowMapper`](src/main/java/io/helidon/examples/imperative/data/jdbc/model/PokemonRowMapper.java) maps a joined
  database row to a `Pokemon` with a nested `Type`.
- [`application.yaml`](src/main/resources/application.yaml) defines the HikariCP data source, named JDBC client, and
  local demo credentials. Change these credentials before using the example outside a local development environment.
- [`schema.sql`](etc/schema.sql) creates the example schema and loads the sample data.
- [`Dockerfile`](etc/docker/Dockerfile) defines the PostgreSQL image used by the local setup and tests.

## What the Example Demonstrates

- creating a `JdbcClient.Statement` for each SQL operation
- binding positional parameters, including one value bound to two positions
- mapping list and optional query results with a row mapper
- mapping a scalar count with `long.class`
- mapping a joined row to a `Pokemon` that contains a nested `Type`
- retrieving a PostgreSQL generated identifier
- executing update and delete statements and reading the number of affected rows
- injecting a named JDBC client managed by Service Registry
- combining a type lookup and insert or update in one local JDBC transaction
- separating imperative JDBC operations from HTTP routing and response handling

## JdbcClient Operations

`PokemonStore` keeps the SQL and `JdbcClient` calls together in a focused data access class. Each method creates a
statement, binds its parameters by position, applies a mapper when needed, and selects a terminal operation that matches
the expected result.

| Result | JdbcClient operation |
| --- | --- |
| Multiple rows | `statement.map(pokemonRowMapper).list()` |
| Optional row | `statement.map(pokemonRowMapper).optional()` |
| Exactly one scalar | `statement.map(long.class).one()` |
| Generated identifier | `statement.generatedKeys().addColumn("ID").map(...).one()` |
| Update or delete count | `statement.execute()` |

The name or type search binds the same `term` value to positions `1` and `2`. The type and name search binds
`typeName` to position `1` and `name` to position `2`. `PokemonRowMapper` combines columns from the joined tables into
the nested model. A separate inline mapper converts a type row to a `Type` record.

## Client Configuration and Transactions

The client managed by Service Registry is configured under `data.clients.jdbc` and uses the HikariCP data source named
`example`:

```yaml
data:
  clients:
    jdbc:
      - name: "pokemon"
        data-source: "example"
```

`PokemonStore` selects the JDBC provider and the named client at its injection point:

```java
@Service.Inject
PokemonStore(@Data.ProviderType("jdbc")
             @Service.Named(Main.POKEMON_CLIENT)
             JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
}
```

Unlike a standalone client, the client managed by Service Registry participates in `Tx.transaction`. `PokemonStore`
wraps each type lookup and its corresponding insert or update in one local transaction. The tests verify that a
deliberate failure rolls back an insert and that the client recovers after a unique constraint violation.

## Configuration and Tests

`application.yaml` configures PostgreSQL, a HikariCP data source named `example`, and the JDBC client named `pokemon`.
By default, the application connects to the `pokemons` database at `localhost:5432` with username `user` and password
`pgsql123`. The PostgreSQL driver registers itself and is selected from the `jdbc:postgresql` URL.

The sample username and password are intended only for this local demo. They are not a recommendation for production
use. Configure credentials that are appropriate for your environment before deploying the application.

The JDBC URL disables quoting for `RETURNING` identifiers. This allows PostgreSQL to fold the generated key column name
`ID` in the same way as the unquoted schema and application SQL.

The application does not create or migrate the schema. To use an existing PostgreSQL instance, update `data.url` and
the HikariCP credentials in `application.yaml`. Then create the configured database and user, and run `etc/schema.sql`
as that user. Follow your normal provisioning and credential management practices outside this local demo.

Run the tests without packaging:

```shell
mvn test
```

Testcontainers builds `etc/docker/Dockerfile`, whose base image is
`container-registry.oracle.com/os/oraclelinux:9-slim`. It starts PostgreSQL, creates the `pokemons` database,
initializes it with `etc/schema.sql`, and exercises the query and mutation endpoints through the PostgreSQL connection.
Testcontainers removes the container afterward, so the local PostgreSQL container is not required for the tests.
Because this process requires Docker, JUnit skips the tests when Docker is not available.

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

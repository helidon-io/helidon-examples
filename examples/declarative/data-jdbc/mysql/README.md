# Helidon Data JDBC Declarative with MySQL

This example shows how to use Helidon Data declarative repositories with MySQL. The repository interfaces define SQL
statements and result mapping, and Helidon generates the JDBC implementations at build time. To work with
`JdbcClient` directly, see the [imperative MySQL example](../../../imperative/data-jdbc/mysql).

## Run This Example

### Prerequisites

- JDK 26
- Maven 3.8.0 or newer
- Docker to run the local MySQL container and the integration tests

Run all commands from `examples/declarative/data-jdbc/mysql`.

### 1. Start MySQL

Start the MySQL container:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.3
```

Follow the MySQL container logs:

```shell
docker logs -f mysql
```

Wait until MySQL reports that it is ready for connections. Then press Ctrl+C to stop following the logs. The database
container continues to run.

### 2. Initialize the Schema

> **Warning:** This command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database, including all
> their data, before recreating and populating them. It does not modify tables in other databases.

Run the schema script as the demo user:

```shell
docker exec -i mysql \
       sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --user=user pokemons' \
       < etc/schema.sql
```

The command reads the password from the `MYSQL_PASSWORD` environment variable in the container and sends
`etc/schema.sql` to the MySQL client. The script recreates the sample tables and their foreign key, inserts the Pokemon
types and Pokemon, and commits the sample data.

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
java -jar target/helidon-examples-declarative-data-jdbc-mysql.jar
```

The packaged application connects to the local MySQL database and listens on
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
type, so it does not need an `id`. The endpoint returns `404` when the identifier does not match a row.

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
  defines the SQL queries for Pokemon types.
- [`PokemonLookup`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonLookup.java) defines the
  inherited `findByName` operation.
- [`PokemonEndpoint`](src/main/java/io/helidon/examples/declarative/data/jdbc/PokemonEndpoint.java) provides the HTTP
  API and defines the transaction boundaries.
- [`application.yaml`](src/main/resources/application.yaml) defines the HikariCP data source and its local demo
  credentials. Change these credentials before using the example outside a local development environment.
- [`schema.sql`](etc/schema.sql) creates the example schema and loads the sample data.

## What the Example Demonstrates

- generated JDBC repository implementations for list, optional, insert, update, and delete operations
- named parameter binding, including one named parameter used twice
- positional parameter binding in repository parameter order
- generated binding of reference parameters as typed SQL `NULL` values
- generated mapping of the `PokemonType` record
- mapping of a joined row to a `Pokemon` that contains a nested `PokemonType`
- generated key retrieval for inserts and row count handling for updates and deletes
- explicit query selection for a primitive `long` count result
- inherited repository methods
- local JDBC transactions that combine a type lookup with an insert or update

### Repository Behavior and Execution Inference

`PokemonRepository` and `PokemonTypeRepository` are declarative JDBC repositories. Every operation in both repositories
declares its SQL with `@Jdbc.Statement`. `PokemonRepository` also extends `PokemonLookup`, so its generated
implementation includes the inherited `findByName(String name)` operation.

Helidon infers query execution for methods that return `List`, `Optional`, or record types, including methods annotated
with `@Jdbc.RowMapper`. Primitive `int` and `long` results are ambiguous, so `count`, update, and delete operations
select `QUERY` or `UPDATE` explicitly. `@Jdbc.GeneratedKeys("ID")` identifies the insert as an update. The generated
implementation requests `ID` as a generated key before it runs the statement, then maps the returned scalar value.

`PokemonTypeRepository.listNames()` maps the first selected column in each row to a `String`.
`PokemonTypeRepository.listTypes()` maps the selected columns in each row to a `PokemonType` record. Helidon provides
both mappings, so the methods do not declare `@Jdbc.RowMapper`.

Named parameters bind by Java parameter name. The search by name or type uses the `term` argument for both occurrences
of `:term`. In the search by type and name, `typeName` binds to the first positional `?` parameter and `name` binds to
the second. Generated code also binds a null reference argument as a typed SQL `NULL` value.

The generated repositories map a flat row to a `PokemonType` record. The Pokemon row mapper combines columns from the
joined tables into a `Pokemon` that contains a nested `PokemonType`.

### Transactions

`PokemonEndpoint` marks each type lookup and its corresponding insert or update with `@Tx.Required`. The default JDBC
client therefore performs each compound operation in one local transaction.

## Advanced Mapper Selection

The regular query methods use `@Jdbc.RowMapper` without specifying a mapper class. Two services implement the exact
`JdbcClient.RowMapper<Pokemon>` contract:

- [`PokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonRowMapper.java) has weight
  `Weighted.DEFAULT_WEIGHT + 10` and maps the database value unchanged.
- [`ExplicitPokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/ExplicitPokemonRowMapper.java)
  has weight `Weighted.DEFAULT_WEIGHT - 10` and prefixes the name to make its selection visible.

`ExplicitPokemonRowMapper` comes first alphabetically, but the marker lookup selects `PokemonRowMapper` because it has
the higher service weight. The explicit repository method uses `@Jdbc.RowMapper(ExplicitPokemonRowMapper.class)` to
select a mapper by class, regardless of its service weight.

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

Expected response:

```json
{"id":5,"name":"LOW-WEIGHT EXPLICIT: Meowth","type":"Normal"}
```

## Configuration and Tests

`application.yaml` configures MySQL Connector/J and a HikariCP data source named `example`. The default JDBC client uses
this data source. By default, it connects to the `pokemons` database at `localhost:3306` with username `user` and
password `changeit`.

The sample username and password are intended only for this local demo. They are not a recommendation for production
use. Configure credentials that are appropriate for your environment before deploying the application.

The application does not create or migrate the schema. To use an existing MySQL instance, update `data.url` and the
data source credentials in `application.yaml`. Then create the configured database and user, and run `etc/schema.sql`
as that user. Follow your normal provisioning and credential management practices outside this local demo.

Run the tests without packaging:

```shell
mvn test
```

Testcontainers runs the tests against `container-registry.oracle.com/mysql/community-server:9.7.3`, the same image used
for the local setup. It creates the `pokemons` database, initializes it with `etc/schema.sql`, and exercises the query
and mutation endpoints through the MySQL connection. Testcontainers removes the container afterward, so the local
MySQL container is not required for the tests. Because this process requires Docker, JUnit skips the tests when Docker
is not available.

> **Note**
> Helidon Data JDBC is incubating, and some APIs used by this example are preview. Source types opt in locally with
> `@SuppressWarnings` and the corresponding `Api.SUPPRESS_INCUBATING` or `Api.SUPPRESS_PREVIEW` constant.

## Stop MySQL

Stop the container:

```shell
docker stop mysql
```

Remove the stopped container:

```shell
docker rm mysql
```

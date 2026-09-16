# Helidon Data JDBC Imperative with MySQL

This example shows how to execute MySQL statements directly with the Helidon Data `JdbcClient`. Application code
creates each statement, binds positional parameters, selects a mapper, and invokes the terminal operation. To define
data access as repository interfaces, see the
[Declarative MySQL example](../../../declarative/data-jdbc/mysql).

## Run This Example

### Prerequisites

- JDK 26
- Maven 3.8.0 or newer
- Docker to run the local MySQL container and the integration tests

Run all commands from `examples/imperative/data-jdbc/mysql`.

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

Wait until MySQL accepts connections:

```shell
docker logs -f mysql
```

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
java -jar target/helidon-examples-imperative-data-jdbc-mysql.jar
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

The standalone client performs the type lookup and insert as separate JDBC operations.

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
type, so it does not need an `id`. The endpoint returns `404` when the identifier does not match a row. The standalone
client performs the type lookup and update as separate JDBC operations.

Delete the Pokemon returned by the update request:

```shell
curl -X DELETE http://localhost:8080/pokemon/20
```

Expected response:

```text
Deleted: 1 values
```

## Source Tour

- [`Main`](src/main/java/io/helidon/examples/imperative/data/jdbc/Main.java) builds the standalone `JdbcClient` from
  direct connection settings and registers the HTTP service.
- [`PokemonService`](src/main/java/io/helidon/examples/imperative/data/jdbc/PokemonService.java) defines the routes,
  validates requests, and performs the JDBC operations.
- [`PokemonRowMapper`](src/main/java/io/helidon/examples/imperative/data/jdbc/model/PokemonRowMapper.java) maps a joined
  database row to a `Pokemon` with a nested `Type`.
- [`application.yaml`](src/main/resources/application.yaml) contains the direct connection settings and local demo
  credentials. Change these credentials before using the example outside a local development environment.
- [`schema.sql`](etc/schema.sql) creates the example schema and loads the sample data.

## What the Example Demonstrates

- creating a `JdbcClient.Statement` for each SQL operation
- binding positional parameters, including one value bound to two positions
- mapping list and optional query results with a row mapper
- mapping a scalar count with `long.class`
- mapping a joined row to a `Pokemon` that contains a nested `Type`
- retrieving a MySQL generated identifier
- executing update and delete statements and reading the number of affected rows
- looking up a type before inserting or updating a Pokemon through separate JDBC operations

## JdbcClient Operations

`PokemonService` keeps the SQL and `JdbcClient` calls visible in application code. Each method creates a statement,
binds its parameters by position, applies a mapper when needed, and selects a terminal operation that matches the
expected result.

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

## Client Construction and Transaction Behavior

`Main` reads the values under `app.database` and uses the public builder to create the client:

```java
JdbcClient jdbcClient = JdbcClient.builder()
        .connection(connection -> connection
                .url(url)
                .username(username)
                .password(password))
        .build();
```

MySQL Connector/J registers itself and is selected from the configured JDBC URL. This client is standalone and does
not use `data.clients.jdbc`. Each terminal operation owns its connection and does not participate in `Tx.transaction`.
The type lookup and mutation in each insert or update flow therefore run as separate JDBC operations. For a client
managed by Service Registry that participates in local transactions, see the [PostgreSQL example](../postgres).

## Configuration and Tests

The connection settings are under `app.database` in `application.yaml`. By default, the application connects to the
`pokemons` database at `localhost:3306` with username `user` and password `changeit`.

The sample username and password are intended only for this local demo. They are not a recommendation for production
use. Configure credentials that are appropriate for your environment before deploying the application.

The application does not create or migrate the schema. To use an existing MySQL instance, update the URL and
credentials in `application.yaml`. Then create the configured database and user, and run `etc/schema.sql` as that user.
Follow your normal provisioning and credential management practices outside this local demo.

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

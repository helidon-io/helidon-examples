# Helidon Data JDBC Imperative with Oracle Database

This example shows how to execute Oracle Database statements directly with the Helidon Data `JdbcClient`. Application
code creates each statement, binds positional parameters, selects a mapper, and invokes the terminal operation. To define
data access as repository interfaces, see the
[Declarative Oracle Database example](../../../declarative/data-jdbc/oracle).

## Run This Example

### Prerequisites

- JDK 26
- Maven 3.8.0 or newer
- Docker to run the local Oracle Database container and the integration tests

Run all commands from `examples/imperative/data-jdbc/oracle`.

### 1. Start Oracle Database

Start the Oracle Database container:

```shell
docker run --name oracle \
       -p 1521:1521 \
       -e ORACLE_PWD='oracle123' \
       -v "$PWD/etc/setup-user.sql:/opt/oracle/scripts/startup/01-setup-user.sql:ro" \
       -v "$PWD/etc:/opt/helidon:ro" \
       -d container-registry.oracle.com/database/free:23.26.3.0-lite
```

The first volume places the demo user provisioning script in the startup directory for the container. The
`23.26.3.0-lite` image runs scripts from this directory when the database starts. The second volume makes
`etc/schema.sql` and its SQL*Plus wrapper available in the container.

Follow the Oracle Database container logs:

```shell
docker logs -f oracle
```

Wait until Oracle Database reports that it is ready and `setup-user.sql` has completed successfully. Then press Ctrl+C
to stop following the logs. The database container continues to run.

#### Demo User Provisioning

During startup, `etc/setup-user.sql` creates the `pokemon` user in `FREEPDB1` if the user does not already exist. The
script can run more than once without trying to recreate the user. It uses an administrator connection only while it
performs this provisioning. The application and its schema connect as `pokemon`, not as `SYS` or `SYSTEM`.

The script grants the permissions required by the demo and gives `pokemon` a limited quota on the dedicated
`POKEMON_DATA` tablespace. It creates this tablespace because the `23.26.3.0-lite` image does not provide a general
purpose `USERS` tablespace.

### 2. Initialize the Schema

> **Warning:** This command drops the existing `POKEMON` and `TYPE` tables in the `pokemon` schema, including all their
> data, before recreating and populating them. It does not modify tables in other schemas.

Run the schema script as the demo user:

```shell
docker exec oracle \
       sqlplus -s pokemon/changeit@//localhost:1521/FREEPDB1 \
       @/opt/helidon/run-schema.sql
```

The mounted `run-schema.sql` wrapper stops at the first SQL error and runs `etc/schema.sql`. The schema script recreates
the sample tables and their foreign key, inserts the Pokemon types and Pokemon, and commits the sample data.

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
java '-Dhelidon.serialFilter.pattern=oracle.sql.converter.*' \
     -jar target/helidon-examples-imperative-data-jdbc-oracle.jar
```

The system property allows Oracle JDBC to read its character set conversion data through Java serialization. See
[Oracle JDBC Serialization](#oracle-jdbc-serialization) for details.

The packaged application connects to the local Oracle Database instance and listens on
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

UCP creates the initial connections when the application first accesses the database. The first request can therefore
take longer than later requests, which reuse connections from the pool.

## API

All paths are relative to `http://localhost:8080`.

By default, `curl` displays only the response body. Add `-i` to display the HTTP status and response headers. This is
useful for responses without a body, such as `404 Not Found`.

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/pokemon/all` | Lists all Pokemon ordered by name |
| `GET` | `/pokemon/type/{name}` | Lists Pokemon having the requested type |
| `GET` | `/pokemon/search/{term}` | Finds Pokemon by name or type and binds the term to two positions |
| `GET` | `/pokemon/get/{name}` | Returns a matching Pokemon or `404` when the name is not found |
| `GET` | `/pokemon/search/{type}/{name}` | Finds a Pokemon by type and name using positional parameters |
| `GET` | `/pokemon/count` | Returns the number of Pokemon rows |
| `POST` | `/pokemon` | Inserts a Pokemon and returns the inserted Pokemon with its generated identifier |
| `PUT` | `/pokemon/{id}` | Updates a Pokemon or returns `404` when the identifier is not found |
| `DELETE` | `/pokemon/{id}` | Deletes a Pokemon and returns the number of rows affected |

Run the following mutation examples in order against a freshly initialized schema. The first generated identifier is
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

- [`Main`](src/main/java/io/helidon/examples/imperative/data/jdbc/Main.java) creates the standalone `JdbcClient` from
  the configured UCP data source and registers the HTTP service.
- [`PokemonService`](src/main/java/io/helidon/examples/imperative/data/jdbc/PokemonService.java) defines the routes,
  validates requests, and performs the JDBC operations.
- [`PokemonRowMapper`](src/main/java/io/helidon/examples/imperative/data/jdbc/model/PokemonRowMapper.java) maps a joined
  database row to a `Pokemon` with a nested `Type`.
- [`application.yaml`](src/main/resources/application.yaml) defines the UCP data source and its local demo credentials.
  Change these credentials before using the example outside a local development environment.
- [`setup-user.sql`](etc/setup-user.sql) creates the demo user and its tablespace. Change its demo password together
  with the password in `application.yaml`.
- [`run-schema.sql`](etc/run-schema.sql) runs the schema script and stops if SQL*Plus reports an error.
- [`schema.sql`](etc/schema.sql) creates the example schema and loads the sample data.

## What the Example Demonstrates

- creating a `JdbcClient.Statement` for each SQL operation
- binding positional parameters, including one value bound to two positions
- mapping list and optional query results with a row mapper
- mapping a scalar count with `long.class`
- mapping a joined row to a `Pokemon` that contains a nested `Type`
- retrieving an Oracle Database generated identifier
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

`Main` creates the client from the configured UCP data source name:

```java
JdbcClient jdbcClient = JdbcClient.create(builder -> builder.dataSourceName("example"));
```

The client remains standalone, and the UCP data source remains externally managed. Each terminal operation owns its
connection and does not participate in `Tx.transaction`. The type lookup and mutation in each insert or update flow
therefore run as separate JDBC operations. This example does not use `data.clients.jdbc`. For a client managed by
Service Registry that participates in local transactions, see the [PostgreSQL example](../postgres).

## Configuration and Tests

`application.yaml` configures Oracle JDBC and a UCP data source named `example`. By default, the application connects
to the `FREEPDB1` pluggable database at `localhost:1521` with username `pokemon` and password `changeit`.

The sample application password and the `oracle123` administrator password used by the local container are intended
only for this demo. They are not recommendations for production use. Configure credentials that are appropriate for
your environment before deploying the application.

The application does not create or migrate the schema. To use an existing Oracle Database instance, update `data.url`
and the UCP credentials in `application.yaml`. Then provision the configured user and run `etc/schema.sql` as that user.
Follow your normal provisioning and credential management practices outside this local demo.

### Oracle JDBC Serialization

Oracle JDBC reads bundled character set conversion data through Java serialization. Helidon rejects Java
deserialization by default unless the application explicitly allows the required classes. The schema setup runs in a
separate SQL*Plus process and does not initialize the Oracle JDBC driver in the application.

When the application makes its first JDBC request, `helidon.serialFilter.pattern` allows the
`oracle.sql.converter.*` package through the Helidon serialization filter. Other classes remain subject to the default
rejection policy.

### Tests

Run the tests without packaging:

```shell
mvn test
```

Testcontainers runs the tests against `container-registry.oracle.com/database/free:23.26.3.0-lite`, the same image used
for the local setup. It provisions the `pokemon` user, initializes the database with `etc/schema.sql`, and exercises the
query and mutation endpoints through the Oracle UCP data source. Testcontainers removes the container afterward, so the
local Oracle Database container is not required for the tests. Because this process requires Docker, JUnit skips the
tests when Docker is not available.

> **Note**
> Helidon Data JDBC is incubating, and some APIs used by this example are preview. Source types opt in locally with
> `@SuppressWarnings` and the corresponding `Api.SUPPRESS_INCUBATING` or `Api.SUPPRESS_PREVIEW` constant.

## Stop Oracle Database

Stop the container:

```shell
docker stop oracle
```

Remove the stopped container:

```shell
docker rm oracle
```

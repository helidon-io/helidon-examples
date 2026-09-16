# Helidon Data JDBC Imperative Examples

These examples show how to execute SQL directly with the Helidon Data `JdbcClient`. Application code creates statements,
binds parameters, selects row mappers, and invokes terminal operations for queries, updates, and generated keys.

Choose the imperative approach when you want JDBC operations to remain explicit in application code. To define data
access as repository interfaces and generate their implementations, use the
[declarative JDBC examples](../../declarative/data-jdbc).

Each database directory contains a self-contained Maven application for the same Pokemon API:

| Directory | Database | JDBC client construction |
| --- | --- | --- |
| [`mysql`](mysql) | MySQL | Standalone client built from direct connection settings |
| [`oracle`](oracle) | Oracle Database | Standalone client built from a named UCP data source |
| [`postgres`](postgres) | PostgreSQL | Named client managed by Service Registry that uses a HikariCP data source |

Open the README in your chosen directory for database preparation, configuration, startup, and runnable endpoint
examples. Each README also identifies the container image used by its tests and optional local setup.

## How the Examples Work

MySQL and Oracle Database keep their JDBC operations in `PokemonService`. PostgreSQL keeps its JDBC and transaction
operations in `PokemonStore`, while `PokemonService` handles routing, validation, and responses. Each data access method
creates a `JdbcClient.Statement`, binds positional parameters, applies a mapper where needed, and invokes a terminal
operation such as `list()`, `optional()`, `one()`, or `execute()`.

The modules deliberately demonstrate different client construction and ownership models. MySQL builds a standalone
client from connection settings under `app.database`. Oracle Database builds a standalone client from the configured
UCP data source named `example`. Each terminal operation on these standalone clients owns its connection and does not
participate in `Tx.transaction`. PostgreSQL injects the registry-managed client named `pokemon`, which can participate
in local transactions.

Every database variant exposes the same `/pokemon` HTTP API. The API lists, searches, retrieves, and counts seeded
Pokemon. It also inserts a Pokemon with `POST`, updates one with `PUT /pokemon/{id}`, and deletes one with
`DELETE /pokemon/{id}`. The database-specific READMEs include a complete API table, a runnable mutation sequence, and
an explanation of the relevant `JdbcClient` behavior.

Each module includes an `etc/schema.sql` file that creates and populates the sample tables. Run that script before you
start an application against your own database. The application does not create or migrate its schema. Tests instead
use Testcontainers to start the corresponding database and load the same schema automatically. Oracle Database and
PostgreSQL use standard identity syntax; MySQL uses `AUTO_INCREMENT`.

## Build the Examples

Use JDK 26 and Maven 3.8.0 or newer to build the examples. Docker is required only for tests that use containers. If
Docker is unavailable, JUnit skips those test classes.

From this directory, build and test every database variant:

```shell
mvn verify
```

Your environment must be able to pull each configured database image from its registry to run the tests.

To build every variant without running tests:

```shell
mvn verify -DskipTests
```

To build one variant, first change to its directory. For example:

```shell
cd mysql
```

Then build the module:

```shell
mvn package
```

Follow the README for that variant to prepare the database and start the packaged application.

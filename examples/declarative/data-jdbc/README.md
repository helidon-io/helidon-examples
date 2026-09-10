# Helidon Data JDBC Declarative Examples

These examples show how to define JDBC data access as Helidon Data repository interfaces. Repository methods declare
their SQL and result mapping with annotations, and Helidon generates the JDBC-backed implementations at build time.

Choose the declarative approach when a repository interface is the right boundary for your application and you want
Helidon to generate the implementation. To construct statements and invoke `JdbcClient` operations directly, use the
[imperative JDBC examples](../../imperative/data-jdbc).

Each database directory contains a self-contained Maven application for the same Pokemon API:

| Directory | Database | JDBC configuration |
| --- | --- | --- |
| [`mysql`](mysql) | MySQL | Default client backed by a named HikariCP data source |
| [`oracle`](oracle) | Oracle Database | Default client backed by a named UCP data source |
| [`postgres`](postgres) | PostgreSQL | Named `pokemon` client with direct connection settings |

Open the README in your chosen directory for database preparation, configuration, startup, and runnable endpoint
examples. Each README also identifies the container image used by its tests and optional local setup.

## How the Examples Work

Each module defines `PokemonRepository` and `TypeRepository` interfaces under `src/main`. The repositories use
`@Jdbc.Statement` to associate SQL with each method and use method return types and JDBC annotations to select execution
and mapping behavior. The generated implementations use the JDBC client configured under `data.clients.jdbc`.

The repository interfaces omit `@Data.Provider("jdbc")` because `helidon-data-jdbc-codegen` is the only Helidon Data
provider on the annotation processor path. Specify the provider when an annotation processor path contains more than
one Helidon Data provider.

MySQL and Oracle Database configure the default JDBC client with named HikariCP and UCP data sources, respectively.
PostgreSQL configures a client named `pokemon` with direct connection settings; both PostgreSQL repositories select that
client with `@Jdbc.Client("pokemon")`.

Every database variant exposes the same `/pokemon` HTTP API. The API lists, searches, retrieves, and counts seeded
Pokemon. It also inserts a Pokemon with `POST`, updates one with `PUT /pokemon/{id}`, and deletes one with
`DELETE /pokemon/{id}`. The database-specific READMEs include the complete request sequence and explain the relevant
repository behavior.

Each module includes an `etc/schema.sql` file that creates and populates the sample tables. Run that script before you
start an application against your own database. The application does not create or migrate its schema. Tests instead
use Testcontainers to start the corresponding database and load the same schema automatically. Oracle Database and
PostgreSQL use standard identity syntax; MySQL uses `AUTO_INCREMENT`.

All modules pass `-Ahelidon.api.preview=ignore` through the Maven compiler configuration, so the Java sources do not
need preview-warning suppression annotations.

## Prerequisites

To build the examples, you need:

- A JDK
- Maven

Docker is required only for the container-backed tests. If Docker is unavailable, JUnit skips those test classes.

## Build the Examples

From this directory, build and test every database variant:

```shell
mvn verify
```

Your environment must be able to pull each configured database image from its registry to run the tests.

To build every variant without running tests:

```shell
mvn verify -DskipTests
```

To build one variant, change to its directory. For example:

```shell
cd mysql
mvn package
```

Follow that variant's README to prepare the database and start the packaged application.

# Helidon Data JDBC Declarative Examples

These examples show how to define JDBC data access as Helidon Data repository interfaces. Repository methods declare
their SQL and result mapping with annotations, and Helidon generates the JDBC implementations at build time.

Choose the declarative approach when a repository interface is the right boundary for your application and you want
Helidon to generate the implementation. To construct statements and invoke `JdbcClient` operations directly, use the
[imperative JDBC examples](../../imperative/data-jdbc).

Each database directory contains a self-contained Maven application for the same Pokemon API:

| Directory | Database | JDBC configuration |
| --- | --- | --- |
| [`mysql`](mysql) | MySQL | Default client that uses a named HikariCP data source |
| [`oracle`](oracle) | Oracle Database | Default client that uses a named UCP data source |
| [`postgres`](postgres) | PostgreSQL | Named `pokemon` client with direct connection settings |

Open the README in your chosen directory for database preparation, configuration, startup, and runnable endpoint
examples. Each README also identifies the container image used by its tests and optional local setup.

## How the Examples Work

Each module defines `PokemonRepository` and `PokemonTypeRepository` interfaces under `src/main`. The repositories use
`@Jdbc.Statement` to associate SQL with each method and use method return types and JDBC annotations to select execution
and mapping behavior. The generated implementations use the JDBC client configured under `data.clients.jdbc`.

`PokemonTypeRepository` includes two methods that return multiple rows without `@Jdbc.RowMapper`. The `listNames`
method maps the first selected column in each row to a `String`. The `listTypes` method maps the selected columns in
each row to a `PokemonType` record. Helidon provides both mappings.

The repository interfaces omit `@Data.Provider("jdbc")` because `helidon-data-jdbc-codegen` is the only Helidon Data
provider on the annotation processor path. Specify the provider when an annotation processor path contains more than
one Helidon Data provider.

MySQL and Oracle Database configure the default JDBC client with named HikariCP and UCP data sources, respectively.
PostgreSQL configures a client named `pokemon` with direct connection settings. Both PostgreSQL repositories select
that client with `@Jdbc.Client("pokemon")`.

Every database variant exposes the same `/pokemon` HTTP API. The API lists, searches, retrieves, and counts seeded
Pokemon. It also inserts a Pokemon with `POST`, updates one with `PUT /pokemon/{id}`, and deletes one with
`DELETE /pokemon/{id}`. The database-specific READMEs include a complete API table, a runnable mutation sequence, and
an explanation of the relevant repository behavior.

Each module includes an `etc/schema.sql` file that creates and populates the sample tables. Run that script before you
start an application against your own database. The application does not create or migrate its schema. Tests instead
use Testcontainers to start the corresponding database and load the same schema automatically. Oracle Database and
PostgreSQL use standard identity syntax. MySQL uses `AUTO_INCREMENT`.

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

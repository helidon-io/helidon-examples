# Helidon Data JDBC Imperative Examples

These examples demonstrate imperative use of Helidon Data JDBC. The applications execute SQL for queries, updates,
generated keys, row mapping, and transaction behavior without generated repository implementations.

The same Pokemon application is available for several databases. Each database directory is a separate Maven application
with its own JDBC dependency, datasource configuration, and instructions for preparing and connecting to that database.

| Directory | Database | JDBC client construction |
| --- | --- | --- |
| [`h2`](h2) | Embedded, in-memory H2 database | Programmatic registry configuration and a standalone client from an existing `DataSource` |
| [`mysql`](mysql) | MySQL | `JdbcClient.builder()` with direct connection properties |
| [`oracle`](oracle) | Oracle Database | `JdbcClient.create(Consumer)` with a named data source |
| [`postgres`](postgres) | PostgreSQL | `buildPrototype()` followed by `JdbcClient.create(config)` |

See the `README.md` in the selected database directory for database setup, application startup, and endpoint examples.
Those READMEs identify the Docker images used by the samples. Ensure you have permission to pull each image, or use an
image from the appropriate registry.
The H2 variant does not require an external database and is the quickest way to run the sample.

## Application Layout

Each database module is a self-contained Maven application. Its `src/main` directory contains the Java application,
sample schema initialization utility, and database specific configuration. JDBC dependencies and supporting
documentation also remain with the corresponding database module.

The H2 application contributes a named `JdbcClientConfig` programmatically and injects the resulting registry managed
client into its imperative HTTP service. It also constructs a standalone setup client from the same existing
`DataSource`. The other applications construct
standalone clients through different public API forms. Each `SchemaInitializer` recreates and populates the schema as a
convenience for running the sample, and is not intended for production schema management. In a production environment,
create the schema and populate the required data before starting the application. Every database module supplies its
corresponding JDBC driver. H2, Oracle Database, and PostgreSQL use standard identity syntax. MySQL uses the equivalent
`AUTO_INCREMENT` definition.

Only the registry managed H2 client participates in `Tx.transaction`. The standalone clients use an operation owned
connection for each terminal JDBC operation.

## Build the Examples

From this directory, build every database variant:

```shell
mvn verify
```

To build only one variant, change to its directory. For example:

```shell
cd h2
mvn package
```

The database specific README.md provides the command for starting the resulting application and any database preparation
required before startup.

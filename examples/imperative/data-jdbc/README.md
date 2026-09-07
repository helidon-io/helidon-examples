# Helidon Data JDBC Imperative Examples

These examples demonstrate imperative use of Helidon Data JDBC. The applications execute SQL for queries, updates,
generated keys, row mapping, and transaction behavior without generated repository implementations.

The same Pokemon application is available for several databases. Each database directory is a separate Maven application
with its own JDBC dependency, datasource configuration, and instructions for preparing and connecting to that database.

| Directory | Database | JDBC client construction |
| --- | --- | --- |
| [`mysql`](mysql) | MySQL | `JdbcClient.builder()` with direct connection properties |
| [`oracle`](oracle) | Oracle Database | Standalone client configured with a named data source |
| [`postgres`](postgres) | PostgreSQL | Configuration-managed named client |

See the `README.md` in the selected database directory for database setup, application startup, and endpoint examples.
Those READMEs identify the Docker images used by the samples. Ensure you have permission to pull each image, or use an
image from the appropriate registry.

## Application Layout

Each database module is a self-contained Maven application. Its `src/main` directory contains the Java application and
database-specific configuration, while `etc/schema.sql` contains the schema and sample data. The application never
creates or replaces its own schema. Follow the database-specific README to run that script before application startup.

The PostgreSQL application injects a configuration-managed named client into its imperative HTTP service. MySQL and
Oracle Database construct standalone application clients through different public API forms. Every database module
supplies its corresponding production JDBC driver and uses H2 for its test suite. Tests run the same `etc/schema.sql`
in the matching H2 compatibility mode, so no database container is required for tests. Oracle Database and PostgreSQL
use standard identity syntax. MySQL uses the equivalent `AUTO_INCREMENT` definition.

The registry-managed PostgreSQL client participates in `Tx.transaction`. The standalone clients use an operation-owned
connection for each terminal JDBC operation.

## Build the Examples

From this directory, build every database variant:

```shell
mvn verify
```

To build only one variant, change to its directory. For example:

```shell
cd mysql
mvn package
```

The database specific README.md provides the command for starting the resulting application and any database preparation
required before startup.

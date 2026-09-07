# Helidon Data JDBC Declarative Examples

These examples demonstrate the Helidon Data declarative programming model with JDBC. The application defines repository
interfaces and uses Helidon Data code generation to create their JDBC implementations.

The same Pokemon application is available for several databases. Each database directory is a separate Maven application
with its own JDBC dependency, datasource configuration, and instructions for preparing and connecting to that database.

| Directory | Database | Registry managed JDBC client configuration |
| --- | --- | --- |
| [`mysql`](mysql) | MySQL | Implicit Default JDBC Client with a named data source |
| [`oracle`](oracle) | Oracle Database | Explicit Default JDBC Client with a named data source |
| [`postgres`](postgres) | PostgreSQL | Named `pokemon` client with inline connection properties |

See the `README.md` in the selected database directory for database setup, application startup, and endpoint examples.
Those READMEs identify the Docker images used by the samples. Ensure you have permission to pull each image, or use an
image from the appropriate registry.

## Application Layout

Each database module is a self-contained Maven application. Its `src/main` directory contains the Java application,
repository interfaces, and database-specific configuration. JDBC dependencies and supporting documentation also remain
with the corresponding database module.

The repository interfaces do not require `@Data.Provider("jdbc")` because `helidon-data-jdbc-codegen` is the only
Helidon Data provider on the annotation processor path. The annotation is needed to select JDBC when the annotation
processor path contains more than one Helidon Data provider.

Each module configures a client under `data.clients.jdbc`. Oracle Database names the Default JDBC Client explicitly.
MySQL omits the name and uses the default value. PostgreSQL configures a client named `pokemon` and selects it with
`@Jdbc.Client("pokemon")`. PostgreSQL places its connection properties directly in the JDBC client configuration;
MySQL uses a named HikariCP data source and Oracle Database uses a named UCP data source. Each external-database module
provides `etc/schema.sql`, which must be run before starting the application, and uses H2 in the matching compatibility
mode for tests. Oracle Database and PostgreSQL use standard identity syntax. MySQL uses the equivalent `AUTO_INCREMENT`
definition.

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

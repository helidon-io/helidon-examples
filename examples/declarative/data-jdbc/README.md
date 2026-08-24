# Helidon Data JDBC Declarative Examples

These examples demonstrate the Helidon Data declarative programming model with JDBC. The application defines repository
interfaces and uses Helidon Data code generation to create their JDBC implementations.

The same Pokemon application is available for several databases. Each database directory is a separate Maven application
with its own JDBC dependency, datasource configuration, and instructions for preparing and connecting to that database.

| Directory | Database |
| --- | --- |
| [`h2`](h2) | Embedded, in-memory H2 database |
| [`mysql`](mysql) | MySQL |
| [`oracle`](oracle) | Oracle Database |
| [`postgres`](postgres) | PostgreSQL |

See the `README.md` in the selected database directory for database setup, application startup, and endpoint examples.
Those READMEs identify the Docker images used by the samples. Ensure you have permission to pull each image, or use an
image from the appropriate registry.
The H2 variant does not require an external database and is the quickest way to run the sample.

## Shared Application

The [`common`](common) directory contains the Java application, repository interfaces, and SQL scripts used by all
database variants. It is source content for the database modules and is not a standalone Maven application.

Each database module compiles the application sources from `common/src/main/java`. The module owns the files that vary
by database, such as `application.yaml`, JDBC dependencies, and supporting documentation.

The repository interfaces intentionally omit `@Data.Provider("jdbc")`. Each database module includes JDBC as its only
Helidon Data persistence provider code generator, so JDBC generates implementations for all repositories. Use
`@Data.Provider("jdbc")` to select JDBC when multiple persistence provider code generators are present in the same
compilation.

The shared `init.sql` uses Maven resource filtering for the generated identifier definition. H2, Oracle Database, and
PostgreSQL use standard identity syntax. The MySQL module overrides the relevant Maven properties to generate the
equivalent `AUTO_INCREMENT` definition. Maven places the resulting database-specific `init.sql`, together with the
shared `drop.sql`, in each application's JAR.

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

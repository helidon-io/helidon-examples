# Helidon Data JDBC Declarative Examples

These examples demonstrate the Helidon Data declarative programming model with JDBC. The application defines repository
interfaces and uses Helidon Data code generation to create their JDBC implementations.

The same Pokemon application is available for several databases. Each database directory is a separate Maven application
with its own JDBC dependency, datasource configuration, and instructions for preparing and connecting to that database.

| Directory | Database | Registry managed JDBC client configuration |
| --- | --- | --- |
| [`h2`](h2) | Embedded, in-memory H2 database | Explicit Default JDBC Client with a named data source |
| [`mysql`](mysql) | MySQL | Implicit Default JDBC Client with a named data source |
| [`oracle`](oracle) | Oracle Database | Explicit Default JDBC Client with a named data source |
| [`postgres`](postgres) | PostgreSQL | Named `pokemon` client with inline connection properties |

See the `README.md` in the selected database directory for database setup, application startup, and endpoint examples.
Those READMEs identify the Docker images used by the samples. Ensure you have permission to pull each image, or use an
image from the appropriate registry.
The H2 variant does not require an external database and is the quickest way to run the sample.

## Application Layout

Each database module is a self-contained Maven application. Its `src/main` directory contains the Java application,
repository interfaces, sample schema initialization utility, and database specific configuration. JDBC dependencies
and supporting documentation also remain with the corresponding database module.

The repository interfaces do not require `@Data.Provider("jdbc")` because `helidon-data-jdbc-codegen` is the only
Helidon Data provider on the annotation processor path. The annotation is needed to select JDBC when the annotation
processor path contains more than one Helidon Data provider.

Each module configures a client under `data.clients.jdbc`. H2 and Oracle Database name the Default JDBC Client
explicitly. MySQL omits the name and uses the default value. PostgreSQL configures a client named `pokemon` and selects
it with `@Data.PersistenceUnit("pokemon")`. PostgreSQL places its connection properties directly in the JDBC client
configuration. The other modules reference a named HikariCP data source. Each `SchemaInitializer` recreates and
populates the schema as a convenience for running the sample, and is not intended for production schema management. In
a production environment, create the schema and populate the required data before starting the application. Every
database module supplies its corresponding JDBC driver. H2, Oracle Database, and PostgreSQL use standard identity
syntax. MySQL uses the equivalent `AUTO_INCREMENT` definition.

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

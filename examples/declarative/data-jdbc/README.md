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
The H2 variant does not require an external database and is the quickest way to run the sample.

## Shared Application

The [`common`](common) directory contains the Java application, repository interfaces, tests, and SQL scripts shared by
all database variants. It is source content for the database modules and is not a standalone Maven application.

Each database module compiles the sources from `common/src/main/java`. The declarative tests in
`common/src/test/java` verify the generated repository and row-mapper behavior. Database-specific directories keep only
the files that vary by database, such as `application.yaml`, the JDBC driver dependency, and supporting documentation.

The shared `init.sql` uses Maven resource filtering for the generated identifier definition. H2, Oracle Database, and
PostgreSQL use standard identity syntax. The MySQL module overrides the relevant Maven properties to generate the
equivalent `AUTO_INCREMENT` definition. Maven places the resulting database-specific `init.sql`, together with the
shared `drop.sql`, in each application's JAR.

## Build the Examples

From this directory, build and test every database variant:

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

## Endpoint Validation

After starting one packaged application, validate its endpoints from that database directory. For example, from the
`h2` directory:

```shell
mvn test -Pendpoint-validation
```

The test invokes the running application's HTTP endpoints and validates queries, mapping, generated keys, transactions,
and updates against the application's configured database. It uses `http://localhost:8080` by default. Override the URL
when the application listens elsewhere:

```shell
mvn test -Pendpoint-validation -Dbase-url=http://localhost:9080
```

Run this command from one selected database directory, not from this multi-module directory.

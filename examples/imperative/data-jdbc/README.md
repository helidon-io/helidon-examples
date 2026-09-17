# Helidon Data JDBC Imperative Examples

These examples demonstrate Java SE imperative applications that use Helidon Data JDBC and Helidon WebServer
to access MySQL, Oracle Database, and PostgreSQL. Application code works directly with `JdbcClient` to create
statements, bind parameters, map result rows, and execute database operations.

Each directory contains the same Pokémon application for a different database. To define data access as repository
interfaces, see the [declarative JDBC examples](../../declarative/data-jdbc).

## Build

Run the following command from this directory to build every database example:

```shell
mvn package
```

To build the examples without running the tests, use:

```shell
mvn package -DskipTests
```

## Run

Choose the database you want to use. Its README explains how to start the database, initialize the schema, and run the
application.

- [MySQL](mysql)
- [Oracle Database](oracle)
- [PostgreSQL](postgres)

## Test

The tests use Testcontainers and require Docker. Run all tests from this directory:

```shell
mvn test
```

Each test starts its database container and initializes it with the schema for that example. JUnit skips the tests when
Docker is unavailable.

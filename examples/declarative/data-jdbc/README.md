# Helidon Data JDBC Declarative Examples

These examples show how to use Helidon Data JDBC repository interfaces. Helidon generates the repository
implementations during the build.

Each directory contains the same Pokémon application for a different database. To work directly with `JdbcClient`, see
the [imperative JDBC examples](../../imperative/data-jdbc).

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

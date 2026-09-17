# Helidon Data JDBC Imperative with Oracle Database

This example demonstrates a Java SE imperative application that uses Helidon Data JDBC, Helidon WebServer,
an Oracle Universal Connection Pool data source, and Oracle Database. The application creates a standalone
`JdbcClient` backed by the configured data source and uses it directly to execute SQL operations.

To define data access with repository interfaces, see the
[declarative Oracle Database example](../../../declarative/data-jdbc/oracle).

Run the commands from `examples/imperative/data-jdbc/oracle`.

> **Note**
> Helidon Data JDBC is incubating, and this example uses some preview APIs. The affected types suppress the
> applicable warnings locally with `@SuppressWarnings` and the corresponding Helidon API constants
> `Api.SUPPRESS_INCUBATING` or `Api.SUPPRESS_PREVIEW`.

## Build

Build the application and run its tests:

```shell
mvn package
```

To build the application without running the tests, use:

```shell
mvn package -DskipTests
```

## Run

Start an Oracle Database container for the sample application:

```shell
docker run --name oracle \
       -p 1521:1521 \
       -e ORACLE_PWD='oracle123' \
       -v "$PWD/etc/setup-user.sql:/opt/oracle/scripts/startup/01-setup-user.sql:ro" \
       -v "$PWD/etc:/opt/helidon:ro" \
       -d container-registry.oracle.com/database/free:23.26.3.0-lite
```

At startup, Oracle Database runs `etc/setup-user.sql`, which is mounted in the container as
`/opt/oracle/scripts/startup/01-setup-user.sql`. The script creates the `pokemon` user, grants the required
permissions, and creates the `POKEMON_DATA` tablespace in `FREEPDB1`. The script can run again without
recreating the user. The `23.26.3.0-lite` image does not provide a general-purpose `USERS` tablespace, so
the example creates its own.

The `etc` directory is mounted at `/opt/helidon`, which makes the schema files available to SQL*Plus in
the container.

Follow the container logs until Oracle Database reports that it is ready and the user setup script has completed:

```shell
docker logs -f oracle
```

The schema command drops and recreates the `POKEMON` and `TYPE` tables in the `pokemon` schema. It does not modify
other schemas.

```shell
docker exec oracle \
       sqlplus -s pokemon/changeit@//localhost:1521/FREEPDB1 \
       @/opt/helidon/run-schema.sql
```

`run-schema.sql` configures SQL*Plus to exit on the first SQL error and then runs `schema.sql`.

Start the application after the schema is ready:

```shell
java '-Dhelidon.serialFilter.pattern=oracle.sql.converter.*' \
     -jar target/helidon-examples-imperative-data-jdbc-oracle.jar
```

Oracle JDBC reads character set conversion data through Java serialization. The system property permits the required
Oracle classes while Helidon continues to reject other deserialization targets. The application listens on
`http://localhost:8080`.

These credentials are for local development only. Do not use them in production.

The first database request can take longer while UCP creates its initial connections.

## Test the Application

The application provides its Pokémon API at `http://localhost:8080/pokemon`.

**List all Pokémon:**

```shell
curl http://localhost:8080/pokemon/all
```

**List all Pokémon of the Normal type:**

```shell
curl http://localhost:8080/pokemon/type/Normal
```

**Retrieve a Pokémon by name:**

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

**Insert a Pokémon:**

```shell
curl -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

Run the automated tests with the following command:

```shell
mvn test
```

Testcontainers starts Oracle Database, provisions the `pokemon` user, and initializes it with `etc/schema.sql`. The
tests do not require the manually started container. JUnit skips them when Docker is unavailable.

## Client Construction and Transaction Behavior

`Main` creates the client from the configured UCP data source name:

```java
JdbcClient jdbcClient = JdbcClient.create(builder -> builder.dataSourceName("example"));
```

The client remains standalone, and the UCP data source remains externally managed. Each terminal operation owns its
connection and does not participate in `Tx.transaction`. The type lookup and mutation in each insert or update flow
therefore run as separate JDBC operations. This example does not use `data.clients.jdbc`. For a client managed by
Service Registry that participates in local transactions, see the [PostgreSQL example](../postgres).

## Clean Up

When you have finished with the example, stop the database container:

```shell
docker stop oracle
```

Then remove the database container:

```shell
docker rm -f oracle
```

# Helidon Data JDBC Imperative with MySQL

This example demonstrates a Java SE imperative application that uses Helidon Data JDBC, Helidon WebServer,
MySQL Connector/J, and a MySQL database. The application creates a standalone `JdbcClient` and uses it directly
to execute SQL operations.

To define data access with repository interfaces, see the
[declarative MySQL example](../../../declarative/data-jdbc/mysql).

Run the commands from `examples/imperative/data-jdbc/mysql`.

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

Start a MySQL container for the sample application:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.3
```

Follow the container logs until MySQL reports that it is ready for connections:

```shell
docker logs -f mysql
```

The schema command drops and recreates the `POKEMON` and `TYPE` tables in the `pokemons` database. It does not modify
other databases.

```shell
docker exec -i mysql \
       sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --user=user pokemons' \
       < etc/schema.sql
```

Start the application after the schema is ready:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-mysql.jar
```

The application listens on `http://localhost:8080`.

These credentials are for local development only. Do not use them in production.

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

Testcontainers starts MySQL and initializes it with `etc/schema.sql`. The tests do not require the manually started
container. JUnit skips them when Docker is unavailable.

## Client Construction and Transaction Behavior

`Main` reads the values under `app.database` and uses the public builder to create the client:

```java
JdbcClient jdbcClient = JdbcClient.builder()
        .connection(connection -> connection
                .url(url)
                .username(username)
                .password(password))
        .build();
```

MySQL Connector/J registers itself and is selected from the configured JDBC URL. This client is standalone and does
not use `data.clients.jdbc`. Each terminal operation owns its connection and does not participate in `Tx.transaction`.
The type lookup and mutation in each insert or update flow therefore run as separate JDBC operations. For a client
managed by Service Registry that participates in local transactions, see the [PostgreSQL example](../postgres).

## Clean Up

When you have finished with the example, stop the database container:

```shell
docker stop mysql
```

Then remove the database container:

```shell
docker rm -f mysql
```

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

All paths in the following table are relative to `http://localhost:8080`.

By default, `curl` displays the response body. Add `-i` to include the HTTP status and response headers. This is
especially useful for responses with an empty body, such as `404 Not Found`.

| Method | Path | Behavior |
| --- | --- | --- |
| `GET` | `/pokemon/all` | Lists all Pokémon ordered by name |
| `GET` | `/pokemon/type/{name}` | Lists Pokémon having the requested type; returns an empty array when none match |
| `GET` | `/pokemon/search/{term}` | Lists Pokémon whose name or type matches the term, binding the same value to both positional SQL parameters |
| `GET` | `/pokemon/get/{name}` | Returns the Pokémon having the requested name, or `404` when it does not exist |
| `GET` | `/pokemon/search/{type}/{name}` | Returns the Pokémon matching both values using positional SQL parameters, or `404` when it does not exist |
| `GET` | `/pokemon/count` | Returns the number of Pokémon rows |
| `POST` | `/pokemon` | Inserts a Pokémon and returns it with its database-generated identifier |
| `PUT` | `/pokemon/{id}` | Updates a Pokémon and returns its updated representation, or `404` when the identifier does not exist |
| `DELETE` | `/pokemon/{id}` | Deletes a Pokémon and reports the number of affected rows |

For example, list all Pokémon:

```shell
curl http://localhost:8080/pokemon/all
```

To insert, update, and delete a Pokémon, run the following requests in order:

**Insert a Pokémon:**

```shell
curl -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

**Update the inserted Pokémon:**

The `POST` response includes the generated identifier. It is `20` by default after schema initialization. If Pokémon
have been inserted previously, replace `20` in the following request with the identifier returned by `POST`.

```shell
curl -X PUT \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmeleon","type":"Fire"}' \
     http://localhost:8080/pokemon/20
```

**Delete the updated Pokémon:**

Use the same identifier for the `DELETE` request, replacing `20` if necessary.

```shell
curl -X DELETE \
     http://localhost:8080/pokemon/20
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

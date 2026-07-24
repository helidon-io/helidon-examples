# Helidon Data JDBC Declarative Pokemon Example

This example uses Helidon Data to generate pure JDBC implementations of two declarative repository interfaces:

- `PokemonRepository`
- `TypeRepository`

Repository methods declare SQL with `@Jdbc.Statement` and select query or update execution with `@Jdbc.Execution`.

The sample validates:

- generated JDBC repository implementations for list, optional, insert, and delete operations;
- named SQL parameter binding;
- generated mapping of a flat `Type` record;
- marker form `@Jdbc.RowMapper` selection by the exact `JdbcClient.RowMapper<Pokemon>` service contract;
- Service Registry injection of the matching mapper into the generated repository;
- mapping one joined database row to a `Pokemon` containing a nested `Type`;
- generated-key retrieval for inserts and update-count handling for deletes; and
- one local JDBC transaction that looks up a type and inserts a Pokemon.

The example uses MySQL and HikariCP. The credentials below are intended only for local development.

## Start MySQL

Run this command from the `examples/declarative/data-jdbc` directory.

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.1
```

Wait until `docker logs mysql` reports that the server is ready for connections before starting the application.

## Build and Run

Build the application from this directory:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc.jar
```

The application listens on `http://localhost:8080/pokemon`.

## Try the Application

List all Pokemon:

```shell
curl http://localhost:8080/pokemon/all
```

List Pokemon having the `Normal` type:

```shell
curl http://localhost:8080/pokemon/type/Normal
```

Retrieve `Meowth` by name:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Insert a Pokemon and return its generated identifier:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

The schema starts generated Pokemon identifiers at `20`, so the first insert into a fresh database returns that ID.
Delete it with:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

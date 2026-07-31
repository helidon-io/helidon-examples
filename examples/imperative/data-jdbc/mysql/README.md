# Helidon Data JDBC Imperative using MySQL Database

This example demonstrates direct, imperative use of the Helidon Data JDBC provider with MySQL. It is the imperative
counterpart of `examples/declarative/data-jdbc` and uses the same Pokemon schema, SQL statements, database method names,
row mappers, HTTP paths, and JSON representation.

The configuration defines a HikariCP datasource and a JDBC persistence unit. The persistence unit publishes a
`JdbcClient` through the Service Registry. `Main` obtains that client and passes it to `PokemonService`, which owns the
imperative HTTP handlers and JDBC operations.

The sample demonstrates:

- list, optional, scalar, insert, and delete JDBC operations;
- positional parameter binding, including binding one value to multiple positions;
- explicit `NULL` binding for nullable strings;
- mapping joined rows to a `Pokemon` containing a nested `Type`;
- selecting either the normal or alternate row mapper;
- retrieving a MySQL-generated identifier; and
- looking up a type and inserting a Pokemon in one local JDBC transaction.

The credentials below are intended only for local development.

## Start MySQL

Run the following command:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.1
```

Wait until MySQL reports that it is ready for connections:

```shell
docker logs -f mysql
```

Press `Ctrl+C` to stop following the log; the container continues running in the background.

The datasource settings are in `src/main/resources/application.yaml`. If MySQL runs on a different host or port, update
`data.url`. Update the datasource username and password there if you use different credentials.

## Build and Run

From `examples/imperative/data-jdbc/mysql`, build the application:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-mysql.jar
```

At startup, the JDBC provider runs `drop.sql` followed by `init.sql`, recreating and populating the example schema. The
application listens on `http://localhost:8080/pokemon`.

## Invoke the Endpoints

List all Pokemon:

```shell
curl http://localhost:8080/pokemon/all
```

List Pokemon having the `Normal` type:

```shell
curl http://localhost:8080/pokemon/type/Normal
```

Search for a Pokemon whose name or type is `Normal`:

```shell
curl http://localhost:8080/pokemon/search/Normal
```

The same `term` value is bound to both positional parameters.

Retrieve `Meowth` by name:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Retrieve `Meowth` with the alternate row mapper:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The alternate mapper returns the recognizable name `"LOW-WEIGHT EXPLICIT: Meowth"`.

Retrieve `Meowth` by type and name:

```shell
curl http://localhost:8080/pokemon/search/Normal/Meowth
```

Count all Pokemon:

```shell
curl http://localhost:8080/pokemon/count
```

Insert a Pokemon and return its generated identifier:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

The type lookup and insert run in one local JDBC transaction. The schema starts generated identifiers at `20`, so the
first insert into a freshly initialized database returns that ID.

Delete the inserted Pokemon:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

## Stop MySQL

```shell
docker stop mysql
docker rm mysql
```

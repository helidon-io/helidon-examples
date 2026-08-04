# Helidon Data JDBC Imperative using H2 Database

This example runs the imperative Pokemon application shared by all database variants from the sibling `common`
directory, using an embedded, in-memory H2 database. It demonstrates direct use of `JdbcClient` for list, optional,
scalar, generated-key, update, and transactional operations.

The configuration creates an H2 datasource through HikariCP and publishes a JDBC persistence-unit client through the
Service Registry. No external database installation or container is required.

## Build and Run

From `examples/imperative/data-jdbc/h2`, build the application:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-h2.jar
```

At startup, the JDBC provider runs `drop.sql` followed by `init.sql`. The schema and sample data live only for the
duration of the process. The application listens on `http://localhost:8080/pokemon`.

## Invoke the Endpoints

List all Pokemon:

```shell
curl http://localhost:8080/pokemon/all
```

List Pokemon having the `Normal` type:

```shell
curl http://localhost:8080/pokemon/type/Normal
```

Search by Pokemon name or type:

```shell
curl http://localhost:8080/pokemon/search/Normal
```

Retrieve `Meowth` by name:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Retrieve `Meowth` with the alternate row mapper:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

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

The H2 identity starts at `20`, so the first insert returns that identifier. Delete it with:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

## Endpoint Validation

With the application running, validate every endpoint against H2:

```shell
mvn test -Pendpoint-validation
```

Use `-Dbase-url=http://host:port` if the application does not use `http://localhost:8080`.

# Helidon Data JDBC Imperative using PostgreSQL

This example demonstrates imperative use of the Helidon Data JDBC provider with PostgreSQL. It is the imperative
counterpart of `examples/declarative/data-jdbc` and uses the same Pokemon schema, SQL statements, database method names,
row mappers, HTTP paths, and JSON representation.

The configuration defines a HikariCP data source named `example` and a registry managed JDBC client named `pokemon`.
The Service Registry injects that client into `PokemonService`. `Main` also creates a standalone setup client from an
immutable `JdbcClientConfig`. Both clients use the configured data source and PostgreSQL JDBC driver.

The sample demonstrates:

- list, optional, scalar, insert, and delete JDBC operations;
- positional parameter binding, including binding one value to multiple positions;
- mapping joined rows to a `Pokemon` containing a nested `Type`;
- selecting either the normal or alternate row mapper;
- retrieving a database-generated identifier; and
- looking up a type and inserting a Pokemon in one local JDBC transaction.

## Client Construction

The application client is configured under `data.clients.jdbc`:

```yaml
data:
  clients:
    jdbc:
      - name: "pokemon"
        data-source: "example"
```

`PokemonService` selects both the JDBC provider and the named client:

```java
@Service.Inject
PokemonService(@Data.ProviderType("jdbc")
               @Service.Named("pokemon")
               JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
}
```

`Main` separately prepares an immutable configuration for the standalone schema setup client:

```java
JdbcClientConfig setupClientConfig = JdbcClient.builder()
        .dataSource("example")
        .buildPrototype();
JdbcClient setupClient = JdbcClient.create(setupClientConfig);
```

The setup client is not published in the Service Registry and owns a connection for each terminal operation. The named
application client participates in `Tx.transaction`.

The credentials below are intended only for local development.

## Build the PostgreSQL Image

Like the DbClient PostgreSQL example, the local image installs PostgreSQL Server on Oracle Linux 9 and adds a
standalone initialization entrypoint:

```shell
docker build etc/docker -t helidon-postgres
```

## Start PostgreSQL

Run the following command:

```shell
docker run --name postgres \
       -p 5432:5432 \
       -e POSTGRES_DB='pokemons' \
       -e POSTGRES_USER='user' \
       -e POSTGRES_PASSWORD='pgsql123' \
       -d helidon-postgres
```

Before starting the application, ensure that the PostgreSQL container is running and ready to accept connections.

The password used in this example is intended only for local development. Use a strong, unique password and update both
the Docker command and `src/main/resources/application.yaml` with the new value. For production deployments, provide
credentials through external configuration or a secrets manager instead of storing them in source control.

The datasource settings are in `src/main/resources/application.yaml`. If PostgreSQL runs on a different host or port,
update `data.url`. Update the datasource username and password there if you use different credentials.

The JDBC URL disables quoting of `RETURNING` identifiers so PostgreSQL folds the shared generated-key column name `ID`
in the same way as the unquoted schema and application SQL.

## Build and Run

From `examples/imperative/data-jdbc/postgres`, build the application:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-postgres.jar
```

Before HTTP routing starts, the application owned `SchemaInitializer` recreates and populates the sample schema through
the standalone setup client. The registry managed `pokemon` client handles application operations. The application
listens on `http://localhost:8080/pokemon`.

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

Insert a Pokemon and return a JSON object containing its generated identifier, name, and type:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

The registry managed client performs the type lookup and insert in one local JDBC transaction. The schema starts
generated identifiers at `20`, so the JSON object returned by the first insert into a freshly initialized database
contains that ID.

Delete the inserted Pokemon:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

## Stop PostgreSQL

To stop the PostgreSQL container:

```shell
docker stop postgres
```

To delete the stopped container:

```shell
docker rm postgres
```

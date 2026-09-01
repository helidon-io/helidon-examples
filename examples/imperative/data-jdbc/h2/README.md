# Helidon Data JDBC Imperative using H2 Database

This example runs an imperative Pokemon application using an embedded, in-memory H2 database. It demonstrates direct
use of `JdbcClient` for list, optional, scalar, generated-key, update, and transactional operations.

The configuration creates an H2 data source through HikariCP. `Main` builds a named immutable `JdbcClientConfig` from
that existing data source and contributes the client configuration with `Services.set`. The Service Registry then
publishes the named JDBC Client injected into `PokemonService`.

`Main` also constructs a standalone client from the existing `DataSource` and passes it to `SchemaInitializer`. This
keeps schema setup outside the managed transaction path while demonstrating both construction modes. No external
database installation or container is required.

## Client Construction

The programmatic registry configuration must be installed before the first lookup of `JdbcClientConfig` or
`JdbcClient`:

```java
JdbcClientConfig jdbcClientConfig = JdbcClient.builder()
        .name("pokemon")
        .dataSource(dataSource)
        .buildPrototype();
Services.set(JdbcClientConfig.class, jdbcClientConfig);
```

`PokemonService` is still an imperative service. It receives the named JDBC client from the Service Registry and uses
the `JdbcClient` API directly:

```java
@Service.Inject
PokemonService(@Data.ProviderType("jdbc")
               @Service.Named("pokemon")
               JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
}
```

`@Data.ProviderType("jdbc")` selects the JDBC provider contract while `@Service.Named("pokemon")` selects the configured
client within that provider.

The standalone setup client uses the same existing data source without being published:

```java
JdbcClient setupClient = JdbcClient.builder()
        .dataSource(dataSource)
        .build();
```

## Build and Run

From `examples/imperative/data-jdbc/h2`, build the application:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-h2.jar
```

Before HTTP routing starts, the application owned `SchemaInitializer` recreates and populates the sample schema through
the standalone setup client. The schema and sample data live only for the duration of the process. The named registry
managed client handles application operations and participates in `Tx.transaction`. The application listens on
`http://localhost:8080/pokemon`.

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

Insert a Pokemon and return a JSON object containing its generated identifier, name, and type:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

The H2 identity starts at `20`, so the JSON object returned by the first insert contains that identifier. Delete it
with:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

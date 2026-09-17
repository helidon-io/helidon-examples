# Helidon Data JDBC Imperative with PostgreSQL

This example executes PostgreSQL statements directly with Helidon Data `JdbcClient`. Application code creates each
statement, binds positional parameters, selects a mapper, and invokes a terminal operation. To define data access as
repository interfaces, see the [declarative PostgreSQL example](../../../declarative/data-jdbc/postgres).

`PokemonService` handles routing, validation, and responses. `PokemonStore` handles SQL, row mapping, generated keys,
and transactions.

Run the commands from `examples/imperative/data-jdbc/postgres`.

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

Build the PostgreSQL image used by this example:

```shell
docker build etc/docker -t helidon-postgres
```

Start a PostgreSQL container for the sample application:

```shell
docker run --name postgres \
       -p 5432:5432 \
       -e POSTGRES_DB='pokemons' \
       -e POSTGRES_USER='user' \
       -e POSTGRES_PASSWORD='pgsql123' \
       -d helidon-postgres
```

The image installs PostgreSQL Server on Oracle Linux 9 and uses the standalone entry point supplied by this example.

Follow the container logs until PostgreSQL reports that it is ready to accept connections:

```shell
docker logs -f postgres
```

The schema command drops and recreates the `POKEMON` and `TYPE` tables in the `pokemons` database. It does not modify
other databases.

```shell
docker exec -i postgres \
       psql --username=user --dbname=pokemons \
       < etc/schema.sql
```

The default URL in `application.yaml` includes `quoteReturningIdentifiers=false`. Keep this setting when using the
sample schema so PostgreSQL returns the generated `ID` column with the expected name.

Start the application after the schema is ready:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-postgres.jar
```

The application uses the `pokemon` client managed by Service Registry and listens on `http://localhost:8080`.

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

Testcontainers builds the PostgreSQL image, creates the `pokemons` database, and initializes it with `etc/schema.sql`.
The tests do not require the manually started container. JUnit skips them when Docker is unavailable.

## Client Configuration and Transactions

The client managed by Service Registry is configured under `data.clients.jdbc` and uses the HikariCP data source named
`example`:

```yaml
data:
  clients:
    jdbc:
      - name: "pokemon"
        data-source: "example"
```

`PokemonStore` selects the JDBC provider and the named client at its injection point:

```java
@Service.Inject
PokemonStore(@Data.ProviderType("jdbc")
             @Service.Named(Main.POKEMON_CLIENT)
             JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
}
```

Unlike a standalone client, the client managed by Service Registry participates in `Tx.transaction`. `PokemonStore`
wraps each type lookup and its corresponding insert or update in one local transaction. The tests verify that a
deliberate failure rolls back an insert and that the client recovers after a unique constraint violation.

## Clean Up

When you have finished with the example, stop the database container:

```shell
docker stop postgres
```

Then remove the database container:

```shell
docker rm -f postgres
```

# Helidon Data JDBC Declarative with PostgreSQL

This example demonstrates a Java SE declarative application that uses Helidon Data JDBC, Helidon WebServer,
the PostgreSQL JDBC driver, and a PostgreSQL database. Helidon generates the repository implementations
during the build.

> **Note**
> Helidon Data JDBC is incubating, and this example uses some preview APIs. The affected types suppress the
> applicable warnings locally with `@SuppressWarnings` and the corresponding Helidon API constants
> `Api.SUPPRESS_INCUBATING` or `Api.SUPPRESS_PREVIEW`.

The example defines two repository interfaces:

- `PokemonRepository`
- `PokemonTypeRepository`

To work directly with `JdbcClient`, see the
[imperative PostgreSQL example](../../../imperative/data-jdbc/postgres).

Run the commands from `examples/declarative/data-jdbc/postgres`.

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
java -jar target/helidon-examples-declarative-data-jdbc-postgres.jar
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
| `GET` | `/pokemon/search/{term}` | Lists Pokémon whose name or type matches the term, demonstrating reuse of a named SQL parameter |
| `GET` | `/pokemon/get/{name}` | Returns the Pokémon having the requested name, or `404` when it does not exist |
| `GET` | `/pokemon/explicit-mapper/{name}` | Returns the Pokémon using the explicitly selected row mapper, or `404` when it does not exist |
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

Testcontainers creates, initializes, and removes a temporary PostgreSQL database. The tests do not require the
manually started container. JUnit skips them when Docker is unavailable.

## Advanced Mapper Selection

The regular repository queries use `@Jdbc.RowMapper` without naming a mapper class. Two services implement
`JdbcClient.RowMapper<Pokemon>`:

- [`PokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/postgres/model/PokemonRowMapper.java) has a
  service weight of `Weighted.DEFAULT_WEIGHT + 10` and returns the Pokémon name unchanged.
- [`ExplicitPokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/postgres/model/ExplicitPokemonRowMapper.java)
  has a service weight of `Weighted.DEFAULT_WEIGHT - 10`.

When no mapper class is specified, Helidon selects `PokemonRowMapper` because it has the higher service weight.
`ExplicitPokemonRowMapper` prefixes the Pokémon name, making it clear that the explicitly selected mapper handled the
row. The explicit repository method uses `@Jdbc.RowMapper(ExplicitPokemonRowMapper.class)` to select this mapper.

Call the endpoint that uses the explicit mapper:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

## Clean Up

When you have finished with the example, stop the database container:

```shell
docker stop postgres
```

Then remove the database container:

```shell
docker rm -f postgres
```

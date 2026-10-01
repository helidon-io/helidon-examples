# Helidon Data JDBC Declarative with Oracle Database

This example demonstrates a Java SE declarative application that uses Helidon Data JDBC, Helidon WebServer,
a HikariCP data source, and Oracle Database. Helidon generates the repository implementations during the build.

> **Note**
> Helidon Data JDBC is incubating, and this example uses some preview APIs. The affected types suppress the
> applicable warnings locally with `@SuppressWarnings` and the corresponding Helidon API constants
> `Api.SUPPRESS_INCUBATING` or `Api.SUPPRESS_PREVIEW`.

The example defines two repository interfaces:

- `PokemonRepository`
- `PokemonTypeRepository`

To work directly with `JdbcClient`, see the
[imperative Oracle Database example](../../../imperative/data-jdbc/oracle).

Run the commands from `examples/declarative/data-jdbc/oracle`.

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
     -jar target/helidon-examples-declarative-data-jdbc-oracle.jar
```

Oracle JDBC reads character set conversion data through Java serialization. The system property permits the required
Oracle classes while Helidon continues to reject other deserialization targets. The application listens on
`http://localhost:8080`.

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

Testcontainers starts Oracle Database, provisions the `pokemon` user, and initializes it with `etc/schema.sql`. The
tests do not require the manually started container. JUnit skips them when Docker is unavailable.

## Advanced Mapper Selection

The regular repository queries use `@Jdbc.RowMapper` without naming a mapper class. Two services implement
`JdbcClient.RowMapper<Pokemon>`:

- [`PokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/oracle/model/PokemonRowMapper.java) has a
  service weight of `Weighted.DEFAULT_WEIGHT + 10` and returns the Pokémon name unchanged.
- [`ExplicitPokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/oracle/model/ExplicitPokemonRowMapper.java)
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
docker stop oracle
```

Then remove the database container:

```shell
docker rm -f oracle
```

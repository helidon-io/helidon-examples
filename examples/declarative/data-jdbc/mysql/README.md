# Helidon Data JDBC Declarative with MySQL

This example uses Helidon Data JDBC repository interfaces with MySQL. Helidon generates the repository
implementations during the build. To work directly with `JdbcClient`, see the
[imperative MySQL example](../../../imperative/data-jdbc/mysql).

Run the commands from `examples/declarative/data-jdbc/mysql`.

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
java -jar target/helidon-examples-declarative-data-jdbc-mysql.jar
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

## Advanced Mapper Selection

The regular repository queries use `@Jdbc.RowMapper` without naming a mapper class. Two services implement
`JdbcClient.RowMapper<Pokemon>`:

- [`PokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/PokemonRowMapper.java) has a
  service weight of `Weighted.DEFAULT_WEIGHT + 10` and returns the Pokémon name unchanged.
- [`ExplicitPokemonRowMapper`](src/main/java/io/helidon/examples/declarative/data/jdbc/model/ExplicitPokemonRowMapper.java)
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
docker stop mysql
```

Then remove the database container:

```shell
docker rm -f mysql
```

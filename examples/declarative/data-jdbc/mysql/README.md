# Helidon Data JDBC Declarative with MySQL

This example shows how to use Helidon Data declarative repositories with MySQL. Helidon generates JDBC-backed
implementations of two repository interfaces:

- `PokemonRepository`
- `TypeRepository`

Repository methods declare SQL with `@Jdbc.Statement`. Helidon can infer query or update execution for most result
shapes. Methods with ambiguous primitive `int` or `long` results use `@Jdbc.Execution` to select the operation
explicitly.

Use this example to explore generated repository implementations. If you prefer to construct statements and call
`JdbcClient` directly, see the [imperative MySQL example](../../../imperative/data-jdbc/mysql).

## What the Example Demonstrates

The repositories cover:

- generated JDBC repository implementations for list, optional, insert, update, and delete operations
- named SQL parameter binding, repeated named markers, and positional binding in repository parameter declaration order
- generated typed-null binding for reference parameters
- generated mapping of a flat `Type` record
- marker form `@Jdbc.RowMapper` selection by the exact `JdbcClient.RowMapper<Pokemon>` service contract
- Service Registry selection of the matching mapper with the highest `@Weight`
- class-valued `@Jdbc.RowMapper(ExplicitPokemonRowMapper.class)` selection independently of service weight
- generation of an inherited method declared by a parent repository contract
- mapping one joined database row to a `Pokemon` containing a nested `Type`
- staged generated-key retrieval for inserts and update-count handling for updates and deletes
- explicit query selection for a primitive `long` count result
- local JDBC transactions that combine each type lookup with its insert or update

`PokemonRepository` extends the ordinary `PokemonLookup` interface. `PokemonLookup` declares
`findByName(String name)` and its JDBC annotations, and the generated `PokemonRepository` implementation includes that
inherited method.

## Database Configuration

The application uses MySQL Connector/J and a HikariCP data source. Its settings are in
`src/main/resources/application.yaml`. By default, the application connects to the `pokemons` database at
`localhost:3306` with username `user` and password `changeit`. If you use another database, update `data.url` and the
data source credentials before starting the application.

The configuration registers the default JDBC client and backs it with the HikariCP data source named `example`.
The application does not create or migrate the schema, so initialize the database before starting the application.

The default credentials are intended only for this local demo.

You can use an existing MySQL database or start the optional local container described below. With an existing database,
create the configured database and user, then run `etc/schema.sql` as that user.

## Optional Local MySQL Container

If you want to use a local MySQL container for the demo, run the following command from the
`examples/declarative/data-jdbc/mysql` directory:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.1
```

Follow the startup log and wait for MySQL to accept connections:

```shell
docker logs -f mysql
```

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database,
> including all their data, before recreating and populating them. It does not modify tables in other databases.

When using the optional container, run the schema script as the demo user:

```shell
docker exec -i mysql \
       sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --user=user pokemons' \
       < etc/schema.sql
```

The command reads the password from the container's `MYSQL_PASSWORD` environment variable and sends `etc/schema.sql` to
the MySQL client. The script recreates the sample tables and their foreign key, inserts the Pokemon types and Pokemon,
and commits the sample data.

Use your normal provisioning and credential-management practices for any environment beyond this local demo.

## Build and Run

Use JDK 26 and Maven 3.8.0 or newer.

From this directory, build the application and run its tests:

```shell
mvn package
```

To build the application without running tests:

```shell
mvn package -DskipTests
```

Start the packaged application:

```shell
java -jar target/helidon-examples-declarative-data-jdbc-mysql.jar
```

The test suite uses Testcontainers to start
`container-registry.oracle.com/mysql/community-server:9.7.1`, the same image shown above. It creates the `pokemons`
database, initializes it with the same `etc/schema.sql`, and exercises the documented query and mutation endpoints
through the MySQL connection. Testcontainers manages this database, so the optional local container is not needed for
tests. When Docker is unavailable, JUnit skips the container-backed test class.

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

Search for a Pokemon name or type with one repeated named parameter:

```shell
curl http://localhost:8080/pokemon/search/Normal
```

The repository uses `:term` twice. Generated code binds the same argument to both JDBC positions in marker encounter
order.

Retrieve `Meowth` by name:

```shell
curl http://localhost:8080/pokemon/get/Meowth
```

Two services match the marker method's exact `JdbcClient.RowMapper<Pokemon>` contract:

- `ExplicitPokemonRowMapper` has weight `Weighted.DEFAULT_WEIGHT - 10`
- `PokemonRowMapper` has weight `Weighted.DEFAULT_WEIGHT + 10`

`ExplicitPokemonRowMapper` comes first alphabetically. The marker lookup nevertheless selects `PokemonRowMapper`
because Service Registry evaluates higher weight before service type name. The ordinary result therefore retains the
database name `"Meowth"`.

Select the lower-weight mapper explicitly:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The class-valued mapper annotation bypasses marker lookup and returns `"LOW-WEIGHT EXPLICIT: Meowth"`, which makes the
selected mapper visible in the response.

Retrieve `Meowth` by type and name:

```shell
curl http://localhost:8080/pokemon/search/Normal/Meowth
```

The search endpoint uses two positional `?` markers. The repository binds the `typeName` argument to position `1` and
the `name` argument to position `2`.

Count all Pokemon:

```shell
curl http://localhost:8080/pokemon/count
```

The count method uses `@Jdbc.Execution(QUERY)` because primitive `long` could otherwise mean either a scalar query or an
update count. The list method omits `@Jdbc.Execution` to demonstrate AUTO inference from its `List<Pokemon>` result.

Insert a Pokemon. The response is a JSON object containing the generated identifier, name, and type:

```shell
curl -i -X POST \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmander","type":"Fire"}' \
     http://localhost:8080/pokemon
```

`@Jdbc.GeneratedKeys("ID")` selects update execution without a separate `@Jdbc.Execution(UPDATE)` annotation. Generated
code adds the `ID` column through the staged generated-key builder before mapping the returned scalar.

The schema starts generated Pokemon identifiers at `20`, so the JSON object returned by the first insert into a fresh
database contains that ID.

Update the inserted Pokemon's name and type. Use the identifier returned by `POST`; the first identifier is `20` in a
freshly initialized database:

```shell
curl -i -X PUT \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmeleon","type":"Fire"}' \
     http://localhost:8080/pokemon/20
```

`PUT /pokemon/{id}` updates the row selected by the path identifier and returns its new JSON representation. The body
supplies the new nonblank `name` and an existing Pokemon `type`; it does not need an `id`. A missing row returns HTTP
`404`.

Delete the updated Pokemon:

```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

## Stop MySQL

To stop the MySQL container:

```shell
docker stop mysql
```

To delete the stopped container:

```shell
docker rm mysql
```

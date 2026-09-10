# Helidon Data JDBC Declarative with Oracle Database

This example shows how to use Helidon Data declarative repositories with Oracle Database. Helidon generates JDBC-backed
implementations of two repository interfaces:

- `PokemonRepository`
- `TypeRepository`

Repository methods declare SQL with `@Jdbc.Statement`. Helidon can infer query or update execution for most result
shapes. Methods with ambiguous primitive `int` or `long` results use `@Jdbc.Execution` to select the operation
explicitly.

Use this example to explore generated repository implementations. If you prefer to construct statements and call
`JdbcClient` directly, see the [imperative Oracle Database example](../../../imperative/data-jdbc/oracle).

## What the Example Demonstrates

The repositories cover:

- generated JDBC repository implementations for list, optional, insert, update, and delete operations
- named SQL parameter binding, repeated named markers, and positional binding in repository parameter declaration order
- generated typed-null binding for reference parameters
- generated mapping of a flat `Type` record
- marker form `@Jdbc.RowMapper` selection by the exact `JdbcClient.RowMapper<Pokemon>` service contract
- Service Registry selection of the matching mapper with the highest `@Weight`
- class-valued `@Jdbc.RowMapper(ExplicitPokemonRowMapper.class)` selection independently of service weight
- mapping one joined database row to a `Pokemon` containing a nested `Type`
- staged generated-key retrieval for inserts and update-count handling for updates and deletes
- explicit query selection for a primitive `long` count result
- local JDBC transactions that combine each type lookup with its insert or update

## Database Configuration

The application uses Oracle Universal Connection Pool (UCP). Its settings are in
`src/main/resources/application.yaml`. By default, the application connects to the `FREEPDB1` pluggable database at
`localhost:1521` with username `pokemon` and password `changeit`. If you use another database, update `data.url` and the
UCP credentials before starting the application.

The configuration registers the default JDBC client and backs it with the UCP data source named `example`.
The application does not create or migrate the schema, so initialize the database before starting the application.

The default credentials are intended only for this local demo. You can use an existing Oracle Database or start the
optional local container described below. With an existing database, provision the configured user and run
`etc/schema.sql` as that user.

## Optional Local Oracle Container

If you want to use a local Oracle Database container for the demo, run the following command from the
`examples/declarative/data-jdbc/oracle` directory:

```shell
docker run --name oracle \
       -p 1521:1521 \
       -e ORACLE_PWD='oracle123' \
       -v "$PWD/etc/setup-user.sql:/opt/oracle/scripts/startup/01-setup-user.sql:ro" \
       -v "$PWD/etc:/opt/helidon:ro" \
       -d container-registry.oracle.com/database/free:23.26.3.0-lite
```

The first volume makes the demo user provisioning script available in the container's startup directory. The
`23.26.3.0-lite` image runs scripts from this directory when the database starts. The provisioning script can run
repeatedly and creates the user only when it does not already exist. The second volume makes `etc/schema.sql` and its
SQL*Plus wrapper available inside the container.

Follow the startup log and wait for the database to complete startup and setup-user.sql run successfully:

```shell
docker logs -f oracle
```

## Demo User Provisioning

As part of the optional container setup, `etc/setup-user.sql` creates the `pokemon` user in `FREEPDB1` if it does not
already exist. Oracle Database uses an administrator connection only while running this provisioning script. The demo
application and its sample schema connect as `pokemon`, not as `SYS` or `SYSTEM`.

The script grants the permissions required by the demo and gives `pokemon` a limited quota on a dedicated
`POKEMON_DATA` tablespace. It creates that tablespace because the `23.26.3.0-lite` image does not include a
general-purpose `USERS` tablespace.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemon` schema, including
> all their data, before recreating and populating them. It does not modify tables in other schemas.

When using the optional container, run the schema script as the demo user:

```shell
docker exec oracle \
       sqlplus -s pokemon/changeit@//localhost:1521/FREEPDB1 \
       @/opt/helidon/run-schema.sql
```

The mounted `run-schema.sql` wrapper stops on the first SQL error and executes `etc/schema.sql`. The schema script
recreates the sample tables and their foreign key, inserts the Pokemon types and Pokemon, and commits the sample data.

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

> **Note**
> Helidon Data JDBC is incubating. This example opts in with
> `-Ahelidon.api.incubating=ignore` in [`pom.xml`](pom.xml). An application can instead scope the opt in to its source
> with `@SuppressWarnings(Api.SUPPRESS_INCUBATING)`.

Start the packaged application:

```shell
java '-Dhelidon.serialFilter.pattern=oracle.sql.converter.*' \
     -jar target/helidon-examples-declarative-data-jdbc-oracle.jar
```

The `helidon.serialFilter.pattern` Java system property is required because Oracle JDBC reads bundled character-set
conversion data using Java serialization. Helidon rejects Java deserialization by default unless the application
explicitly allows the classes involved. The schema setup performed earlier by SQL*Plus runs in a separate process and
does not initialize the Oracle JDBC driver in the application. When the application makes its first JDBC request, the
property allows the Oracle converter package through Helidon's serialization filter while retaining the reject-all
default for other classes.

The test suite uses Testcontainers to start
`container-registry.oracle.com/database/free:23.26.3.0-lite`, the same image shown above. It provisions the `pokemon`
user, initializes the database with the same `etc/schema.sql`, and exercises the documented query and mutation
endpoints through its Oracle UCP data source. Testcontainers manages this database, so the optional local container is
not needed for tests. When Docker is unavailable, JUnit skips the container-backed test class.

The application listens on `http://localhost:8080/pokemon`.

After application startup, UCP initializes the connection pool when the first database connection is requested. The
first invocation of an endpoint that accesses the database may therefore take longer while UCP creates the initial
pooled connections. Subsequent invocations reuse pooled connections.

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

## Stop Oracle Database

To stop the Oracle Database container:

```shell
docker stop oracle
```

To delete the stopped container:

```shell
docker rm oracle
```

# Helidon Data JDBC Declarative using Oracle Database

This example uses Helidon Data to generate pure JDBC implementations of two declarative repository interfaces:

- `PokemonRepository`
- `TypeRepository`

Repository methods declare SQL with `@Jdbc.Statement`. Most result shapes let code generation infer query or update
execution. Primitive `int` and `long` results use `@Jdbc.Execution` when the method shape is ambiguous.

The sample validates:

- generated JDBC repository implementations for list, optional, insert, and delete operations;
- named SQL parameter binding, repeated named markers, and positional binding in repository parameter declaration order;
- generated typed-null binding for reference parameters;
- generated mapping of a flat `Type` record;
- marker form `@Jdbc.RowMapper` selection by the exact `JdbcClient.RowMapper<Pokemon>` service contract;
- Service Registry selection of the matching mapper with the highest `@Weight`;
- class-valued `@Jdbc.RowMapper(PokemonAlternateRowMapper.class)` selection independently of service weight;
- mapping one joined database row to a `Pokemon` containing a nested `Type`;
- staged generated-key retrieval for inserts and update-count handling for deletes;
- explicit query selection for a primitive `long` count result; and
- one local JDBC transaction that looks up a type and inserts a Pokemon.

The example uses Oracle Database Free with Oracle Universal Connection Pool (UCP). Before running the application, you
must run `etc/schema.sql` against the database to create and populate the sample tables. The application does not create
its own schema.

For this demo, the application connects to the `FREEPDB1` pluggable database with username `pokemon` and password
`changeit`. These credentials are part of the example and are not intended for use outside a local demo.

The container and volume instructions below are one convenient way to prepare a database and run the SQL script. They
are provided to make the demo easy to try and are not recommendations for configuring or securing a production
environment. You can instead use an existing Oracle Database and run the scripts with the database tools and account
management process appropriate for that environment.

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
`latest-lite` image runs scripts from this directory when the database starts. The provisioning script can run
repeatedly and creates the user only when it does not already exist. The second volume makes `etc/schema.sql` and its
SQL*Plus wrapper available inside the container.

Before continuing, follow the startup log to make sure that the database has completed startup:

```shell
docker logs -f oracle
```

## Demo User Provisioning

As part of the optional container setup, `etc/setup-user.sql` creates the `pokemon` user in `FREEPDB1` if it does not
already exist. Oracle Database uses an administrator connection only while running this provisioning script. The demo
application and its sample schema connect as `pokemon`, not as `SYS` or `SYSTEM`.

The script grants the permissions required by the demo and gives `pokemon` a limited quota on a dedicated
`POKEMON_DATA` tablespace. It creates that tablespace because the `latest-lite` image does not include a general-purpose
`USERS` tablespace.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemon` schema, including
> all their data, before recreating and populating them. It does not modify tables in other schemas.

Running `etc/schema.sql` is a prerequisite for the demo. When using the optional container setup above, this single
command runs the script as the demo user:

```shell
docker exec oracle \
       sqlplus -s pokemon/changeit@//localhost:1521/FREEPDB1 \
       @/opt/helidon/run-schema.sql
```

The mounted `run-schema.sql` wrapper stops on the first SQL error and executes `etc/schema.sql`. The schema script drops
the sample tables, recreates them and their foreign key, inserts the Pokemon types and sample Pokemon, and commits the
sample data. If you use a different Oracle Database setup, run `etc/schema.sql` there as the user the application will
use before starting the application.

The container, volume mounts, demo credentials, and SQL*Plus commands in this section are conveniences for running the
example. Production database provisioning, credential management, storage, and security policies are outside the scope
of this README.

## Build and Run

Build the application from this directory:

```shell
mvn package
```

This example uses Helidon APIs that are currently marked as preview. The Maven compiler configuration passes
`-Ahelidon.api.preview=ignore` for this module so that the preview API diagnostic does not need to be suppressed in
individual Java files.

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

The Maven test suite runs UCP with H2 in Oracle compatibility mode and uses the same `etc/schema.sql` used by Oracle
Database. The test does not require a running Oracle Database container.

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

- `PokemonAlternateRowMapper` has weight `Weighted.DEFAULT_WEIGHT - 10`;
- `PokemonRowMapper` has weight `Weighted.DEFAULT_WEIGHT + 10`.

`PokemonAlternateRowMapper` comes first alphabetically. The marker lookup nevertheless selects `PokemonRowMapper`
because Service Registry evaluates higher weight before service type name. The ordinary result therefore retains the
database name `"Meowth"`.

Select the lower-weight mapper explicitly:

```shell
curl http://localhost:8080/pokemon/explicit-mapper/Meowth
```

The class-valued mapper annotation ignores marker lookup ordering and returns the recognizable name
`"LOW-WEIGHT EXPLICIT: Meowth"`.

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

Insert a Pokemon and return a JSON object containing its generated identifier, name, and type:

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
Delete it with:

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

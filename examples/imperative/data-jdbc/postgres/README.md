# Helidon Data JDBC Imperative using PostgreSQL

This example demonstrates imperative use of the Helidon Data JDBC provider with PostgreSQL. It is the imperative
counterpart of `examples/declarative/data-jdbc` and uses the same Pokemon schema, SQL statements, database method names,
row mappers, HTTP paths, and JSON representation.

The configuration defines a HikariCP data source named `example` and a registry managed JDBC client named `pokemon`.
The Service Registry injects that client into `PokemonService`. The client uses the configured data source and
PostgreSQL JDBC driver.

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

The named application client participates in `Tx.transaction`.

Before running the application, you must run `etc/schema.sql` against the database to create and populate the sample
tables. The application does not create its own schema.

For this demo, the application connects to the `pokemons` database with username `user` and password `pgsql123`. These
credentials are part of the example and are not intended for use outside a local demo.

The container instructions below are one convenient way to prepare a database and run the SQL script. They are provided
to make the demo easy to try; they are not recommendations for configuring or securing a production environment. You
can instead use an existing PostgreSQL database and run the script with the database tools and account management
process appropriate for that environment.

## Optional Local PostgreSQL Container

Like the DbClient PostgreSQL example, the local image installs PostgreSQL Server on Oracle Linux 9 and adds a
standalone entrypoint. If you want to use this image for the demo, build it from the
`examples/imperative/data-jdbc/postgres` directory:

```shell
docker build etc/docker -t helidon-postgres
```

Then start the container:

```shell
docker run --name postgres \
       -p 5432:5432 \
       -e POSTGRES_DB='pokemons' \
       -e POSTGRES_USER='user' \
       -e POSTGRES_PASSWORD='pgsql123' \
       -d helidon-postgres
```

Before continuing, follow the startup log to make sure that the database has completed startup:

```shell
docker logs -f postgres
```

The datasource settings are in `src/main/resources/application.yaml`. If PostgreSQL runs on a different host or port,
update `data.url`. Update the datasource username and password there if you use different credentials.

The JDBC URL disables quoting of `RETURNING` identifiers so PostgreSQL folds the shared generated-key column name `ID`
in the same way as the unquoted schema and application SQL.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database,
> including all their data, before recreating and populating them. It does not modify tables in other databases.

Running `etc/schema.sql` is a prerequisite for the demo. When using the optional container setup above, this single
command runs the local script as the demo user:

```shell
docker exec -i postgres \
       psql --username=user --dbname=pokemons \
       < etc/schema.sql
```

The command sends `etc/schema.sql` to the PostgreSQL client in the container. The script drops the sample tables,
recreates them and their foreign key, inserts the Pokemon types and sample Pokemon, and commits the sample data. If you
use a different PostgreSQL setup, run `etc/schema.sql` there as the user the application will use before starting the
application.

The container, demo credentials, and PostgreSQL commands in this section are conveniences for running the example.
Production database provisioning, credential management, storage, and security policies are outside the scope of this
README.

## Build and Run

From `examples/imperative/data-jdbc/postgres`, build the application:

```shell
mvn package
```

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-postgres.jar
```

The Maven test suite uses H2 in PostgreSQL compatibility mode and runs the same `etc/schema.sql` used by PostgreSQL. The
test does not require a running PostgreSQL container.

The registry-managed `pokemon` client handles application operations. The application listens on
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

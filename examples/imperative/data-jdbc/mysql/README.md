# Helidon Data JDBC Imperative using MySQL Database

This example demonstrates imperative use of the Helidon Data JDBC provider with MySQL. It is the imperative
counterpart of `examples/declarative/data-jdbc` and uses the same Pokemon schema, SQL statements, database method names,
row mappers, HTTP paths, and JSON representation.

`Main` constructs a standalone `JdbcClient` from the MySQL connection properties in `application.yaml` and passes it to
`PokemonService`, which owns the imperative HTTP handlers and JDBC operations. MySQL Connector/J supplies the JDBC
driver. The application does not publish this client in the Service Registry.

The sample demonstrates:

- list, optional, scalar, insert, update, and delete JDBC operations;
- positional parameter binding, including binding one value to multiple positions;
- mapping joined rows to a `Pokemon` containing a nested `Type`;
- selecting either the normal or alternate row mapper;
- retrieving a MySQL-generated identifier; and
- looking up a type and inserting a Pokemon through separate JDBC operations.

## Client Construction

The example uses the public builder with direct connection properties:

```java
JdbcClient jdbcClient = JdbcClient.builder()
        .connection(connection -> connection
                .url(url)
                .username(username)
                .password(password)
                .jdbcDriverClassName("com.mysql.cj.jdbc.Driver"))
        .build();
```

This directly constructed client is standalone. Each terminal operation owns its connection and does not participate in
`Tx.transaction`.

Before running the application, you must run `etc/schema.sql` against the database to create and populate the sample
tables. The application does not create its own schema.

For this demo, the application connects to the `pokemons` database with username `user` and password `changeit`. These
credentials are part of the example and are not intended for use outside a local demo.

The container instructions below are one convenient way to prepare a database and run the SQL script. They are provided
to make the demo easy to try; they are not recommendations for configuring or securing a production environment. You
can instead use an existing MySQL database and run the script with the database tools and account management process
appropriate for that environment.

## Optional Local MySQL Container

If you want to use a local MySQL container for the demo, run the following command from the
`examples/imperative/data-jdbc/mysql` directory:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d container-registry.oracle.com/mysql/community-server:9.7.1
```

Before continuing, follow the startup log to make sure that the database has completed startup:

```shell
docker logs -f mysql
```

The connection settings are under `app.database` in `src/main/resources/application.yaml`. If MySQL runs on a different
host or port, update the URL. Update the username and password there if you use different credentials.

## Initialize the Sample Schema (Required)

> **Warning:** The following command drops the existing `POKEMON` and `TYPE` tables in the `pokemons` database,
> including all their data, before recreating and populating them. It does not modify tables in other databases.

Running `etc/schema.sql` is a prerequisite for the demo. When using the optional container setup above, this single
command runs the local script as the demo user:

```shell
docker exec -i mysql \
       sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql --user=user pokemons' \
       < etc/schema.sql
```

The command uses the container's `MYSQL_PASSWORD` environment variable without passing the password as a MySQL
command-line argument, and sends `etc/schema.sql` to the MySQL client in the container. The script drops the sample
tables, recreates them and their foreign key, inserts the Pokemon types and sample Pokemon, and commits the sample data.
If you use a different MySQL setup, run `etc/schema.sql` there as the user the application will use before starting the
application.

The container, demo credentials, and MySQL commands in this section are conveniences for running the example.
Production database provisioning, credential management, storage, and security policies are outside the scope of this
README.

## Build and Run

From `examples/imperative/data-jdbc/mysql`, build the application and run its tests:

```shell
mvn package
```

To build the application without running the tests, use:

```shell
mvn package -DskipTests
```

This example uses Helidon APIs that are currently marked as preview. Maven passes
`-Ahelidon.api.preview=ignore` to the compiler, so individual Java files do not need preview-warning suppression
annotations.

Start the packaged application:

```shell
java -jar target/helidon-examples-imperative-data-jdbc-mysql.jar
```

The Maven test suite uses Testcontainers to start
`container-registry.oracle.com/mysql/community-server:9.7.1`, the same image shown above. It creates the `pokemons`
database, initializes it with the same `etc/schema.sql`, and exercises the documented query and mutation endpoints
through its MySQL connection. Testcontainers manages this database, so the optional local container is not needed for
tests. When Docker is unavailable, JUnit skips the container-backed test class.

The application listens on `http://localhost:8080/pokemon`.

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

The type lookup and insert are separate JDBC operations. The schema starts generated identifiers at `20`, so the JSON
object returned by the first insert into a freshly initialized database contains that ID.

Update the inserted Pokemon's name and type, using the identifier returned by `POST` (the first identifier is `20` in a
freshly initialized database):

```shell
curl -i -X PUT \
     -H 'Content-Type: application/json' \
     -d '{"name":"Charmeleon","type":"Fire"}' \
     http://localhost:8080/pokemon/20
```

`PUT /pokemon/{id}` updates the row selected by the path identifier and returns its new JSON representation. The body
supplies the new nonblank `name` and an existing Pokemon `type`; it does not need an `id`. A missing row returns HTTP
`404`. The standalone client performs the type lookup and update as separate JDBC operations.

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

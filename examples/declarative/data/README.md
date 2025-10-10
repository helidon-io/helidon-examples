Helidon Data SE Declarative Example
----

This example demonstrates a Java SE declarative application that utilizes Helidon Data, EclipseLink,
WebServer, Hikari connection pool DataSource and MySQL database.

There are 2 repository interfaces in the example:

- `PokemonRepository`
- `TypeRepository`

> **NOTE:** Database tables are initialized with ID auto increment to supply primary key values
>           by the database. MySQL database default String comparisons are case-insensitive.

## Start the Database

To run the application, a MySQL database is required. You can start the database with the necessary
configuration using the following Docker command:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pokemons' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d mysql
```

### Database Schema and Content

The application's Jakarta Persistence API implementation automatically creates the database schema
using the `resources/init.sql` script. The schema consists of two main entities: `Pokemon` and `Type`.
The initialization script populates the database with a basic set of records.

## Build and Run

1. Build the application using Maven:

```shell
mvn package
```

2. Run the application:

```shell
java -jar target/helidon-examples-declarative-data.jar
```

> **NOTE:** The default username and password from this example should never be used in a production environment!

## Test Example

The application provides `http://localhost:8080/pokemon` endpoint.

**List all pokémons:**
```shell
curl http://localhost:8080/pokemon/all
```

**List all normal type pokémons:**
```shell
curl http://localhost:8080/pokemon/type/Normal
```

**Retrieve a pokémon by name (`Meowth`):**
```shell
curl http://localhost:8080/pokemon/get/Meowth
```

**Insert new pokémon:**
```shell
curl -i -X POST -H 'Content-type: application/json' -d '{"name":"Charmander","type":"Fire"}' http://localhost:8080/pokemon
```

**Delete existing pokémon by ID (`20`):**
```shell
curl -i -X DELETE http://localhost:8080/pokemon/20
```

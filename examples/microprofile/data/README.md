Helidon Data MP Example
----

This example demonstrates a Java MP application that utilizes Helidon Data, WebServer, Hikari
connection pool DataSource and MySQL database.

There are 3 repository interfaces in the example:

- `OwnerRepository`
- `BreedRepository`
- `PetRepository`

All methods in `OwnerRepository` are defined as methods with queries defined by the @Data.Query
annotation. There is no specific limitation on the names of those methods.

All methods in `BreedRepository` and `PetRepository` are defined as methods with queries defined
by the method name. Method names must follow the _Query by Method Name_ grammar.

> **NOTE:** Database tables are initialized with ID auto increment to supply primary key values
>           by the database. MySQL database default String comparisons are case-insensitive.

## Start the Database

To run the application, a MySQL database is required. You can start the database with the necessary
configuration using the following Docker command:

```shell
docker run --name mysql \
       -p 3306:3306 \
       -e MYSQL_DATABASE='pets' \
       -e MYSQL_RANDOM_ROOT_PASSWORD='yes' \
       -e MYSQL_USER='user' \
       -e MYSQL_PASSWORD='changeit' \
       -d mysql
```

### Database Schema and Content

The application's Jakarta Persistence API implementation automatically drops and creates the database
schema using the `resources/drop.sql` and `resources/init.sql` scripts. The schema consists of three
main entities: `Pet`, `Owner` and `Breed`. The initialization script populates the database with a basic
set of records.

## Build and Run

1. Build the application using Maven:

```shell
mvn package
```

2. Run the application:

```shell
java -jar target/helidon-examples-microprofile-data.jar
```

> **NOTE:** The default username and password from this example should never be used in a production environment!

## Test Example

The application provides the following endpoints:
- http://localhost:8080/pet - Pet entity endpoint
- http://localhost:8080/owner - Owner entity endpoint
- http://localhost:8080/breed - Breed entity endpoint

### Pet Endpoint

**Retrieve Pet entity endpoint information:**
```shell
curl http://localhost:8080/pet
```

**List all pets:**
```shell
curl http://localhost:8080/pet/all
```

**List all pets as pages:**
```shell
curl http://localhost:8080/pet/all/0
curl http://localhost:8080/pet/all/1
curl http://localhost:8080/pet/all/2
```

The last page will be empty with the initial set of `Pet` records. Additional records would fill it.

**List all dogs:**
```shell
curl http://localhost:8080/pet/breed/Dog
```

**Retrieve a Pet by name (`Max`):**
```shell
curl http://localhost:8080/pet/get/Max
```

**Insert new pet:**
```shell
curl -i -X POST -H 'Content-type: application/json' -d '{"name":"Ken","weight":3.5,"birth":"2023-05-10","owner":"Betty","breed":"Dog"}' http://localhost:8080/pet
```

**Delete existing pet by ID (`20`):**
```shell
curl -i -X DELETE http://localhost:8080/pet/20
```

### Owner Endpoint

**Retrieve Owner entity endpoint information:**
```shell
curl http://localhost:8080/owner
```

**List all owners:**
```shell
curl http://localhost:8080/owner/all
```

**List all names of owners who own a cat:**
```shell
curl http://localhost:8080/owner/names/Cat
```

**Insert new owner:**
```shell
curl -i -X POST http://localhost:8080/owner/Alice
```

**Delete existing owner by ID (`10`):**
```shell
curl -i -X DELETE http://localhost:8080/owner/10
```

### Breed Endpoint

**Retrieve Breed entity endpoint information:**
```shell
curl http://localhost:8080/breed
```

**List all breeds:**
```shell
curl http://localhost:8080/breed/all
```

**Insert new breed:**
```shell
curl -i -X POST http://localhost:8080/breed/Hamster
```

**Delete existing breed by ID (`10`):**
```shell
curl -i -X DELETE http://localhost:8080/breed/10
```

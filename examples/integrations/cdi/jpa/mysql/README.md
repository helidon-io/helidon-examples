# Helidon Examples Integrations CDI JPA MySQL

This project implements a simple CRUD service using Helidon MP and JPA.

## Build

```shell
mvn package
```

## Run

Start the database:
```shell
docker run -d \
    --name mysql \
    -e MYSQL_DATABASE=db1 \
    -e MYSQL_USER=user \
    -e MYSQL_PASSWORD=mysql123 \
    -p 3306:3306 \
    container-registry.oracle.com/mysql/community-server:latest
```

Or for ARM machines:
```shell
docker run -d \
    --name mysql \
    -e MYSQL_DATABASE=db1 \
    -e MYSQL_USER=user \
    -e MYSQL_PASSWORD=mysql123 \
    -p 3306:3306 \
    container-registry.oracle.com/mysql/community-server:9.4.0-aarch64
```

Or, if the container already exists:
```shell
docker start mysql
```

Start the application:
```shell
java -jar target/helidon-examples-integrations-cdi-jpa-mysql.jar
```

Restart the application:
```shell
java \
  -Djakarta.persistence.schema-generation.database.action=none \
  -jar target/helidon-examples-integrations-cdi-jpa-mysql.jar
```

### Exercise the application

```shell
# create two pokemons
curl -X POST -d '{"name": "Pikachu", "type": "Electric"}' -H "Content-Type: application/json" http://localhost:8080/pokemon
curl -X POST -d '{"name": "Raticate", "type": "Normal"}' -H "Content-Type: application/json" http://localhost:8080/pokemon

# list all pokemons
curl -X GET http://localhost:8080/pokemon

# update
curl -X PUT -d '{"type": "Ice"}' -H "Content-Type: application/json" http://localhost:8080/pokemon/Raticate

# get a pokemon
curl -X GET http://localhost:8080/pokemon/Raticate

# delete a pokemon
curl -X DELETE http://localhost:8080/pokemon/Raticate

# delete all pokemons
curl -X DELETE http://localhost:8080/pokemon

# verify all deleted
curl -X GET http://localhost:8080/pokemon
```

## Stop

```shell
docker stop mysql
```

---

Pokémon, and Pokémon character names are trademarks of Nintendo.

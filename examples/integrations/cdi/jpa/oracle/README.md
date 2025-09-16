# Helidon Examples Integrations CDI JPA Oracle

This project implements a simple CRUD service using Helidon MP and JPA.

## Build

```shell
mvn package
```

## Run

Start the database:
```shell
docker run -d \
  --name oracledb \
  -e ORACLE_PWD=oracle123 \
  -p 1521:1521 \
  container-registry.oracle.com/database/free:latest-lite
```

Or, if the container already exists:
```shell
docker start oracledb
```

Start the application:
```shell
java -jar target/helidon-examples-integrations-cdi-jpa-oracle.jar
```

Restart the application:
```shell
java \
  -Djakarta.persistence.schema-generation.database.action=none \
  -jar target/helidon-examples-integrations-cdi-jpa-oracle.jar
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
docker stop oracledb
```

---

Pokémon, and Pokémon character names are trademarks of Nintendo.

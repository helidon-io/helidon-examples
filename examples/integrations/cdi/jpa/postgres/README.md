# Helidon Examples Integrations CDI JPA Postgres

This project implements a simple CRUD service using Helidon MP and JPA.

## Build

```shell
mvn package
```

Build the Docker image:
```shell
docker build etc/docker -t postgres
```

## Run

Start the database:
```shell
docker run -d \
    --name postgres \
    -e POSTGRES_USER=user \
    -e POSTGRES_PASSWORD=pgsql123 \
    -e POSTGRES_DB=db1 \
    -p 5432:5432 \
    postgres
```

Or, if the container already exists:
```shell
docker start postgres
```

Start the application:
```shell
java -jar target/helidon-examples-integrations-cdi-jpa-postgres.jar
```

Restart the application:
```shell
java \
  -Djakarta.persistence.schema-generation.database.action=none \
  -jar target/helidon-examples-integrations-cdi-jpa-postgres.jar
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
docker stop postgres
```

---

Pokémon, and Pokémon character names are trademarks of Nintendo.

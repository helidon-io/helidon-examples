# Helidon Examples DbClient MongoDB

This project implements a simple CRUD service using Helidon SE and Helidon DbClient.

## Build

```shell
mvn package
```

Build the Docker image:
```shell
docker build etc/docker -t mongodb
```

## Run

Start the database:
```shell
docker run -d \
    --name mongodb \
    -e MONGO_DB=db1 \
    -e MONGO_USER=user \
    -e MONGO_PASSWORD=mongo123 \
    -p 27017:27017 \
    mongodb
```

Or, if the container already exists:
```shell
docker start mongodb
```

Start the application:
```shell
java -jar target/helidon-examples-dbclient-mongodb.jar
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
docker stop mongodb
```

---

Pokémon, and Pokémon character names are trademarks of Nintendo.

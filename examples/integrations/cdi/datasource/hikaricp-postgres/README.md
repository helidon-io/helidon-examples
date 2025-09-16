# Helidon Examples Integrations CDI DataSource Postgres

This project implements a simple REST service using Helidon MP and JDBC.

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
java -jar target/helidon-examples-integrations-cdi-datasource-hikaricp-postgres.jar
```

### Exercise the application

```shell
# list all tables
curl -X GET http://localhost:8080/tables
```

## Stop

```shell
docker stop postgres
```

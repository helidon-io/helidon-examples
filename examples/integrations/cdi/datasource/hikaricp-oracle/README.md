# Helidon Examples Integrations CDI DataSource Oracle

This project implements a simple REST service using Helidon MP and JDBC.

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
java -jar target/helidon-examples-integrations-cdi-datasource-hikaricp-oracle.jar
```

### Exercise the application

```shell
# list all tables
curl -X GET http://localhost:8080/tables
```

## Stop

```shell
docker stop oracledb
```

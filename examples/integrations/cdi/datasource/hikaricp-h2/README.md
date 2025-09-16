# Helidon Examples Integrations CDI DataSource H2

This project implements a simple REST service using Helidon MP and JDBC.

## Build

```shell
mvn package
```

## Run

Start the application:
```shell
java -jar target/helidon-examples-integrations-cdi-datasource-hikaricp-h2.jar
```

### Exercise the application

```shell
# list all tables
curl -X GET http://localhost:8080/tables
```

# Helidon Examples DbClient Health

This project demonstrates how to use DbClient Health.

## Build

```shell
mvn package
```

## Run

Start the application:
```shell
java -jar target/helidon-examples-dbclient-health.jar
```

### Exercise the application

```shell
curl -X GET http://localhost:8080/observe/health
```

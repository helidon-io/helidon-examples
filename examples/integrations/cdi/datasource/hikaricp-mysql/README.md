# Helidon Examples Integrations CDI DataSource MySQL

This project implements a simple REST service using Helidon MP and JDBC.

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
java -jar target/helidon-examples-integrations-cdi-datasource-hikaricp-mysql.jar
```

### Exercise the application

```shell
# list all tables
curl -X GET http://localhost:8080/tables
```

## Stop

```shell
docker stop mysql
```

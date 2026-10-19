# Helidon Examples DbClient Health

This project demonstrates how to use DbClient Health.

## Build

```shell
mvn package
```

## Run

Start MySQL:
```shell
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=db1 \
  -e MYSQL_USER=user \
  -e MYSQL_PASSWORD=mysql123 \
  -p 3306:3306 \
  container-registry.oracle.com/mysql/community-server:9.7.3
```

On ARM machines, use the image tag `9.7.3-aarch64` instead.
Wait for MySQL to accept connections before starting the application.
If the container already exists, start it with `docker start mysql`.

Start the application:
```shell
java -jar target/helidon-examples-dbclient-health.jar
```

### Exercise the application

```shell
curl -X GET http://localhost:8080/observe/health
```

## Stop

Stop the application, then stop MySQL:
```shell
docker stop mysql
```

To remove the database and its data:
```shell
docker rm -v mysql
```

# Helidon Examples DbClient Tracing

This project demonstrates how to use DbClient Tracing.

## Build

```shell
mvn package
```

## Run

Start the database:
```shell
docker run -d \
  --name jaeger \
  -p 14250:14250 \
  -p 16686:16686 \
  cr.jaegertracing.io/jaegertracing/jaeger:2.10.0
```

Or, if the container already exists:
```shell
docker start jaeger
```

Start the application:
```shell
java -jar target/helidon-examples-dbclient-tracing.jar
```

### Exercise the application

```shell
# create an entry
curl -X POST -d 'bar' http://localhost:8080/db/foo

# get an entry
curl -X GET http://localhost:8080/db/foo

# update an entry
curl -X PUT -d 'bar' http://localhost:8080/db/foo

# delete an entry
curl -X DELETE http://localhost:8080/db/foo

# look at the traces
jq -r '.data[].spans[] | select(.tags[] | select(.key == "component" and .value == "dbclient"))' \
  <(curl -X GET "Accept: application/json" "http://localhost:16686/api/traces?service=helidon-examples-dbclient-tracing")
```

## Stop

```shell
docker stop jaeger
```

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
  -p 4317:4317 \
  -p 16686:16686 \
  cr.jaegertracing.io/jaegertracing/jaeger:2.21.0
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
```

Wait a few seconds for Jaeger to receive the traces, then query the last hour
and display spans from DbClient. Jaeger v3 requires time bounds and returns
spans in OTLP JSON format:

```shell
curl --fail --silent --show-error --get \
  -H 'Accept: application/json' \
  --data-urlencode 'query.serviceName=helidon-examples-dbclient-tracing' \
  --data-urlencode "query.startTimeMin=$(jq -nr 'now - 3600 | todateiso8601')" \
  --data-urlencode "query.startTimeMax=$(jq -nr 'now | ceil | todateiso8601')" \
  http://localhost:16686/api/v3/traces \
  | jq '.result.resourceSpans[]?.scopeSpans[]?.spans[]?
        | select(any(.attributes[]?; .key == "component" and .value.stringValue == "dbclient"))'
```

## Stop

```shell
docker stop jaeger
```

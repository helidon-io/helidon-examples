# Helidon Examples DbClient Metrics

This project demonstrates how to use DbClient Metrics.

## Build

```shell
mvn package
```

## Run

Start the application:
```shell
java -jar target/helidon-examples-dbclient-metrics.jar
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

# look at the metrics
curl -X GET -H "Accept: application/json" http://localhost:8080/observe/metrics/application | jq
```

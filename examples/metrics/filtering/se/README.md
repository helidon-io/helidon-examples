# Helidon Metrics Name Filtering SE Example

This project implements a simple Hello World REST service using Helidon SE and demonstrates narrowing the metrics endpoint
report using its `name` query parameter.

## Build and run

```shell
mvn package
java -jar target/helidon-examples-metrics-se.jar
```

## Exercise the application

```shell
curl -X GET http://localhost:8080/greet
#Output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
#Output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
#Output: {"message":"Hola Jose!"}

curl -X GET http://localhost:8080/greet
#Output: {"message":"Hola World!"}
```

## Retrieve selected metrics

Use the registered meter name to select which meter family the metrics endpoint reports:

```shell
curl -s 'http://localhost:8080/observe/metrics?name=counterForPersonalizedGreetings'
```

The response contains the `counterForPersonalizedGreetings` meter family and does not contain `timerForGets`.

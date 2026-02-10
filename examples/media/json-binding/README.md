# Helidon SE Media JSON Patch Example

This example demonstrates Helidon JSON Binding HTTP media support.

This project exposes a simple `HttpService` where you can test Helidon JSON Binding with a `JsonObject` as well as custom type.

## Build

```shell
mvn package
```

## Run

First, start the server:

```shell
java -jar target/helidon-examples-media-json-binding.jar
```

## Try it!

This uses dynamic JsonObject to process and respond to your requests.

```shell
curl -X GET http://localhost:8080/value
#Output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/value/Joe
#Output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/value/greeting

curl -X GET http://localhost:8080/value/Jose
#Output: {"message":"Hola Jose!"}
```

This usage uses POJO binding instead of generic JsonObject processing.

```shell
curl -X GET http://localhost:8080/object
#Output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/object/Joe
#Output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/object/greeting

curl -X GET http://localhost:8080/object/Jose
#Output: {"message":"Hola Jose!"}
```

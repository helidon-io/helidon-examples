# Helidon SE Media JSON Patch Example

This example demonstrates `application/json-patch+json` media type support.

This project exposes a simple `HttpService` where you can access, insert and patch a `JsonObject`.

## Build

```shell
mvn package
```

## Run

First, start the server:

```shell
java -jar target/helidon-examples-media-json-patch.jar
```

## Try it!

```shell
# verify the empty initial state
curl http://localhost:8080/patch
{}

# set the object
curl -X PUT -H "Content-Type: application/json" http://localhost:8080/patch -d '{"foo": "bar"}'

# verify the object
curl http://localhost:8080/patch
{"foo":"bar"}

# patch the object
curl -X POST -H "Content-Type: application/json-patch+json" http://localhost:8080/patch -d '[ {"op":"replace","path":"/foo","value": "123"} ]'

# verify the result
curl http://localhost:8080/patch
{"foo":"123"}
```

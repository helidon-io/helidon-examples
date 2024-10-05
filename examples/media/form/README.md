# Helidon SE Media Form Example

This example demonstrates how to use `Parameters` consume or upload form data with both the `WebServer`
 and `WebClient` APIs.

This project implements a simple web application that supports uploading
and listing form data. The unit test uses the `WebClient` API to test the endpoints.

## Build

```shell
mvn package
```

## Run

First, start the server:

```shell
java -jar target/helidon-examples-media-form.jar
```

Then open <http://localhost:8080/ui> in your browser.

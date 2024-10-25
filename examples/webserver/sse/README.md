# Helidon SE SSE Example

This example demonstrates how to use Server Sent Events (SSE) with both the WebServer and WebClient APIs.

This project implements a couple of SSE endpoints that send either text or JSON messages.
The unit test uses the WebClient API to test the endpoints.

## Build, run and test

Build and start the server:
```shell
mvn package
java -jar target/helidon-examples-webserver-sse.jar
```

Then open http://localhost:8080 in your browser.

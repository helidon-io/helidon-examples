Declarative Typed WebClient Example with JSON Binding
---

This example shows how to use Helidon declarative to create an HTTP server "Hello World" endpoint using Helidon JSON binding,
and a declarative webclient to invoke that endpoint.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-webclient.jar
```

Expected output should be similar to the following:

```text
2026.09.16 13:32:26.933 INFO Logging at runtime configured using classpath: /logging.properties
2026.09.16 13:32:27.774 INFO Helidon SE 27.0.0-SNAPSHOT features: [Config, Encoding, JSON, Media, Registry, WebClient, WebServer]
2026.09.16 13:32:27.777 INFO [0x4b73cae2] http://0.0.0.0:8080 bound for socket '@default'
2026.09.16 13:32:27.796 INFO Started all channels in 21 milliseconds. 937 milliseconds since JVM startup. Java 27+35-2325
Server started on: http://localhost:8080/hello
```

Helidon JSON Binding is a preview feature.

# Declarative Typed WebClient

The REST endpoint is separated into three types:

- `HelloWorldApi` - definition of the path, and HTTP methods on the endpoint, used by both server and client
- `HelloWorldServerEndpoint` - implementation of the server endpoint
- `HelloWorldClientEndpoint` - definition of the type client

In a real application, the `HelloWorldApi` may be a library dependency, or it may not exist at all, and you could only create
`HelloWorldClientEndpoint`.

The client endpoint can be injected with qualifier `@RestClient.Client`.

This examples obtains the instance programmatically in the test class, to validate everything works.

Note that for testing, we use a "known" configuration key in `applicaton-test.yaml` to override the client endpoint. 
The key is `test.server.port`, and it will contain the port of the `@default` socket of the WebServer (as we use random
socket for testing).

# Exercising the application

The following endpoints are available:

## Hello

There are several methods for handling `/hello` path (in class `HelloWorldEndpoint`):

1. `hello()` - a simple "Hello World"
2. `hello(String)` - a "Hello World" that uses path parameter to provide a named greeting
3. `updateGreeting(String)` - a POST method handler to update the greeting value in memory

To execute these endpoints, you can use the following `curl` commands:

Command:

```shell
curl -i -H "Accept: application/json" -i http://localhost:8080/hello
```

Expected output:

```
HTTP/1.1 200 OK
Date: Tue, 6 Jan 2026 16:00:04 +0100
Connection: keep-alive
Content-Type: application/json
Transfer-Encoding: chunked

{"greeting":"Hello","name":"World"}
```

Command:

```shell
curl -i -H "Accept: application/json" -i http://localhost:8080/hello/Reader
```

Expected output:

```
HTTP/1.1 200 OK
Date: Tue, 6 Jan 2026 16:02:21 +0100
Connection: keep-alive
Content-Type: application/json
Transfer-Encoding: chunked

{"greeting":"Hello","name":"Reader"}
```

Command:

```shell
curl -i -X POST -d "Ahoj" -H "Content-Type: text/plain" -i http://localhost:8080/hello
```

Expected output:

```
HTTP/1.1 204 No Content
Date: Tue, 6 Jan 2026 14:18:09 +0100
Connection: keep-alive
Content-Length: 0
```

And the next call to greet (i.e. the fist command) should provide:

```
HTTP/1.1 200 OK
Date: Tue, 6 Jan 2026 16:00:04 +0100
Connection: keep-alive
Content-Type: application/json
Transfer-Encoding: chunked

{"greeting":"Ahoj","name":"World"}
```

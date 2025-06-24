Declarative Server Example
---

This example shows how to use Helidon declarative to create an HTTP server endpoint, and use other declarative features.

The example can be built using GraalVM native image as well.

# Running as jar

Build this application:
```shell
mvn clean package
```

Run from command line:
```shell
java -jar target/helidon-examples-declarative-server.jar
```

Expected output should be similar to the following:
```text
2025.06.24 14:21:40.389 INFO Logging at runtime configured using classpath: /logging.properties
2025.06.24 14:21:40.573 INFO [0x5b2b9773] http://0.0.0.0:8080 bound for socket '@default'
2025.06.24 14:21:40.574 INFO Helidon SE 4.3.0 features: [Config, Encoding, Media, Metrics, Registry, Scheduling, Tracing, WebServer]
2025.06.24 14:21:40.577 INFO Started all channels in 6 milliseconds. 221 milliseconds since JVM startup. Java 21.0.7+8-LTS-245
Server started on: http://localhost:8080/api/business/greet
```

# Running as native image
You must use GraalVM with native image installed as your JDK,
or you can specify an environment variable `GRAALVM_HOME` that points
to such an installation.

Build this application:
```shell
mvn clean package -Pnative-image
```

Run from command line:
```shell
./target/helidon-examples-declarative-server 
```

Expected output should be the same as when starting regular Java
```text
2020.11.19 15:38:14 INFO io.helidon.logging.common.LogConfig Thread[main,5,main]: Logging at runtime configured using classpath: /logging.properties ""
2020.11.19 15:38:14 INFO io.helidon.examples.logging.jul.Main Thread[main,5,main]: Starting up "startup"
2020.11.19 15:38:14 INFO io.helidon.examples.logging.jul.Main Thread[pool-1-thread-1,5,main]: Running on another thread "propagated"
2020.11.19 15:38:14 INFO io.helidon.common.features.HelidonFeatures Thread[features-thread,5,main]: Helidon SE 2.2.0 features: [Config, WebServer] ""
2020.11.19 15:38:14 INFO io.helidon.reactive.webserver.NettyWebServer Thread[nioEventLoopGroup-2-1,10,main]: Channel '@default' started: [id: 0x2b929906, L:/0:0:0:0:0:0:0:0:8080] ""
```

# Exercising the application

The following endpoints are available:

## Scheduling Counter

Endpoint that provides the number of times the `ScheduledTasks.refreshTask()` was invoked

```shell
curl -i http://localhost:8080/api/business/refresh
```

Result should be similar to:

```text
HTTP/1.1 200 OK
Date: Tue, 24 Jun 2025 14:30:36 +0200
Connection: keep-alive
Content-Type: application/json
Transfer-Encoding: chunked

{"count":2}
```

## Greetings

There are several methods for handling `/greet` path (in class `BusinessServiceEndpoint`):

1. `greeting()` - a simple "Hello World" that returns text
2. `greeting(String)` - a "Hello World" that uses path parameter to provide a named greeting
3. `jsonGreeting()` - a JSON equivalent of `greeting()`
4. `jsonGreeting(String)` - a JSON equivalent of `greeting(String)`
5. `updateGreeting(String)` - a POST method handler to update the greeting value in memory

To execute these endpoints, you can use the following `curl` commands:

```shell
curl -H "Accept: text/plain" -i http://localhost:8080/api/business/greet
curl -H "Accept: text/plain" -i http://localhost:8080/api/business/greet/Reader
curl -H "Accept: application/json" -i http://localhost:8080/api/business/greet
curl -H "Accept: application/json" -i http://localhost:8080/api/business/greet/Reader
curl -X POST -d "Ahoj" -H "Content-Type: text/plain" -i http://localhost:8080/api/business/greet
```
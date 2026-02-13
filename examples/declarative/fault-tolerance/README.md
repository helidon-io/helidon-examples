Declarative Server Example with Fault Tolerance
---

This example shows how to use Helidon declarative to create an HTTP server endpoint and use fault tolerance
with it. This example shows usage of fallback and retry fault tolerance handlers.

The example can be built using GraalVM native image as well.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-fault-tolerance.jar
```

Expected output should be similar to the following:

```text
2026.01.09 12:08:28.275 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.09 12:08:28.484 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Fault Tolerance, Media, Registry, WebServer]
2026.01.09 12:08:28.484 INFO [0x4feacf62] http://0.0.0.0:8080 bound for socket '@default'
2026.01.09 12:08:28.495 INFO Started all channels in 6 milliseconds. 273 milliseconds since JVM startup. Java 21.0.7+8-LTS-245
Server started on: http://localhost:8080/ft
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
./target/helidon-examples-declarative-fault-tolerance 
```

Expected output should be the same as when starting regular Java

```text
2026.01.09 12:11:34.470 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.09 12:11:34.476 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Fault Tolerance, Media, Registry, WebServer]
2026.01.09 12:11:34.476 INFO [0x692ddcb1] http://0.0.0.0:8080 bound for socket '@default'
2026.01.09 12:11:34.476 INFO Started all channels in 0 milliseconds. 7 milliseconds since JVM startup. Java 21.0.3+7-LTS-jvmci-23.1-b37
Server started on: http://localhost:8080/ft
```

# Exercising the application

The following endpoints are available:

## Fallback

Fallback is defined as two methods in `FaltToleranceEndpoint`:

1. `hello()` - method that calls a `failingOperation()` which in the real world would be an operation that can fail and throw an
   exception; the `@Ft.Fallback` annotation on `hello` method defines which method to call (it must have the same parameters and
   return type), and what are the conditions for the fallback (i.e. which exceptions to use, which to ignore)
2. `helloFallback()` - method invoked as a fallback

In addition, the `failingOperation` is now implemented to throw an excpetion on each invocation, so the fallback will always
trigger.

To execute this endpoint, you can use the following `curl` commands:

Command:

```shell
curl -i http://localhost:8080/ft/fallback
```

Expected output:

```
HTTP/1.1 200 OK
Date: Fri, 9 Jan 2026 12:14:35 +0100
Connection: keep-alive
Content-Length: 8
Content-Type: text/plain

Fallback 
```

## Retry

Retry is defined in method `retry()` in `FaltToleranceEndpoint`:

The method throws an exception every second invocation (using an atomic counter).
The annotation `@Ft.Retry` defines how to handle retries.

As the retry expects two calls, we always succeed in invocation. If the number is even, we return successfully,
if the number is odd, we throw an exception, and the retry "kicks-in" and the method is called again and succeeds.

To execute this endpoint, you can use the following `curl` commands:

Command:

```shell
curl -i http://localhost:8080/ft/retry
```

Expected output:

```
HTTP/1.1 200 OK
Date: Fri, 9 Jan 2026 12:17:12 +0100
Connection: keep-alive
Content-Length: 7
Content-Type: text/plain

Success
```

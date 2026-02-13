Declarative Server Example with Validation
---

This example shows how to use Helidon declarative with validation.
This example uses WebServer endpoint, as it is easy to invoke the endpoints to show the behavior.

*IMPORTANT*: configuration uses `server.error-handling.include-entity: true` so we can easily see why our request failed; this
MUST NOT be used in production settings where the endpoint is invoked by untrusted parties, as this may leak internal information

The example can be built using GraalVM native image as well.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-validation.jar
```

Expected output should be similar to the following:

```text
2026.01.09 13:11:44.392 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.09 13:11:44.629 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Registry, Validation, WebServer]
2026.01.09 13:11:44.630 INFO You are using preview features. These APIs are production ready, yet may change more frequently. Please follow Helidon release changelog!
2026.01.09 13:11:44.630 INFO [0x403469ef] http://0.0.0.0:8080 bound for socket '@default'
2026.01.09 13:11:44.630 INFO    Preview feature: Validation (Validation)
2026.01.09 13:11:44.634 INFO Started all channels in 10 milliseconds. 327 milliseconds since JVM startup. Java 21.0.3+7-LTS-jvmci-23.1-b37
Server started on: http://localhost:8080/validate
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
./target/helidon-examples-declarative-metrics 
```

Expected output should be the same as when starting regular Java

```text
2026.01.09 13:31:35.730 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.09 13:31:35.736 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Registry, Validation, WebServer]
2026.01.09 13:31:35.736 INFO You are using preview features. These APIs are production ready, yet may change more frequently. Please follow Helidon release changelog!
2026.01.09 13:31:35.736 INFO    Preview feature: Validation (Validation)
2026.01.09 13:31:35.737 INFO [0x741bade2] http://0.0.0.0:8080 bound for socket '@default'
2026.01.09 13:31:35.737 INFO Started all channels in 1 milliseconds. 8 milliseconds since JVM startup. Java 21.0.3+7-LTS-jvmci-23.1-b37
Server started on: http://localhost:8080/validate
```

# Application sources

The `ValiationEndpoint` defines an endpoint to validate the provided JSON against the validation rules
in `MyDto`.

There is a single method `validate`, that accepts JSON and returns JSON.
The `@Validation.Valid` ensures the `dto` parameter is validated.

In `MyDto`, the `@Validation.Validated` ensures code generation of validator for the type, and each getter defines
validation rules for that property.

# Exercising the application

## Failed validations

Command:

```shell
 curl -i -X POST -d '{"age":-1,"name":"Tom"}' -H "Accept: application/json" -H "Content-Type: application/json" http://localhost:8080/validate
```

Expected output:

```
HTTP/1.1 400 Bad Request
Date: Fri, 9 Jan 2026 13:19:38 +0100
Connection: close
Content-Length: 803
Content-Type: text/plain

Constraint validation failed: does not match pattern &quot;.*example&quot; with flags 0 at TYPE(io.helidon.examples.declarative.validation.ValidationEndpoint)/METHOD(validate(io.helidon.examples.declarative.validation.MyDto))/PARAMETER(dto)/TYPE(io.helidon.examples.declarative.validation.MyDto)/PROPERTY(getName), -1 is less than 0 at TYPE(io.helidon.examples.declarative.validation.ValidationEndpoint)/METHOD(validate(io.helidon.examples.declarative.validation.MyDto))/PARAMETER(dto)/TYPE(io.helidon.examples.declarative.validation.MyDto)/PROPERTY(getAge)
```

As we can see we have two validation failures:

1. the `name` does not match the pattern
2. the 'age' is less that the expected 0

## Validation success

Command:

```shell
 curl -i -X POST -d '{"age":14,"name":"goodexample"}' -H "Accept: application/json" -H "Content-Type: application/json" http://localhost:8080/validate
```

Expected output:

```
HTTP/1.1 200 OK
Date: Fri, 9 Jan 2026 13:22:20 +0100
Connection: keep-alive
Content-Type: application/json
Transfer-Encoding: chunked

{"age":14,"name":"goodexample"}
```
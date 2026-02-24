Declarative Security Example
---

This example shows how to use Helidon declarative to create an HTTP server "Hello World" endpoint with a protected path.

The example can be built using GraalVM native image as well.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-webserver-hello-world.jar
```

Expected output should be similar to the following:

```text
2026.01.06 14:13:12.705 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.06 14:13:12.915 INFO [0x3050f673] http://0.0.0.0:8080 bound for socket '@default'
2026.01.06 14:13:12.915 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Registry, Security, WebServer]
2026.01.06 14:13:12.927 INFO Started all channels in 7 milliseconds. 276 milliseconds since JVM startup. Java 21.0.7+8-LTS-245
Server started on: http://localhost:8080/hello
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
./target/helidon-examples-declarative-webserver-hello-world 
```

Expected output should be the same as when starting regular Java

```text
2026.01.06 14:14:19.234 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.06 14:14:19.240 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Registry, Security, WebServer]
2026.01.06 14:14:19.241 INFO [0x6929dd42] http://0.0.0.0:8080 bound for socket '@default'
2026.01.06 14:14:19.241 INFO Started all channels in 1 milliseconds. 8 milliseconds since JVM startup. Java 21.0.3+7-LTS-jvmci-23.1-b37
Server started on: http://localhost:8080/hello
```

# Exercising the application

The following endpoints are available:

## Hello

There are several methods for handling `/hello` path (in class `HelloWorldEndpoint`):

1. `hello()` - a simple "Hello World"
2. `hello(String)` - a "Hello World" that uses path parameter to provide a named greeting
3. `updateGreeting(String)` - a POST method handler to update the greeting value in memory, protected with `@Authorized` and
        `@RolesValidator.Roles("admin")`, so only admin user can invoke this method

To execute these endpoints, you can use the following `curl` commands:

Command:

```shell
curl -i -H "Accept: text/plain" -i http://localhost:8080/hello
```

Expected output:

```
HTTP/1.1 200 OK
Date: Tue, 6 Jan 2026 14:16:20 +0100
Connection: keep-alive
Content-Length: 11
Content-Type: text/plain

Hello World    
```

Command:

```shell
curl -i -H "Accept: text/plain" -i http://localhost:8080/hello/Reader
```

Expected output:

```
HTTP/1.1 200 OK
Date: Tue, 6 Jan 2026 14:17:42 +0100
Connection: keep-alive
Content-Length: 12
Content-Type: text/plain

Hello Reader
```

Command:

```shell
curl -i -X POST -d "Ahoj" -H "Content-Type: text/plain" -i http://localhost:8080/hello
```

Expected output:

```
HTTP/1.1 204 No Content
Date: Tue, 24 Feb 2026 15:55:30 +0100
Connection: keep-alive
Content-Length: 0
```

And the next call to greet (i.e. the fist command) should provide:

```
HTTP/1.1 200 OK
Date: Tue, 6 Jan 2026 14:18:41 +0100
Connection: keep-alive
Content-Length: 10
Content-Type: text/plain

Ahoj World
```
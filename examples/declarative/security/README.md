Declarative Security Example
---

This example shows how to use Helidon declarative to create an HTTP server "Hello World" endpoint with a protected path.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-security.jar
```

Expected output should be similar to the following:

```text
2026.01.06 14:13:12.705 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.06 14:13:12.915 INFO [0x3050f673] http://0.0.0.0:8080 bound for socket '@default'
2026.01.06 14:13:12.915 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Registry, Security, WebServer]
2026.01.06 14:13:12.927 INFO Started all channels in 7 milliseconds. 276 milliseconds since JVM startup. Java 21.0.7+8-LTS-245
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

Command (no user to a protected endpoint):

```shell
curl -i -X POST -d "Ahoj" -H "Content-Type: text/plain" -i http://localhost:8080/hello
```

Expected output:

```
HTTP/1.1 401 Unauthorized
Date: Tue, 24 Feb 2026 15:54:45 +0100
Connection: keep-alive
Content-Length: 46
Content-Type: text/plain
WWW-Authenticate: Basic realm="helidon"
```

Command (user with wrong role):

```shell
curl -i -X POST -d "Ahoj" -u "jack:changeit" -H "Content-Type: text/plain" -i http://localhost:8080/hello
```

Expected output:

```
HTTP/1.1 403 Forbidden
Date: Tue, 24 Feb 2026 15:55:42 +0100
Connection: keep-alive
Content-Length: 46
Content-Type: text/plain

Security did not allow this request to proceed%  
```

Command (correct role):

```shell
curl -i -X POST -d "Ahoj" -u "john:changeit" -H "Content-Type: text/plain" -i http://localhost:8080/hello
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

Declarative WebSocket Example
---

This example shows how to use Helidon declarative to create a websocket application.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-websocket.jar
```

Expected output should be similar to the following:

```text
2026.02.27 11:48:39.273 INFO Logging at runtime configured using classpath: /logging.properties
2026.02.27 11:48:39.471 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Registry, WebServer]
2026.02.27 11:48:39.472 INFO [0x48bed351] http://0.0.0.0:8080 bound for socket '@default'
2026.02.27 11:48:39.479 INFO Started all channels in 9 milliseconds. 238 milliseconds since JVM startup. Java 21.0.7+8-LTS-245
Server started on: http://localhost:8080/web
```

# Exercising the application

[Open In browser](http://localhost:8080/web/index.html)

- The `Send to Rest` button will send a message to a message queue through the HTTP endpoint, backed by `MessageQueueHttpEndpoint`
- The `Send to Websocket` button will send `SEND` message to the WebSocket endpoint, backed by `MessageBoardWsEndpoint` that
    will check if the queue has any messages, and if so, send them to the WebSocket client (the browser), which will display
    the messages as `Message is received: UPPER_CASED_MESSAGE`

# Configuration details

The following section in `application.yaml` configures static content:

```yaml
features:
  static-content:
      welcome: "index.html"
      classpath:
        - context: "web"
          location: "WEB"
```

The `welcome` defines a welcome file the static content looks for if a directory is requested.

We can configure multiple instances of classpath (or file) static content; each should at least have the `context` (where it will be exposed over HTTP), and `location` (where is the directory on the classpath). 

# Implementations details

The following classes are used in this example:

- `Main` - the entry point to the application, configured in `pom.xml` to be added to the jar manifest, so you can simply run `java -jar ...`; see comments in the class for details of each line
- `MessageQueue` - a singleton service taking care of communication between the WebSocket and HTTP endpoints to transfer messages
- `MessageQueueHttpEndpoint` - an HTTP endpoint (`@RestServer.Endpoint`) that exposes a `POST` method (`@Http.Post`) on path `/rest` (`@Http.Path("/rest")`); this type injects the `MessageQueue` to push messages to it
- `MessageBoardWsEndpoint` - a WebSocket endpoint (`@WebSocketServer.Endpoint`) that exposes websocket operations on path `/websocket` (`@Http.Path("/websocket")`); this type also injects the `MessageQueue` to read message from in method `onMessage`

The example implementation uses `INFO` log level and prints unsanitized user input - this is for example only, and MUST be changed for production use:

- you should never use `INFO` messages for each request, as that can be easily misused by attackers to overload your logs
- you must never use unsanitized user input, as that can be easily used by attackers to inject text into your logs that could be misinterpreted (or could cause even more damage)
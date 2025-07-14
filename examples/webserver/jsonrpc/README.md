# Helidon JSON-RPC Example

This example shows how to use the JSON-RPC SE API. The main class `JsonRpcMain`
creates a routing object for JSON-RPC and a couple of methods on a _machine_
resource. The single test class in the project uses the JSON-RPC client API
to test the application.

For more information about the protocol see the [JSON-RPC Specification](https://www.jsonrpc.org/specification).

## Build and run tests

```shell
mvn package
```

## Run the app

```shell
java -jar target/helidon-examples-webserver-jsonrpc.jar
```

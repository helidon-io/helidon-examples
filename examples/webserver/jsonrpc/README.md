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

## Exercise the application

```shell
curl -X POST \
     -H 'Content-Type: application/json' \
     -d '{ "jsonrpc": "2.0", "id": 1, "method": "start", "params" : { "when": "NOW", "duration": "PT0S" } }' \
     http://localhost:8080/rpc/machine
      
#Output: {"jsonrpc":"2.0","id":1,"result":{"status":"RUNNING"}}
```
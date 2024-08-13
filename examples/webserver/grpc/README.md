# Helidon gRPC SE Example

This example shows a simple _Strings_ service written with the Helidon gRPC SE 
API. See `StringService` for the service implementation and `StringServiceTest`
for how to use the Helidon's `WebClient` to access the service. Client interceptors 
are also supported, see `StringServiceInterceptor` for an example.

The gRPC service definition is found in the `strings.proto` file which is compiled 
using `protoc` at build time. The Strings service includes all 4 types of methods:
unary, client streaming, server streaming and bidirectional.

## Build and run tests

```shell
mvn package
```

## Run the app

```shell
java -jar target/helidon-examples-webserver-grpc.jar
```

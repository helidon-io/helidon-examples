# Helidon gRPC SE Randome Example


This example shows a simple _Random_ service written with the Helidon gRPC SE 
API. See `RandomService` for the service implementation and `RandomServiceTest`
for how to use the Helidon's `WebClient` to access the service. .

The gRPC service definition is found in the `random.proto` file which is compiled 
using `protoc` at build time. 

## Build and run tests

```shell
mvn package
```

## Run the app

```shell
java -jar target/helidon-examples-webserver-grpc-random.jar
```

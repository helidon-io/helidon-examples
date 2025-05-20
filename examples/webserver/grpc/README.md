# Helidon gRPC SE Example

This example shows a simple _Strings_ service written with the Helidon gRPC SE 
API. See `StringService` for the service implementation and `StringServiceTest`
for how to use the Helidon's `WebClient` to access the service. Client interceptors 
are also supported, see `StringServiceInterceptor` for an example.

The gRPC service definition is found in the `strings.proto` file which is compiled 
using `protoc` at build time. The Strings service includes all 4 types of methods:
unary, client streaming, server streaming and bidirectional.

This example also enables the `grpc-reflection` feature to expose the Helidon
gRPC reflection service. This service answers reflection queries so that clients
that support [this protocol](https://grpc.io/docs/guides/reflection/) can interact 
with the _Strings_ service without having access to the `strings.proto` file.

## Build and run tests

```shell
mvn package
```

## Run the app

```shell
java -jar target/helidon-examples-webserver-grpc.jar
```

## Testing the app using `grpcurl`

With the gRPC reflection feature enabled:

```shell
>> grpcurl -insecure -d '{ "text": "hello world" }' localhost:8080 StringService.Split
{
  "text": "hello"
}
{
  "text": "world"
}
```

Note that the `-proto` parameter of `grpcurl` is not required in this
example (see `grpc-reflection` feature in `application.yaml`). The `-insecure` option 
is necessary to skip certificate and domain verification given the use of self-signed
certificates in this example.

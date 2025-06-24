# Helidon gRPC MP Example

This examples shows a simple application written using Helidon gRPC MP API:

- StringService: a gRPC service implementation that uses MP
- StringServiceClient: an interface from which a client proxy can be created to call StringService remote methods
- StringServiceTest: a sample test that starts a server and tests the client and server components
- application.yaml: configuration for server and client channels

## Build and run tests

```shell
mvn package
```

## Run the app

```shell
java -jar target/helidon-examples-microprofile-grpc.jar
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
# Helidon WebServer Tracing Example

This project demonstrates Helidon SE server and client tracing using the `tracing` configuration together with the OpenTelemetry tracing provider.

The example exposes a version-specific route and a client route:

* `GET /versionspecific` returns different text for HTTP/1.1 and HTTP/2 requests.
* `GET /client` uses a traced `WebClient` call to invoke `/versionspecific` and also creates a manual `custom-span`.

## Start a tracing back-end

Run Jaeger with OTLP enabled so the example can export spans using the default OpenTelemetry gRPC endpoint on `localhost:4317`.

```bash
docker run -d --rm --name helidon-examples-jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  cr.jaegertracing.io/jaegertracing/jaeger:2.17.0
```

## View the tracing configuration

Look at `src/main/resources/application.yaml`.

The example keeps the same service and sampling behavior as before and makes the OpenTelemetry exporter choice explicit:

* `service` identifies the traced application in Jaeger as `helidon-server`.
* `sampler-type` and `sampler-param` keep tracing enabled for every request. The OpenTelemetry provider uses `constant` for the same always-sample behavior that the old Jaeger configuration expressed as `const`.
* `exporter-type: grpc` sends spans using OTLP gRPC. The example relies on the default collector settings, so Jaeger only needs OTLP enabled on `localhost:4317`.

## Build and run

With JDK 26:

```bash
mvn package
java -jar target/helidon-examples-webserver-tracing.jar
```

## Exercise the application

HTTP/1.1 route:

```bash
curl http://localhost:8080/versionspecific
```

Expected response:

```text
HTTP/1.1 route
```

HTTP/2 route:

```bash
curl --http2-prior-knowledge http://localhost:8080/versionspecific
```

Expected response:

```text
HTTP/2 route
```

Traced client route:

```bash
curl http://localhost:8080/client
```

Expected response:

```text
HTTP/1.1 route
```

## View traces in Jaeger

1. Open `http://localhost:16686`.
2. Select the `helidon-server` service and run a search.
3. Open a trace created by `GET /client`.

The trace should include the incoming server span, the outgoing traced `WebClient` call, and the manual `custom-span`.

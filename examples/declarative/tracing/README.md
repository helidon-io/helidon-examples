# Declarative Server Example with Tracing

This example shows how to use Helidon Declarative tracing annotations together with the Helidon `telemetry`
configuration. The application uses the OpenTelemetry-based Helidon telemetry integration and exports spans directly to
Zipkin using OTLP.

## Start Zipkin

Run Zipkin so the example can export spans using OTLP.

```bash
docker run -d --rm --name helidon-examples-zipkin \
  -p 9411:9411 \
  ghcr.io/openzipkin-contrib/zipkin-otel
```

## View the telemetry configuration

Look at `src/main/resources/application.yaml`.

The important settings are:

* `telemetry.service` sets the service name shown in Zipkin to `helidon-examples-declarative-tracing`.
* `telemetry.signals.tracing.processors` uses the `simple` processor so spans are exported immediately while you exercise
  the example.
* `telemetry.signals.tracing.exporters` selects the `otlp` exporter using the `http/proto` protocol and the Zipkin OTLP
  HTTP endpoint at `http://localhost:9411/v1/traces`.

The endpoint methods are annotated with `@Tracing.Traced`, and selected request values are added to spans using
`@Tracing.ParamTag`.

## Build and run

Build this application:

```bash
mvn package
```

Run from the command line:

```bash
java -jar target/helidon-examples-declarative-tracing.jar
```

The server prints a URL similar to:

```text
Server started on: http://localhost:8080/hello
```

## Exercise the application

Greeting:

```bash
curl -H 'User-Agent: declarative-demo' http://localhost:8080/hello
```

Expected output:

```text
Hello World
```

Named greeting:

```bash
curl -H 'User-Agent: declarative-demo' http://localhost:8080/hello/Joe
```

Expected output:

```text
Hello Joe
```

Update the greeting:

```bash
curl -X POST -H 'Content-Type: text/plain' -d 'Hola' http://localhost:8080/hello
curl -H 'User-Agent: declarative-demo' http://localhost:8080/hello/Jose
```

Expected output:

```text
Hola Jose
```

## View traces in Zipkin

1. Open `http://localhost:9411/zipkin/`.
2. Select the `helidon-examples-declarative-tracing` service and run a search.
3. Open one of the traces created by the `GET /hello` or `GET /hello/{name}` requests.

The trace should include:

* the incoming `HTTP Request` span created by WebServer tracing,
* the `content-write` span for the response, and
* the declarative method span such as `greet-world` or `greet-name`.

The declarative method span also carries tags from the tracing annotations, including the `route` tag and the
`userAgent` parameter tag.

## Stop Zipkin

```bash
docker stop helidon-examples-zipkin
```

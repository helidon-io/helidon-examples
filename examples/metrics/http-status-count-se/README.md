# http-status-count-se

This Helidon SE project illustrates a service which updates a family of counters based on the final HTTP status returned
in each response.

The main source in this example is identical to that in the Helidon SE QuickStart application except in these ways:
* The `HttpStatusMetricService` class creates and updates the status metrics.
* The `Main` class has a two small enhancements:
   * The `createRouting` method instantiates `HttpStatusMetricService` and sets up routing for it.
   * The `startServer` method has an additional variant to simplify a new unit test.

## Incorporating status metrics into your own application
Use this example for inspiration in writing your own service or just use the `HttpStatusMetricService` directly in your own application.

Add a metrics provider to your application's dependencies. The metrics API and metrics observer do not select a provider.
This example uses the Helidon provider:

```xml
<dependency>
    <groupId>io.helidon.metrics.providers</groupId>
    <artifactId>helidon-metrics-providers-helidon</artifactId>
    <scope>runtime</scope>
</dependency>
```

1. Copy and paste the `HttpStatusMetricService` class into your application, adjusting the package declaration as needed.
2. Register routing for an instance of `HttpStatusMetricService`, as shown here:
   ```java
   Routing.Builder builder = Routing.builder()
                ...
                .register(HttpStatusMetricService.create()
                ...
   ```

## Build and run


```shell
mvn package
java -jar target/http-status-count-se.jar
```

## Exercise the application
```shell
curl -X GET http://localhost:8080/simple-greet
```
```listing
{"message":"Hello World!"}
```

```shell
curl -X GET http://localhost:8080/greet
```
```listing
{"message":"Hello World!"}
```
```shell
curl -X GET http://localhost:8080/greet/Joe
```
```listing
{"message":"Hello Joe!"}
```
```shell
curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
```
```listing
{"message":"Hola Jose!"}
```

## Try metrics
```shell
# Prometheus Format
curl -s -X GET http://localhost:8080/observe/metrics
```

```listing
...
# TYPE httpStatus_total counter
# HELP httpStatus_total Counts the number of HTTP responses in each final status category (2xx, 3xx, 4xx, and 5xx)
httpStatus_total{range="2xx"} 5
httpStatus_total{range="3xx"} 0
httpStatus_total{range="4xx"} 0
httpStatus_total{range="5xx"} 0
...
```
# JSON Format

```shell
curl -H "Accept: application/json" -X GET http://localhost:8080/observe/metrics
```
```json
{
...
    "httpStatus;range=2xx": 5,
    "httpStatus;range=3xx": 0,
    "httpStatus;range=4xx": 0,
    "httpStatus;range=5xx": 0,
...
```

## Export metrics using OTLP

The example also includes the native OTLP publisher for the Helidon metrics provider. The counters still use the
Helidon metrics API. The publisher writes OTLP/HTTP JSON directly to the HTTP request stream using Helidon JSON Binding.
No Micrometer or OpenTelemetry SDK instrumentation is needed.

OTLP publishing is disabled by default so the application can run without a collector. To enable it, first start an
OpenTelemetry Collector in a separate terminal from this example's directory:

```shell
docker run --rm --name http-status-metrics-collector \
  -p 127.0.0.1:4318:4318 \
  -v "$PWD/collector-config.yaml:/etc/otelcol-contrib/config.yaml:ro" \
  ghcr.io/open-telemetry/opentelemetry-collector-releases/opentelemetry-collector-contrib:0.123.0@sha256:b6be1d9c10123821bebc952c62642abc36fe93c39c143faabc14e0e05065695a
```

Then start the application with publishing enabled:

```shell
java -Dmetrics.publishers.otlp.enabled=true -jar target/http-status-count-se.jar
```

The publisher sends metrics to `http://localhost:4318/v1/metrics` every second, with a five-second export timeout and
`service.name=http-status-count-se`. These settings are under `metrics.publishers.otlp` in `application.yaml`.

Send several requests to `/greet`. The collector output shows the `httpStatus` counter with the `range=2xx` attribute
increasing. The `/observe/metrics` endpoint remains available for Prometheus and JSON output.

Stop the collector after stopping the application:

```shell
docker stop http-status-metrics-collector
```

## Try health

```shell
curl -s -X GET http://localhost:8080/observe/health
```
```listing
{"outcome":"UP",...

```

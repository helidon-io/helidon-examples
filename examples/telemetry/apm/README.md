# Helidon OpenTelemetry to Oracle APM Example

This example shows how to configure a Helidon SE service so it exports OpenTelemetry tracing data to Oracle Application Performance Monitoring (APM) using OTLP over HTTP.

The application code does not call the OpenTelemetry API directly. Helidon prepares the OpenTelemetry SDK from configuration, and the webserver observe tracing integration emits spans for incoming HTTP requests automatically.

## Dependencies to add

The example POM includes the Helidon and OpenTelemetry pieces needed for this setup:

```xml
<dependency>
    <groupId>io.helidon.telemetry</groupId>
    <artifactId>helidon-telemetry-opentelemetry-config</artifactId>
</dependency>
<dependency>
    <groupId>io.helidon.webserver.observe</groupId>
    <artifactId>helidon-webserver-observe-tracing</artifactId>
</dependency>
<dependency>
    <groupId>io.helidon.tracing.providers</groupId>
    <artifactId>helidon-tracing-providers-opentelemetry</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.helidon.webserver.observe</groupId>
    <artifactId>helidon-webserver-observe-telemetry-tracing</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Configuration to add

Look at `src/main/resources/application.yaml`. The important settings are:

* `telemetry.service` assigns the service name shown later in Oracle APM.
* `telemetry.signals.tracing.exporters` configures an OTLP exporter using `http/proto`, which matches Oracle APM's OTLP HTTP ingestion.
* `telemetry.signals.tracing.exporters.headers.Authorization` sends the required `dataKey` header.
* `telemetry.signals.tracing.processors` uses the `simple` processor so the example exports spans immediately. For production systems, prefer the default `batch` processor.

The configuration uses environment variables so you can point the same application at your Oracle APM domain without code changes:

* `APM_TRACES_ENDPOINT` must be the full Oracle APM traces OTLP endpoint, such as:
  * private data key: `https://<apm-data-upload-endpoint>/20200101/opentelemetry/private/v1/traces`
  * public data key: `https://<apm-data-upload-endpoint>/20200101/opentelemetry/public/v1/traces`
* `APM_DATA_KEY` is the matching Oracle APM data key value.

## Build and run

With JDK 21:

```bash
export APM_TRACES_ENDPOINT="https://<apm-data-upload-endpoint>/20200101/opentelemetry/private/v1/traces"
export APM_DATA_KEY="<oracle-apm-data-key>"
mvn package
java -jar target/helidon-examples-telemetry-apm.jar
```

## Exercise the application

Basic:

```bash
curl -X GET http://localhost:8080/simple-greet
Hello World!
```

JSON:

```bash
curl -X GET http://localhost:8080/greet
{"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
{"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
{"message":"Hola Jose!"}
```

## Verify data in Oracle APM

After sending a few requests, open Oracle APM and look for traces from service `helidon-apm-example`.

Each HTTP request should create a trace for this service. In the trace details you should also see the resource attributes configured in `application.yaml`, including `deployment.environment=example` and `telemetry.backend=oracle-apm`.

For more detail on the Oracle APM endpoint and data key requirements, see the
[Oracle APM documentation for ingesting OpenTelemetry data](https://docs.oracle.com/en-us/iaas/application-performance-monitoring/doc/configure-open-source-tracing-systems.html)
and the
[Helidon documentation for OpenTelemetry configuration](https://helidon.io/docs/latest/se/telemetry/open-telemetry).

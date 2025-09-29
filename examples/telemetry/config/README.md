# Helidon OpenTelemetry SE Example

This project implements a simple Hello World REST service using Helidon SE and using OpenTelemetry tracing prepared using Helidon configuration.

## Download and start a telemetry back-end
One easy way to see telemetry in action is to run a back-end server that can collect OpenTelemetry OTLP telemetry data and display it. This example Helidon service by default uses OpenTelemetry to transmit its data using OTLP.

One option is to download the [Jaeger back-end](https://www.jaegertracing.io/download/), install it, and run it, but most modern backends support OTLP. Later steps in this example use Jaeger as an example back-end.

## View the telemetry configuration
Look at the `src/main/resources/application.yaml` file. It contains configuration for OpenTelemetry. Note these settings under `telemetry`:
* `service` - Assigns the service name by which all telemetry from this application is identified. You will use this later when using the telemetry back-end UI to browse traces.
* `tracing.attributes` - Declares settings applied to all traces transmitted to the back-end.
* `processors` - Specifies a single span processor, `simple`, which emits each span as soon as it is ended. This makes sure that OpenTelemetry sends span data to the back-end as soon as possible. 

**NOTE**: In production systems, you should use the default `batch` processor type (with its additional settings if you wish) for better network performance.

## Build and run

With JDK21
```bash
mvn package
java -jar target/helidon-examples-telemetry-config.jar
```

## Exercise the application

Basic:
```
curl -X GET http://localhost:8080/simple-greet
Hello World!
```


JSON:
```
curl -X GET http://localhost:8080/greet
{"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
{"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
{"message":"Hola Jose!"}
```

## Use the telemetry back-end to view tracing information
Use a browser to access the back-end UI and view the spans. For example, with Jaeger:
1. Access `http://localhost:16686`.
2. Expand the upper-left "Service" drop list and select "otel-config-example" and then click the "Find Traces" button. ![Service Selection](images/service-selection.png "Service Selection")
   
   Recall that the configuration assigns "otel-config-example" as the `telemetry.service`, so that is the service name the back-end displays.
3. The UI displays separate traces for each of the requests you made to the Helidon service.
4. Click on one of the traces.
5. The back-end shows two or more spans, depending on which trace you clicked. ![Example GET trace](images/get-trace.png "GET trace")
6. Click on any of the spans. ![Example GET span](images/get-span.png "GET span") Note that the "Process" tags include values for `x` and `y` from the `attributes` settings in the `application.yaml` config file.


# Direct Use of the OpenTelemetry API from a Helidon SE

This project shows how to write a Helidon SE application that uses the OpenTelemetry API directly, without using the Helidon integration with OpenTelemetry.

## Download and start a telemetry back-end
One easy way to see telemetry in action is to run a back-end server that can collect OpenTelemetry OTLP metrics and tracing data and display it. This example Helidon service uses OpenTelemetry to transmit its data using OTLP.

One option is to use the [Signoz self-hosted back-end](https://signoz.io/docs/install/docker/) Docker image. 

Note that the default port for the Signoz UI is 8080, the same port many Helidon example applications--including this one--use. To configure self-hosted Signoz to use a different port, in the Signoz configuration file  `signoz/signoz/deploy/docker/docker-compose.yaml` find the section for `signoz:` and in the `ports` section map the UI port you want to use for the UI to `8080` in the container. For example:
```yaml
signoz:
  ...
  ports:
    - "9090:8080"
```
After you make that change, start Siznoz as explained on the Signoz page linked above and browse to `localhost:9090` to use the Signoz UI.

## View the application `otel` configuration
For convenience, this example includes a utility method which makes the Helidon configuration section `otel` in the `src/main/resources/application.yaml` file available to OpenTelemetry autoconfig as OTel config settings. You can set the values described on the [OpenTelemetry configuration](https://opentelemetry.io/docs/languages/java/configuration/#environment-variables-and-system-properties) page under `otel` in the Helidon config.

## Build and run

With JDK21
```bash
mvn package
java -jar target/helidon-examples-telemetry-otel-autoconfigure.jar
```

## Exercise the application

Basic:
```
curl -X GET http://localhost:8080/greet/Joe
Hello Joe!
```

## Use the telemetry back-end to view tracing information
Use a browser to access the back-end UI and view the spans and metrics. For example, with Signoz:
1. Access `http://localhost:9090`.
2. View tracing spans. 
   
   Click the "Open Traces Explorer" link or hover over the left-hand column of icons and click Traces. The available traces should appear. Click on one to see its details.
3. View metrics.
   
   Hover over the left-hand column again and click Metrics. Click the Explorer link near the upper left. In the query field enter `example.greet.personalized` and then click the "Run Query" button near the upper right. By default the metrics data is updated once a minute (so be patient!).



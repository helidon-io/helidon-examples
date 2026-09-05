# Helidon Metrics Exemplar SE Example

This project implements a simple Hello World REST service using Helidon SE and demonstrates the 
optional metrics exemplar support.

## Start Jaeger (optional)
If you do not start Jaeger, the example app will still function correctly but it will log a warning
when it cannot contact the OpenTelemetry OTLP endpoint on the Jaeger server to report tracing spans. Even so, the metrics output
will contain valid exemplars.

With Docker:
```shell
docker run --rm --name jaeger -d -p 16686:16686 -p 4317:4317 cr.jaegertracing.io/jaegertracing/jaeger:2.17.0
```

## Build and run

```shell
mvn package
java -jar target/helidon-examples-metrics-exemplar.jar
```

## Exercise the application

```shell
curl -X GET http://localhost:8080/greet
#output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
#output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
#output:  {"message":"Hola Jose!"}

curl -X GET http://localhost:8080/greet          
#output:  {"message":"Hola World!"}
```

## Retrieve metrics

```
# Prometheus format with exemplars

curl -s -X GET http://localhost:8080/observe/metrics
# TYPE counterForPersonalizedGreetings_total counter
# HELP counterForPersonalizedGreetings_total
counterForPersonalizedGreetings_total 2 # {trace_id="78e61eed351f4c9d"} 1 1617812495.016000
. . .
# TYPE timerForGets_mean_seconds gauge
timerForGets_mean_seconds 0.005772598385062112 # {trace_id="b22f13c37ba8b879"} 0.001563945 1617812578.687000
# TYPE timerForGets_max_seconds gauge
timerForGets_max_seconds 0.028018165 # {trace_id="a1b127002725143c"} 0.028018165 1617812467.524000
```
The exemplars contain `trace_id` values tying them to specific samples.
Note that the exemplar for the counter refers to the most recent update to the counter. 

For the timer, the value for the `max` is exactly the same as the value for its exemplar, 
because the `max` value has to come from at least one sample. 
In contrast, Helidon calculates the `mean` value from possibly multiple samples. The exemplar for 
`mean` is a sample with value as close as that of other samples to the mean. 

## Browse the Jaeger traces
If you started the Jaeger server, visit `http://localhost:16686`, select the `hello-world` service,
and run a search to see the spans your Helidon application reported using OpenTelemetry OTLP.
You can compare the trace IDs in the Jaeger display to those in the metrics output.

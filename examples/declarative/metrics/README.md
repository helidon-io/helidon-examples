Declarative Server Example with Metrics
---

This example shows how to use Helidon declarative with metrics.
This example uses WebServer endpoint, as it is easy to invoke the endpoints to show the behavior.

The example can be built using GraalVM native image as well.

# Running as jar

Build this application:
```shell
mvn clean package
```

Run from command line:
```shell
java -jar target/helidon-examples-declarative-metrics.jar
```

Expected output should be similar to the following:
```text
2026.01.09 12:32:01.998 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.09 12:32:02.237 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Metrics, Observe, Registry, WebServer]
2026.01.09 12:32:02.237 INFO [0x26c1825a] http://0.0.0.0:8080 bound for socket '@default'
2026.01.09 12:32:02.241 INFO Started all channels in 6 milliseconds. 329 milliseconds since JVM startup. Java 21.0.3+7-LTS-jvmci-23.1-b37
Server started on: http://localhost:8080/hello
```

# Running as native image
You must use GraalVM with native image installed as your JDK,
or you can specify an environment variable `GRAALVM_HOME` that points
to such an installation.

Build this application:
```shell
mvn clean package -Pnative-image
```

Run from command line:
```shell
./target/helidon-examples-declarative-metrics 
```

Expected output should be the same as when starting regular Java
```text
2026.01.09 12:56:28.791 INFO Logging at runtime configured using classpath: /logging.properties
2026.01.09 12:56:28.798 INFO Helidon SE 4.4.0-SNAPSHOT features: [Config, Encoding, Media, Metrics, Observe, Registry, WebServer]
2026.01.09 12:56:28.798 INFO [0x55b4e273] http://0.0.0.0:8080 bound for socket '@default'
2026.01.09 12:56:28.799 INFO Started all channels in 1 milliseconds. 9 milliseconds since JVM startup. Java 21.0.3+7-LTS-jvmci-23.1-b37
Server started on: http://localhost:8080/hello
```

# Application sources

There are three metered elements defined in the `MetricsEndpoint` service.

## Counter

The method `counted` is annotated with `@Metrics.Counted` with additional tags.

The resulting metric will be:
- `Name`: `MetricsEndpoint.counted`
- `Tags`:
  - `application=MetricsExample` - from class `@Metrics.Tag`
  - `endpoint=MetricsEndpoint` - from class `@Metrics.Tag`
  - `location=method` - from tag defined in `@Metrics.Counted` on the method

## Timer

The method `timed` is annotated with `@Metrics.Timed` with an explicit name.

The resulting metric will be:
- `Name`: `my-timed-metric` - from the method annotation, note the `absoluteName=true`, so there is no type prefix
- `Tags`:
    - `application=MetricsExample` - from class `@Metrics.Tag`
    - `endpoint=MetricsEndpoint` - from class `@Metrics.Tag`

## Gauge

The method `gaugeValue` is annotated with `@Metrics.Gauge`

The resulting metric will be:
- `Name`: `MetricsEndpoint.gaugeValue`
- `Tags`:
    - `application=MetricsExample` - from class `@Metrics.Tag`
    - `endpoint=MetricsEndpoint` - from class `@Metrics.Tag`
- `unit`: `bytes` (can be seen in text output of the `/observe/metrics` endpoint)


# Exercising the application

## Meters

Each application endpoint (from `MetricsEndpoint` type) modifies a metric

### Counted

Command:
```shell
curl -i -H "Accept: text/plain" -i http://localhost:8080/hello/counted
```
Expected output:
```
HTTP/1.1 200 OK
Date: Fri, 9 Jan 2026 12:48:11 +0100
Connection: keep-alive
Content-Length: 7
Content-Type: text/plain

counted
```

### Timed

Command:
```shell
curl -i -H "Accept: text/plain" -i http://localhost:8080/hello/timed
```
Expected output:
```
HTTP/1.1 200 OK
Date: Fri, 9 Jan 2026 12:48:36 +0100
Connection: keep-alive
Content-Length: 5
Content-Type: text/plain

timed
```

### Gauge

To set the gauge value:

Command:
```shell
curl -i http://localhost:8080/hello/gauge?value=42
```
Expected output:
```
HTTP/1.1 200 OK
Date: Fri, 9 Jan 2026 12:49:39 +0100
Connection: keep-alive
Content-Length: 15
Content-Type: text/plain; charset=UTF-8

gauge set to 42
```

## Observe metrics

The final endpoint is a built-in endpoint brought by the following dependency:

```xml
<dependency>
    <groupId>io.helidon.webserver.observe</groupId>
    <artifactId>helidon-webserver-observe-metrics</artifactId>
</dependency>
```

To read values in JSON and pretty printed, you can use the following command (`json_pp` is a command line utility for pretty printing JSON, can be omitted):

Command:
```shell
curl -H "Accept: application/json" http://localhost:8080/observe/metrics | json_pp
```
Expected output:
```
{
   "application" : {
      "MetricsEndpoint.counted;application=MetricsExample;endpoint=MetricsEndpoint;location=method" : 1,
      "MetricsEndpoint.gaugeValue;application=MetricsExample;endpoint=MetricsEndpoint" : 42,
      "my-timed-metric" : {
         "count;application=MetricsExample;endpoint=MetricsEndpoint" : 1,
         "elapsedTime;application=MetricsExample;endpoint=MetricsEndpoint" : 8.81e-06,
         "max;application=MetricsExample;endpoint=MetricsEndpoint" : 8.81e-06,
         "mean;application=MetricsExample;endpoint=MetricsEndpoint" : 8.81e-06,
         "p0.5;application=MetricsExample;endpoint=MetricsEndpoint" : 8.704e-06,
         "p0.75;application=MetricsExample;endpoint=MetricsEndpoint" : 8.704e-06,
         "p0.95;application=MetricsExample;endpoint=MetricsEndpoint" : 8.704e-06,
         "p0.98;application=MetricsExample;endpoint=MetricsEndpoint" : 8.704e-06,
         "p0.999;application=MetricsExample;endpoint=MetricsEndpoint" : 8.704e-06,
         "p0.99;application=MetricsExample;endpoint=MetricsEndpoint" : 8.704e-06
      }
   },
   "vendor" : {
      "requests.count" : 5
   }
}
```

All three metrics are visible (if we called all three endpoint mentioned above). Note that the non-gauge metrics will not be created unless the endpoint is called at least once.

The default output of the metrics observability endpoint is Prometheus text format, i.e.:

Command:
```shell
curl http://localhost:8080/observe/metrics
```
Expected output:
```
# HELP MetricsEndpoint_gaugeValue_bytes Metrics.Gauge annotation on method MetricsEndpoint.gaugeValue()
# TYPE MetricsEndpoint_gaugeValue_bytes gauge
MetricsEndpoint_gaugeValue_bytes{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",} 42.0
# HELP my_timed_metric_seconds_max Metrics.Timed annotation on method MetricsEndpoint.timed()
# TYPE my_timed_metric_seconds_max gauge
my_timed_metric_seconds_max{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",} 8.81E-6
# HELP my_timed_metric_seconds Metrics.Timed annotation on method MetricsEndpoint.timed()
# TYPE my_timed_metric_seconds summary
my_timed_metric_seconds{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",quantile="0.5",} 0.0
my_timed_metric_seconds{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",quantile="0.75",} 0.0
my_timed_metric_seconds{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",quantile="0.95",} 0.0
my_timed_metric_seconds{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",quantile="0.98",} 0.0
my_timed_metric_seconds{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",quantile="0.99",} 0.0
my_timed_metric_seconds{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",quantile="0.999",} 0.0
my_timed_metric_seconds_count{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",} 1.0
my_timed_metric_seconds_sum{application="MetricsExample",endpoint="MetricsEndpoint",scope="application",} 8.81E-6
# HELP requests_count_total Each request (regardless of HTTP method) will increase this counter
# TYPE requests_count_total counter
requests_count_total{scope="vendor",} 6.0
# HELP MetricsEndpoint_counted_none_total Metrics.Counted annotation on method MetricsEndpoint.counted()
# TYPE MetricsEndpoint_counted_none_total counter
MetricsEndpoint_counted_none_total{application="MetricsExample",endpoint="MetricsEndpoint",location="method",scope="application",} 1.0
```
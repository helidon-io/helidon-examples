Log4j Example
---

This example shows how to use log4j with MDC (`ThreadContext`)
 using Helidon API.
 
The example moves all Java Util Logging to log4j.

GraalVM Native Image is not supported in Helidon 27. Run this example on the JVM as described below.

# Running as jar

Build this application:
```shell
mvn clean package
```

Run from command line:
```shell
java -jar target/helidon-examples-logging-log4j.jar
```

Expected output should be similar to the following:
```text
15:44:48.596 INFO  [main] io.helidon.examples.logging.log4j.Main - Starting up "startup"
15:44:48.598 INFO  [main] io.helidon.examples.logging.log4j.Main - Using System logger "startup"
15:44:48.600 INFO  [pool-2-thread-1] io.helidon.examples.logging.log4j.Main - Running on another thread "propagated"
15:44:48.704 INFO  [features-thread] io.helidon.common.features.HelidonFeatures - Helidon 4.0.0-SNAPSHOT features: [Config, Encoding, Media, WebServer] ""
15:44:48.801 INFO  [main] io.helidon.webserver.LoomServer - Started all channels in 12 milliseconds. 746 milliseconds since JVM startup. Java 20.0.1+9-29 "propagated"
```

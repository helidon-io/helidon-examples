JUL Example
---

This example shows how to use Java Util Logging with MDC
 using Helidon API.
 
GraalVM Native Image is not supported in Helidon 27. Run this example on the JVM as described below.

# Running as jar

Build this application:
```shell
mvn clean package
```

Run from command line:
```shell
java -jar target/helidon-examples-logging-jul.jar
```

Expected output should be similar to the following:
```text
2020.11.19 15:37:28 INFO io.helidon.logging.jul.JulProvider Thread[#1,main,5,main]: Logging at initialization configured using classpath: /logging.properties ""
2020.11.19 15:37:28 INFO io.helidon.examples.logging.jul.Main Thread[main,5,main]: Starting up "startup"
2020.11.19 15:37:28 INFO io.helidon.examples.logging.jul.Main Thread[pool-1-thread-1,5,main]: Running on another thread "propagated"
2020.11.19 15:37:28 INFO io.helidon.common.features.HelidonFeatures Thread[#23,features-thread,5,main]: Helidon 4.0.0-SNAPSHOT features: [Config, Encoding, Media, WebServer] ""
2020.11.19 15:37:28 INFO io.helidon.webserver.LoomServer Thread[#1,main,5,main]: Started all channels in 46 milliseconds. 577 milliseconds since JVM startup. Java 20.0.1+9-29 "propagated"
```

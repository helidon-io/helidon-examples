# Comments As a Service

This application runs fault tolerance service on port 8079.

## Build and run

```bash
mvn package
java -jar target/helidon-examples-webserver-fault-tolerance.jar
```

The output will be like:

```bash
[Wed Sep 26 16:01:16 CEST 2023] INFO: io.helidon.common.LogConfig doConfigureLogging - Logging at initialization configured using classpath: /logging.properties 
[Wed Sep 26 16:01:17 CEST 2023] INFO: io.helidon.common.HelidonFeatures features - Helidon SE 3.2.6-SNAPSHOT features: [Config, Fault Tolerance, Tracing, WebServer]
[Wed Sep 26 16:01:17 CEST 2023] INFO: io.helidon.webserver.NettyWebServer lambda$start$9 - Channel '@default' started: [id: 0xb11f6086, L:/[0:0:0:0:0:0:0:0]:8079] 
Server started on http://localhost:8079
```

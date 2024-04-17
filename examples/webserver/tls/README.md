# Comments As a Service

This application starts a web server with TLS support on the port 8081.

## Build and run

```shell
mvn package
java -jar target/helidon-examples-webserver-tls.jar
```

The output will be like:

```shell
[Wed Sep 27 16:11:25 CEST 2023] INFO: io.helidon.common.LogConfig doConfigureLogging - Logging at initialization configured using classpath: /logging.properties 
[Wed Sep 27 16:11:25 CEST 2023] INFO: io.helidon.common.HelidonFeatures features - Helidon SE 3.2.6-SNAPSHOT features: [Config, Tracing, WebServer]
[Wed Sep 27 16:11:25 CEST 2023] INFO: io.helidon.webserver.NettyWebServer lambda$start$9 - Channel '@default' started: [id: 0xb28f94e2, L:/[0:0:0:0:0:0:0:0]:8080] with TLS  
Started config based WebServer on http://localhost:8080
[Wed Sep 27 16:11:25 CEST 2023] INFO: io.helidon.webserver.NettyWebServer lambda$start$9 - Channel '@default' started: [id: 0x8aed396c, L:/[0:0:0:0:0:0:0:0]:8081] with TLS  
Started builder based WebServer on https://localhost:8081
```

The http://localhost:8080 displays static content (in browser) and https://localhost:8081 displays 'Hello!' after accepting 
invalid certificate (in browser)

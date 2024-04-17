# Tutorial Server

This application demonstrates various WebServer use cases together and in its complexity.

## Build and run

```shell
mvn package
java -jar target/helidon-examples-webserver-tutorial.jar
```

run
```shell
curl -X POST http://localhost:8080/mgmt/shutdown
```
in order to stop the server
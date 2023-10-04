# Tutorial Server

This application demonstrates various WebServer use cases together and in its complexity.

## Build and run

```bash
mvn package
java -jar target/helidon-examples-webserver-tutorial.jar
```

run
```bash
curl -X POST http://localhost:8080/mgmt/shutdown
```
in order to stop the server
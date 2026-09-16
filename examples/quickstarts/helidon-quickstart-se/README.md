# Helidon Quickstart SE Example

This project implements a simple Hello World REST service using Helidon SE.

## Build and run

```shell
mvn package
java -jar target/helidon-quickstart-se.jar
```

## Exercise the application

```shell
curl -X GET http://localhost:8080/greet
#Output: {"message":"Hello World!"}

curl -X GET http://localhost:8080/greet/Joe
#Output: {"message":"Hello Joe!"}

curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Hola"}' http://localhost:8080/greet/greeting

curl -X GET http://localhost:8080/greet/Jose
#Output: {"message":"Hola Jose!"}
```

## Try health and metrics

```shell
curl -s -X GET http://localhost:8080/observe/health
#Output: {"outcome":"UP",...

# Prometheus Format
curl -s -X GET http://localhost:8080/observe/metrics
# TYPE base:gc_g1_young_generation_count gauge

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:8080/observe/metrics
#Output: {"base":...

```

## Build the Docker Image

```shell
docker build -t helidon-quickstart-se .
```

## Start the application with Docker

```shell
docker run --rm -p 8080:8080 helidon-quickstart-se:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```shell
kubectl cluster-info                # Verify which cluster
kubectl get pods                    # Verify connectivity to cluster
kubectl create -f app.yaml   # Deply application
kubectl get service helidon-quickstart-se  # Get service info
```

## Native Image

GraalVM Native Image is not supported in Helidon 27. Use the JVM or jlink instructions in this example.

## Build a Java Runtime Image using jlink

You can build a custom Java Runtime Image (JRI) containing the application jars and the JDK modules 
on which they depend. This image also:

* Creates an ahead-of-time (AOT) cache by default to reduce startup time.
* Contains a customized `start` script to simplify AOT cache usage and support debug and test modes.
 
You can build a custom JRI in two different ways:
* Local
* Using Docker


### Local build

```shell
# build the JRI
mvn package -Pjlink-image
```

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-jlink-image
 for more information.

Start the application:

```shell
./target/helidon-quickstart-se-jri/bin/start
```

### Multi-stage Docker build

Build the JRI as a Docker Image

```shell
docker build -t helidon-quickstart-se-jri -f Dockerfile.jlink .
```

Start the application:

```shell
docker run --rm -p 8080:8080 helidon-quickstart-se-jri:latest
```

See the start script help:

```shell
docker run --rm helidon-quickstart-se-jri:latest --help
```

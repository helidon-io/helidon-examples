# Helidon SE integration with Neo4J example

## Build and run

Bring up a Neo4j instance via Docker

```shell
docker run --publish=7474:7474 --publish=7687:7687 -e 'NEO4J_AUTH=neo4j/secret'  neo4j:4.0
```

Goto the Neo4j browser and play the first step of the movies graph: [`:play movies`](http://localhost:7474/browser/?cmd=play&arg=movies).

Build and run
```shell
mvn package
java -jar target/helidon-examples-integration-neo4j-se.jar  
```

Then access the rest API like this:

````
curl localhost:8080/api/movies
````

#Health and metrics

Heo4jSupport provides health checks and metrics reading from Neo4j.

To enable them add to routing:
```java
// metrics
Neo4jMetricsSupport.builder()
        .driver(neo4j.driver())
        .build()
        .initialize();
// health checks
HealthSupport health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .addReadiness(Neo4jHealthCheck.create(neo4j.driver()))
                .build();

return Routing.builder()
        .register(health)                   // Health at "/health"
        .register(metrics)                  // Metrics at "/metrics"
        .register(movieService)
        .build();
```
and enable them in the driver:
```yaml
  pool:
    metricsEnabled: true
```


````
curl localhost:8080/health
````

````
curl localhost:8080/metrics
````

## Build a native image with GraalVM

GraalVM allows you to compile your programs ahead-of-time into a native
 executable. See https://www.graalvm.org/docs/reference-manual/aot-compilation/
 for more information.

Download Graal VM at https://www.graalvm.org/downloads. We recommend
version `20.1.0` or later.

```
# Setup the environment
export GRAALVM_HOME=/path
# build the native executable
mvn package -Pnative-image
```

You can also put the Graal VM `bin` directory in your PATH, or pass
 `-DgraalVMHome=/path` to the Maven command.

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-native-image
 for more information.

Start the application:

```
./target/helidon-integrations-heo4j-se
```

## Build a Java Runtime Image using jlink

You can build a custom Java Runtime Image (JRI) containing the application jars and the JDK modules 
on which they depend. This image also:

* Enables Class Data Sharing by default to reduce startup time. 
* Contains a customized `start` script to simplify CDS usage and support debug and test modes. 
 
```
# build the JRI
mvn package -Pjlink-image
```

See https://github.com/oracle/helidon-build-tools/tree/master/helidon-maven-plugin#goal-jlink-image
 for more information.

Start the application:

```
./target/helidon-integrations-heo4j-se-jri/bin/start
```

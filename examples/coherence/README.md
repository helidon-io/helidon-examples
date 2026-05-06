# Helidon Example Coherence

Sample Helidon SE applications that uses Coherence CE as a cache for application data.

The example sets Coherence WKA to `127.0.0.1` in `Main.java` and `MainTest.java`
to force a local unicast cluster. Remove the following line from both files if
you need Coherence to form a multicast cluster:

```java
System.setProperty("coherence.wka", "127.0.0.1");
```

## Build and run

```shell
mvn package
java -jar target/helidon-examples-coherence.jar
```

## Exercise the application

```shell
curl -X POST -H "Content-Type: application/json" \
 -d '{"ssn" : "123-45-6789", "firstName" : "Frank", "lastName" : "Helidon", "dateOfBirth" : "02/14/2019"}' \
  http://localhost:8080/creditscore
```

You'll notice a short delay as the application computes the credit score.
Now repeat the same request. You'll see the score is returned instantly as it is retrieved from the cache.

# helidon-creditscore-mp

Sample Helidon MP applications that uses Coherence CE as a cache for application data.

## Build and run

```bash
mvn package
java -jar -Dcoherence.ttl=0 -Dcoherence.localhost=127.0.0.1 -Dcoherence.wka=127.0.0.1 target/helidon-creditscore-mp.jar
```

## Exercise the application

```
curl -X POST -H "Content-Type: application/json" \
 -d '{"ssn" : "123-45-6789", "firstname" : "Frank", "lastname" : "Helidon", "dateofbirth" : "01/30/2018"}' \
  http://localhost:8080/creditscore
```

You'll notice a short delay as the application computes the credit score.
Now repeat the same request. You'll see the score is returned instantly
as it is retrieved from the cache.

## Try health

```
curl -s -X GET http://localhost:8080/health
{"outcome":"UP",...
```

# Jedis Integration Example

## Start Redis

```shell
docker run --rm --name redis -d -p 6379:6379 redis
```
## Build and run

With Java:
```shell
mvn package
java -jar target/helidon-examples-integrations-cdi-jedis.jar
```

Try the endpoint:
```shell
curl -X PUT  -H "Content-Type: text/plain" http://localhost:8080/jedis/foo -d 'bar'
curl http://localhost:8080/jedis/foo
```

Stop the docker container:
```shell
docker stop redis
```

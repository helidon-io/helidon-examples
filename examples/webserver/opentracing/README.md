# Opentracing Example Application

## Start Zipkin

With Docker:
```shell
docker run --name zipkin -d -p 9411:9411 openzipkin/zipkin
```

With Java:
```shell
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

## Build and run

```shell
mvn package
java -jar target/helidon-examples-webserver-opentracing.jar
```

Try the endpoint:
```shell
curl http://localhost:8080/test
```

Then check out the traces at http://localhost:9411.

Stop the docker container:
```shell
docker stop zipkin
```

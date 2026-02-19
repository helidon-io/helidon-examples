# TODO Demo Application

This application implements TodoMVC[https://todomvc.com] with two microservices
implemented with Helidon MP and Helidon SE.

## Build

```shell
mvn package
```

## Run

Start the database:
```shell
docker run -d \
    --name cassandra \
    -p 9042:9042 \
    cassandra:3.11.2
```

Initialize the database:
```shell
docker exec cassandra cqlsh -e "CREATE KEYSPACE backend WITH REPLICATION = {
    'class' : 'SimpleStrategy',
    'replication_factor' : 1
}"

docker exec cassandra  cqlsh -e "CREATE TABLE backend.backend (
    id ascii,
    user ascii,
    message ascii,
    completed Boolean,
    created timestamp,
    PRIMARY KEY (id)
);"

docker exec cassandra  cqlsh -e "select * from backend.backend"
```

Or, if the container already exists:
```shell
docker start cassandra
```

Start the tracing backend:
```shell
docker run -d \
  --name jaeger \
  -p 14250:14250 \
  -p 16686:16686 \
  cr.jaegertracing.io/jaegertracing/jaeger:2.10.0
```

Or, if the container already exists:
```shell
docker start jaeger
```

Start the services:
```shell
java -jar backend/target/helidon-examples-todo-backend.jar &
java -jar frontend/target/helidon-examples-todo-frontend.jar &
```

### Exercise the application

- Open http://localhost:8080 in your browser
- Login with a Google account
- Add some TODO entries
- Check-out the traces at http://localhost:9411

## Stop

```shell
kill %1 %2
docker rm -f jaeger cassandra
```

## HTTP proxy

If you want to run behind an HTTP proxy:

```shell
export security_providers_0_google_dash_login_proxy_dash_host=proxy.acme.com
export security_providers_0_google_dash_login_proxy_dash_port=80
```

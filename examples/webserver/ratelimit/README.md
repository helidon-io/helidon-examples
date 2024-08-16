# Helidon SE Rate Limiting Example

This example demonstrates two mechanisms to perform request rate limiting in a Helidon SE application.
The mechanisms shown here will also work with Helidon MP.

The example does the following:

1. Sets WebServer's `max-concurrent-requests` to 10
2. Uses a `java.util.concurrent.Semaphore` to limit the number of requests processed concurrently to 5

The server has one endpoint: `sleep` that sleeps for the specified number of seconds. The
sleep operation is protected by a `Semaphore` to allow no more than five requests to be processed concurrently.

These values are configured in `application.yaml`

## Build and run

Build and start the server:
```shell
mvn package
java -jar target/helidon-examples-webserver-ratelimit.jar
```

The application logs the limits it is using:
```
2024.08.15 14:34:27.694          Application rate limit is 5
2024.08.15 14:34:27.694 WebServer maxConcurrentRequests is 10
```

## Exercise the application

Send a burst of 15 concurrent requests to the server:
```shell
curl -s  \
  --noproxy '*' \
  -o /dev/null \
  --parallel \
  --parallel-immediate \
  -w "%{http_code}\n" \
  "http://localhost:8080/sleep/3?c=[1-15]"
```

When the 15 concurrent requests hit the server:

* 5 of the requests will be rejected with a 503 because they exceed the server limit of 10
* 10 of the requests will be accepted by the server
* The first 5 of those will return a 200 after sleeping for 3 seconds
* The next 5 requests will then be processed and return a 200 after sleeping for 3 more seconds.

You will see this in the output:
```
503
503
503
503
503
# three second pause
200
200
200
200
200
# three second pause
200
200
200
200
200
200
```
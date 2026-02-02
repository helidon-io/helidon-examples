# Helidon SE Concurrency Limits Example

This example demonstrates the Concurrency Limits feature of Helidon WebServer. 
For other approaches for rate limiting see the [Rate Limit Example](../ratelimit).

The Concurrency Limits feature provides mechanisms to limit concurrent execution of incoming requests.
Currently three algorithms are supported:

1. Fixed: A semaphore based limit that supports queuing for a permit and timeout on the queue.
2. AIMD: Additive-increase/multiplicative-decrease. Uses a feedback control algorithm that combines linear growth of the request limit when there is no congestion with an exponential reduction when congestion is detected.
2. Throughput: throttles to a specified number of requests per second.

The example does the following:

1. Defines three ports for accepting incoming requests: 8080, 8088 and 8089
2. 8080 is configured to use the Fixed concurrency limit algorithm
3. 8088 is configured to use the AIMD concurrency limit algorithm
4. 8089 is configured to use the throughput concurrency limit algorithm

The server has three endpoints: `fixed/sleep`, `aimd/sleep` and `throughput/sleep`.
All sleep for the specified number of seconds to simulate a slow workload.

These values are configured in [`application.yaml`](./src/main/resources/application.yaml)

## Build and run

Build and start the server:
```shell
mvn package
java -jar target/helidon-examples-webserver-concurrencylimits.jar
```

## Exercise the application

### Fixed Concurrency Limit

The server is configured to process 6 requests concurrently with a backlog queue of depth 4. Therefore it can handle bursts of up to 10 requests at a time.

Send a burst of 15 concurrent requests to the `fixed/sleep` endpoint (each requesting a sleep of 3 seconds):
```shell
curl -s  \
  --noproxy '*' \
  -o /dev/null \
  --parallel \
  --parallel-immediate \
  -w "%{http_code}\n" \
  "http://localhost:8080/fixed/sleep/3?c=[1-15]"
```

When the 15 concurrent requests hit the server:

* 5 of the requests are rejected with a 503 because they exceed the size of the number of concurrent requests limit (6) plus the size of the queue (4).
* 10 of the requests are accepted by the server
* The first 6 of those return a 200 after sleeping for 3 seconds
* The next 4 requests are then processed from the queue and return a 200 after sleeping for 3 more seconds.

You will see this in the output:
```
503
503
503
503
503
# Three second pause
200
200
200
200
200
200
# Three second pause
200
200
200
200
```

### AIMD Concurrency Limit

The AIMD limiter is [adaptive](https://en.wikipedia.org/wiki/Additive_increase/multiplicative_decrease)
and will adjust according to the workload and responsiveness of the server.
If the server successfully processes requests then the limiter will increase limits (up to a max limit).
If the server starts to fail or timeout requests then the limiter will reduce limits (down to a min limit).

In this example we set the initial and minimum limit to 6 and a max limit of 10 concurrent requests.
We configure the timeout as 3 seconds. So requests that can't be serviced within 3 seconds fail -- which can lead to a reduction of the current limit.

First execute 15 concurrent requests, each sleeping for 3 seconds.

```shell
curl -s  \
  --noproxy '*' \
  -o /dev/null \
  --parallel \
  --parallel-immediate \
  -w "%{http_code}\n" \
  "http://localhost:8088/aimd/sleep/3?c=[1-15]"
```

This will process 6 request (the currently configured limit), and fail 9:

```
503
503
503
503
503
503
503
503
503
# Pause for 3 seconds
200
200
200
200
200
200
```

Repeat the curl command and you will see the same results. Because we have the AIMD timeout set to 3 seconds, and our sleep operation is taking 3 seconds, the current limit is never increased.

Now repeat the curl command, but specify a sleep time of 2 seconds:

```shell
curl -s  \
  --noproxy '*' \
  -o /dev/null \
  --parallel \
  --parallel-immediate \
  -w "%{http_code}\n" \
  "http://localhost:8088/aimd/sleep/2?c=[1-15]"
```

As before the first invocation will process 6 requests. But as you
repeat the curl command you will see more requests are
successful until you hit the max concurrency limit of 10. Since
each request is now taking 2 seconds (and not 3), the limiter
is able to adapt and increase the current limit up to the maximum.

Now go back and run the curl command a few times using a sleep time
of 3, and you will see the limiter adapt again and lower the current limit.

### Throughput Concurrency Limit

The server is configured to process 1 request per second with a backlog queue
of depth 4 using the FIXED_RATE algorithm. et can handle bursts of up to 5 requests
at a time.

Send a burst of 15 concurrent requests to the `throughput/sleep` endpoint
(each requesting a sleep of 0 seconds). Run this command a couple of
times to reach a steady state.

```shell
curl -s  \
  --noproxy '*' \
  -o /dev/null \
  --parallel \
  --parallel-immediate \
  -w "%{http_code}\n" \
  "http://localhost:8089/throughput/sleep/3?c=[1-15]"
```

When the 15 concurrent requests hit the server:

* One request will be immediately handled by the server.
* 4 requests will be accepted and placed on the queue then processed at a rate of one request per second.
* The other requests will be rejected.

You should see something like this in the output:
```
200
503
503
503
503
503
503
503
503
503
503
# final four requests are processed at a rate of about one per second
200
200
200
200
```

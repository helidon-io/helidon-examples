Declarative Scheduling Example
---

This example shows how to use Helidon declarative to create a scheduled task.

The example can be built using GraalVM native image as well.

# Description

The class `ScheduledTask` contains a single scheduled method `refreshTask`.
This will be triggered every 5 seconds by default, overridden through configuration
to 2 seconds.

An alternative annotation is `Scheduling.FixedRate`, which allows for scheduling using a specified duration (i.e. `PT5S` for 5
seconds).

The `Main` class will start the example, wait until the task gets executed 3 times, and exit.

# Running as jar

Build this application:

```shell
mvn clean package
```

Run from command line:

```shell
java -jar target/helidon-examples-declarative-scheduling.jar
```

Expected output should be similar to the following:

```text
2026.01.06 14:54:32.429 INFO Logging at runtime configured using classpath: /logging.properties
1
2
3
2026.01.06 14:54:38.605 INFO Shutdown requested by JVM shutting down
2026.01.06 14:54:38.606 INFO Shutdown finished
```

# Running as native image

You must use GraalVM with native image installed as your JDK,
or you can specify an environment variable `GRAALVM_HOME` that points
to such an installation.

Build this application:

```shell
mvn clean package -Pnative-image
```

Run from command line:

```shell
./target/helidon-examples-declarative-scheduling 
```

Expected output should be the same as when starting regular Java

```text
2026.01.06 14:56:50.562 INFO Logging at runtime configured using classpath: /logging.properties
1
2
3
2026.01.06 14:56:56.567 INFO Shutdown requested by JVM shutting down
2026.01.06 14:56:56.567 INFO Shutdown finished
```

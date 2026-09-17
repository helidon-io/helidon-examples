<p align="center">
    <img src="../etc/images/Primary_logo_blue.png" height="180">
</p>

# Helidon Examples

Welcome to the Helidon Examples! If this is your first experience with
Helidon we recommend you start with our
[quickstart](quickstarts/helidon-quickstart-se/README.md).
That will quickly get you going with your first Helidon application.

After that you can come back here and dig into the examples. Use the
`helidon-27.x` branch for Helidon 27 examples:

```shell
git clone https://github.com/helidon-io/helidon-examples.git
cd helidon-examples
git checkout helidon-27.x
```

Our examples are Maven projects and can be built and run with
Java 27 or newer -- so make sure you have those:

```shell
java -version
mvn -version
```

# Building an Example

Each example has a `README` that you will follow. To build most examples
just `cd` to the directory and run `mvn package`:

```shell
cd examples/quickstarts/helidon-quickstart-se
mvn package
```

Usually the example will produce an application jar file that you can run:

```shell
java -jar target/example-name.jar
```

But always see the example's `README` for details.

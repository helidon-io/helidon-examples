
# Helidon Examples

Examples for Helidon 2.

[Helidon 4 Examples](https://github.com/helidon-io/helidon/tree/main/examples) and [Helidon 2 Examples](https://github.com/helidon-io/helidon/tree/helidon-2.x/examples) are in the primary Helidon repository.

## How to Run

To build and run Helidon 2 examples you need:

* Java 11 or later
* Maven 3.6.1 or later

Then:

```
git clone https://github.com/helidon-io/helidon-examples.git
cd helidon-examples
git checkout helidon-2.x
mvn clean install
```

### How Repository is Organized

| Branch        | Description |
| ------------- |-------------|
| helidon-3.x   | Examples for the current release of Helidon 3 |
| helidon-2.x   | Examples for the current release of Helidon 2 |
| dev-3.x       | Development branch for Helidon 3 release currently under development |
| dev-2.x       | Development branch for Helidon 2 release currently under development |

| Tags          | Description |
| ------------- |-------------|
| N.N.N         | Examples for a specific version of Helion |

To checkout examples for the most recent release of Helidon 2:

```
git checkout helidon-2.x
```

To checkout examples for a specific release of Helidon:

```
git checkout tags/2.X.Y
```

## Documentation

Each example has a README that contains additional details for building and running the example.

## Help

* See the [Helidon FAQ](https://github.com/oracle/helidon/wiki/FAQ)
* Ask questions on Stack Overflow using the [helidon tag](https://stackoverflow.com/tags/helidon)
* Join us on Slack: [#helidon-users](http://slack.helidon.io)

## Contributing

This project welcomes contributions from the community. Before submitting a pull request, please [review our contribution guide](./CONTRIBUTING.md)

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2017, 2024 Oracle and/or its affiliates.

Released under [Apache License 2.0](./LICENSE.txt).

## Third Party Attributions

Developers choosing to distribute a binary implementation of this project are responsible for obtaining and providing all required licenses and copyright notices for the third-party code used in order to ensure compliance with their respective open source licenses.

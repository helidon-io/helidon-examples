# Helidon Examples

Examples for Helidon 27.

## How to Run

To build and run Helidon 4 examples you need:

* Java 25 or later
* Maven 3.8.* or later

Then:

```
git clone https://github.com/helidon-io/helidon-examples.git
cd helidon-examples
git checkout helidon-27.x
mvn clean install
```

### How Repository is Organized

| Branch      | Used By    | Modified By      | Description                                               |
| ----------- |------------|------------------|--------------------------------------------------|
| helidon-N.x | Customers  | Release workflow | Latest examples for major version N of Helidon   |
| dev-N.x     | Developers | PRs              | Examples under development for major version N of Helidon |

| Tags  | Used By    | Created By       | Description                                         |
|-------|------------|------------------|-----------------------------------------------------|
| N.N.N | Customers  | Release workflow | Released examples for a specific version of Helidon |

To checkout examples for the most recent release of Helidon 27:

```
git checkout helidon-27.x
```

To checkout examples for a specific release of Helidon:

```
git checkout tags/27.0.0
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

Released under [Apache License 2.0](./LICENSE.txt).

## Third Party Attributions

Developers choosing to distribute a binary implementation of this project are responsible for obtaining and providing all required licenses and copyright notices for the third-party code used in order to ensure compliance with their respective open source licenses.

## Changes since 4.x

Helidon is now separated into multiple repositories, and examples in this repo are only for modules in Helidon "core":

(some of these repositories will be made public later):

- Helidon "core": https://github.com/helidon-io/helidon - core features of Helidon, Helidon SE and Helidon Declarative
- Helidon Extensions: https://github.com/helidon-io/helidon-extensions - set of extensions for Helidon "core", includes examples
- Helidon MicroProfile: https://github.com/helidon-io/helidon-microprofile - MicroProfile implementations, includes examples

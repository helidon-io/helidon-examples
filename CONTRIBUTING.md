
# Contributing

We welcome your contributions! There are multiple ways to contribute.

## Discussions

Join us at [#helidon-users](http://slack.helidon.io) and participate in discussions.

## Opening Issues

For bugs or enhancement requests, please file a [GitHub issue](https://github.com/helidon-io/helidon/issues) unless it's
security related. When filing a bug remember that the better written the bug is,
the more likely it is to be fixed. If you think you've found a security
vulnerability, do not raise a GitHub issue and follow the instructions in our
[security policy](./SECURITY.md).

## Contributing code

We welcome your code contributions. Before submitting code via a pull request,
you will need to have signed the [Oracle Contributor Agreement][OCA] (OCA) and
your commits need to include the following line using the name and e-mail
address you used to sign the OCA:

```text
Signed-off-by: Your Name <you@example.org>
```

This can be automatically added to pull requests by committing with `--sign-off`
or `-s`, e.g.

```text
git commit --signoff
```

Only pull requests from committers that can be verified as having signed the OCA
can be accepted.

## Pull request process

1. Ensure there is an issue created to track and discuss the fix or enhancement
   you intend to submit.
1. Fork this repository
1. Create a branch in your fork to implement the changes. We recommend using
   the issue number as part of your branch name, e.g. `1234-fixes`. Make sure to
   base your branch off of the corresponding `dev-N.x` branch (where N is the
   major Helidon version).
1. Submit the pull request. *Do not leave the pull request blank*. Explain exactly
   what your changes are meant to do and provide simple steps on how to validate
   your changes. Ensure that you reference the issue you created as well. Target
   your PR to the corresponding `dev-N.x` branch.
1. We will assign the pull request for review and running of PR validation workflow before it is merged.

Development work for examples is performed in the `dev-N.x` branches (where
`N` corresponds to a Helidon major version). For example, to make a change to
a Helidon 2 example you would use the `dev-2.x` branch.

## Code of Conduct

Follow the [Golden Rule](https://en.wikipedia.org/wiki/Golden_Rule). If you'd
like more specific guidelines, see the [Contributor Covenant Code of Conduct][COC].

[OCA]: https://oca.opensource.oracle.com
[COC]: https://www.contributor-covenant.org/version/1/4/code-of-conduct/

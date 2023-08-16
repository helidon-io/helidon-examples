
# Releasing Helidon Examples

These are the steps for doing a release of Helidon Examples. These steps
will use release 3.0.0 in examples. Of course you are not releasing
3.0.0, so make sure to change that release number to your release
number when copy/pasting.

# Overview

Helidon Examples releases mirror Helidon releases. So every time Helidon
is released, we do a release of examples that consumes that Helidon release.

The Helidon Examples release workflow is triggered when a change is pushed to
a branch that starts with `release-`. The release workflow performs
a test build of the examples against a released version of Helidon. It then
creates a release tag and updates the corresponding helidon-X.X branch.
Here is the overall procedure:

1. Create a local release branch from the corresponding dev branch.
2. Update the version of Helidon consumed by the examples. Typically this will be
   needed as the dev branch poms reference a snapshot version of Helidon.
3. Push release branch to upstream, release workflow runs
4. Verify tag and branch update performed by workflow by pulling the branch and tag and 
   building them.

# Steps in detail

```
# Set this to the version you are releasing. This should match the released version of Helidon
export VERSION="3.0.0"
```

1. Create local release branch

   ```
   # Checkout dev branch into release branch
   git fetch origin
   git checkout -b release-${VERSION} origin/dev-3.x
   git log  # Make sure you have what you think you have
   ```

2. Update Helidon version used by examples. This should be a released version of Helidon
   ```
   etc/scripts/updatehelidonversion.sh ${VERSION}
   ```
3. Commit and Push local release branch to upstream to trigger release workflow. 
   ```
   mvn clean install  # Do test build first
   git add .
   git commit
   git push origin release-${VERSION}
   ```
4. After workflow completes check status. Check for creation of tag. Check that `helidon-3.x`
   branch has been updated. 
   ```
   git fetch -t origin
   # Checkout and verify branch
   git checkout helidon-3.x 
   git rebase origin/helidon-3.x
   git log # Make sure it is what it should be
   mvn clean install
   # Checkout and veriy tag
   git checkout tags/${VERISON}
   git log # Make sure it is what it should be
   mvn clean install
   ```

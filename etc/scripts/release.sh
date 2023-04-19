#!/bin/bash
#
# Copyright (c) 2022, 2023 Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Path to this script
[ -h "${0}" ] && readonly SCRIPT_PATH="$(readlink "${0}")" || readonly SCRIPT_PATH="${0}"

# Load pipeline environment setup and define WS_DIR
. $(dirname -- "${SCRIPT_PATH}")/includes/pipeline-env.sh "${SCRIPT_PATH}" '../..'

# Setup error handling using default settings (defined in includes/error_handlers.sh)
error_trap_setup

readonly SCRIPT_DIR=$(dirname ${SCRIPT_PATH})

usage(){
    cat <<EOF

DESCRIPTION: Helidon Examples Release Script

USAGE:

$(basename ${0}) [ --build-number=N ] CMD

  --version=V
        Override the version to use.
        This trumps --build-number=N

  --help
        Prints the usage and exits.

  CMD:

    update_version
        Update the version in the workspace

    release_build
        Perform a release build
        This will create a local branch, deploy artifacts and push a tag

EOF
}

# parse command line args
ARGS=( "${@}" )
for ((i=0;i<${#ARGS[@]};i++))
{
    ARG=${ARGS[${i}]}
    case ${ARG} in
    "--version="*)
        VERSION=${ARG#*=}
        ;;
    "--help")
        usage
        exit 0
        ;;
    *)
        if [ "${ARG}" = "update_version" ] || [ "${ARG}" = "release_build" ] ; then
            readonly COMMAND="${ARG}"
        else
            echo "ERROR: unknown argument: ${ARG}"
            exit 1
        fi
        ;;
    esac
}

if [ -z "${COMMAND}" ] ; then
    echo "ERROR: no command provided"
    usage
    exit 1
fi

# For releases this should be a released version of Helidon
readonly HELIDON_VERSION=`cat ${WS_DIR}/pom.xml | grep "<helidon.version>" | cut -d">" -f 2 | cut -d"<" -f 1`

# Resolve FULL_VERSION of this project
if [ -z "${VERSION+x}" ]; then

    # get maven version
    MVN_VERSION=$(mvn ${MAVEN_ARGS} \
        -q \
        -f ${WS_DIR}/pom.xml \
        -Dexec.executable="echo" \
        -Dexec.args="\${project.version}" \
        --non-recursive \
        org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)

    # strip qualifier
    readonly VERSION="${MVN_VERSION%-*}"
    readonly FULL_VERSION="${VERSION}"
else
    readonly FULL_VERSION="${VERSION}"
fi

export FULL_VERSION
printf "\n%s: FULL_VERSION=%s\n\n" "$(basename ${0})" "${FULL_VERSION}"
printf "\n%s: HELIDON_VERSION=%s\n\n" "$(basename ${0})" "${HELIDON_VERSION}"

update_version(){
    # Update version
    echo "Updating version to ${FULL_VERSION}"
    mvn -e ${MAVEN_ARGS} -f ${WS_DIR}/pom.xml versions:set \
        -DgenerateBackupPoms=false \
        -DnewVersion="${FULL_VERSION}" \
        -DupdateMatchingVersions=false \
        -DprocessAllModules=true
}

# A release build of the examples consists of:
#
# 1. Merge helidon-N.x branch that we will push to at the end
# 2. Perform full test build against corresponding released Helidon version.
# 3. Create tag
# 4. Update "helidon-N.x" branch with latest
#
release_build(){
    echo "Starting release build for ${HELIDON_VERSION}"
    mvn --version
    java --version

    # Branch we will push this release to
    local LATEST_BRANCH="helidon-3.x"
    # Branch we do the release build in
    local GIT_BRANCH="release/${HELIDON_VERSION}"

    # Create a "release" remote that is the same as "origin"
    # We do this so the release branch can be in its own
    # namespace (based on the remote)
    local GIT_REMOTE=$(git config --get remote.origin.url)
    git remote add release "${GIT_REMOTE}" > /dev/null 2>&1 || \
    git remote set-url release "${GIT_REMOTE}"
    git fetch release ${LATEST_BRANCH}

    # Create a local branch to do the release build in
    # It's based on the dev branch
    git branch -D "${GIT_BRANCH}" > /dev/null 2>&1 || true
    git checkout -b "${GIT_BRANCH}"

    # Merge this branch (based on dev-3.x) with the
    # helidon-3.x branch to ensure helidon-3.x has
    # valid history when we push all this to it.
    git merge -s ours --no-ff release/${LATEST_BRANCH}

    # Now update the version to FULL_VERSION
    # TODO remove: we do not update the example project version.
    # Tags, etc are based on HELIDON_VERSION
    # update_version

    # Update scm/tag entry in the parent pom
    cat pom.xml | \
        sed -e s@'<tag>HEAD</tag>'@"<tag>${HELIDON_VERSION}</tag>"@g \
        > pom.xml.tmp
    mv pom.xml.tmp pom.xml

    # Git user info
    git config user.email || git config --global user.email "info@helidon.io"
    git config user.name || git config --global user.name "Helidon Robot"

    # Commit version changes
    git commit -a -m "Release ${HELIDON_VERSION} [ci skip]"

    # Run build as a sanity check
    ${SCRIPT_DIR}/copyright.sh
    ${SCRIPT_DIR}/checkstyle.sh
    mvn ${MAVEN_ARGS} -f ${WS_DIR}/pom.xml \
        clean install -e

    # Create and push a git tag
    git tag -f "${HELIDON_VERSION}"
    git push --force release refs/tags/"${HELIDON_VERSION}":refs/tags/"${HELIDON_VERSION}"

    # Update helidon-3.x branch with this release
    git push release HEAD:${LATEST_BRANCH}

    echo "======================"
    echo "Created tag:    ${HELIDON_VERSION}"
    echo "Updated branch: ${LATEST_BRANCH}"
    echo "======================"
}

# Invoke command
${COMMAND}

#!/bin/bash
#
# Copyright (c) 2022, 2024 Oracle and/or its affiliates.
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
. "$(dirname -- "${SCRIPT_PATH}")/includes/pipeline-env.sh" "${SCRIPT_PATH}" '../..'

# Setup error handling using default settings (defined in includes/error_handlers.sh)
error_trap_setup

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
readonly HELIDON_VERSION=$(cat "${WS_DIR}/pom.xml" | grep "<helidon.version>" | cut -d">" -f 2 | cut -d"<" -f 1)

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
printf "%s: FULL_VERSION=%s\n" "$(basename ${0})" "${FULL_VERSION}"
printf "%s: HELIDON_VERSION=%s\n\n" "$(basename ${0})" "${HELIDON_VERSION}"

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
# 2 Create tag
# 3. Update "helidon-N.x" branch with latest
#
# A release build does not modify the source in any way. It assumes the
# Helidon version has already been changed to the final version before
# being triggered and it does not update the SNAPSHOT version of the
# example project itself.
#
# A release build also does not do a test build of the examples. It
# assume a validate workflow has been run first.
#
release_build(){
    echo "Starting release build for ${HELIDON_VERSION}"
    mvn --version
    java --version

    # Branch we will push this release to
    local LATEST_BRANCH="helidon-2.x"

    if [[ ${HELIDON_VERSION} == *-SNAPSHOT ]]; then
        echo "Helidon version ${HELIDON_VERSION} is a SNAPSHOT version and not a released version. Failing release."
        exit 1
    fi

    # Merge this branch (based on dev-2.x) with the
    # helidon-2.x branch to ensure helidon-2.x has
    # valid history when we push all this to it.
    git fetch origin
    git merge -s ours --no-ff origin/${LATEST_BRANCH}

    # Create and push a git tag
    git tag -f "${HELIDON_VERSION}"
    git push --force origin refs/tags/"${HELIDON_VERSION}":refs/tags/"${HELIDON_VERSION}"

    # Update helidon-2.x branch with this release
    git push origin HEAD:${LATEST_BRANCH}

    echo "======================"
    echo "Created tag:    ${HELIDON_VERSION}"
    echo "Updated branch: ${LATEST_BRANCH}"
    echo "======================"
}

# Invoke command
${COMMAND}

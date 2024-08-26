#!/bin/bash
#
# Copyright (c) 2018, 2024 Oracle and/or its affiliates.
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

set -o pipefail || true  # trace ERR through pipes
set -o errtrace || true # trace ERR through commands and functions
set -o errexit || true  # exit the script if any statement returns a non-true return value

on_error(){
    CODE="${?}" && \
    set +x && \
    printf "[ERROR] Error(code=%s) occurred at %s:%s command: %s\n" \
        "${CODE}" "${BASH_SOURCE[0]}" "${LINENO}" "${BASH_COMMAND}"
}
trap on_error ERR

# Path to this script
if [ -h "${0}" ] ; then
    SCRIPT_PATH="$(readlink "${0}")"
else
    # shellcheck disable=SC155
    SCRIPT_PATH="${0}"
fi
readonly SCRIPT_PATH

# Path to the root of the workspace
# shellcheck disable=SC2046
WS_DIR=$(cd $(dirname -- "${SCRIPT_PATH}") ; cd ../.. ; pwd -P)
readonly WS_DIR

readonly HELIDON_REPO=https://github.com/helidon-io/helidon

version() {
  awk 'BEGIN {FS="[<>]"} ; /<helidon.version>/ {print $3; exit 0}' "${1}"
}

readonly HELIDON_BRANCH="main"

HELIDON_VERSION=$(version "${WS_DIR}/pom.xml")
readonly HELIDON_VERSION

echo "HELIDON_VERSION=${HELIDON_VERSION}"

# We only need a priming build if the Helidon Version being used is a SNAPSHOT version.
# If it is not a SNAPSHOT version then we are using a released version of Helidon and
# do not want to prime
if [[ ! ${HELIDON_VERSION} == *-SNAPSHOT ]]; then
    echo "Helidon version ${HELIDON_VERSION} is not a SNAPSHOT version. Skipping priming build."
    exit 0
fi

cd "$(mktemp -d)"

git clone ${HELIDON_REPO} --branch ${HELIDON_BRANCH} --single-branch --depth 1
cd helidon

HELIDON_VERSION_IN_REPO=$(version bom/pom.xml)
readonly HELIDON_VERSION_IN_REPO

if [ "${HELIDON_VERSION}" != "${HELIDON_VERSION_IN_REPO}" ]; then
    echo "ERROR: Examples Helidon version ${HELIDON_VERSION} does not match version in Helidon repo ${HELIDON_VERSION_IN_REPO}"
    exit 1
fi

# shellcheck disable=SC2086
mvn ${MAVEN_ARGS} --version

echo "Building Helidon version ${HELIDON_VERSION} from Helidon repo branch ${HELIDON_BRANCH}"

# shellcheck disable=SC2086
mvn ${MAVEN_ARGS} \
  -DskipTests \
  -Dmaven.test.skip=true \
  clean install

#!/bin/bash -e
#
# Copyright (c) 2018, 2022 Oracle and/or its affiliates.
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

readonly HELIDON_REPO_NAME=helidon
readonly HELIDON_REPO=https://github.com/helidon-io/${HELIDON_REPO_NAME}
readonly HELIDON_BRANCH=${1}

if [ -z "${1}" ]; then
    echo "No branch specified. Must specify branch of ${HELIDON_REPO}"
    exit 1
fi

# Do a priming build of Helidon to populate local maven cache
# with SNAPSHOT versions
cd ${TMPDIR}

if [ -d "${HELIDON_REPO_NAME}" ]; then
    echo "Removing existing ${HELIDON_REPO_NAME} repository in $(pwd)"
    rm -rf "${HELIDON_REPO_NAME}"
fi

mvn ${MAVEN_ARGS} --version

git clone ${HELIDON_REPO}
cd ${HELIDON_REPO_NAME}
git checkout ${HELIDON_BRANCH}
mvn clean install -DskipTests


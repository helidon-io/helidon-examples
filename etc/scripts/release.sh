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

version() {
  awk 'BEGIN {FS="[<>]"} ; /<helidon.version>/ {print $3; exit 0}' "${1}"
}

# For releases this should be a released version of Helidon
HELIDON_VERSION=$(version "${WS_DIR}/pom.xml")
readonly HELIDON_VERSION

echo "HELIDON_VERSION=${HELIDON_VERSION}"

release_build(){
  echo "Starting release build for ${HELIDON_VERSION}"

  # Branch we will push this release to
  local LATEST_BRANCH="helidon-4.x"

  if [[ ${HELIDON_VERSION} == *-SNAPSHOT ]]; then
    echo "Helidon version ${HELIDON_VERSION} is a SNAPSHOT version and not a released version. Failing release."
    exit 1
  fi

  # Merge this branch (based on dev-4.x) with the
  # helidon-4.x branch to ensure helidon-4.x has
  # valid history when we push all this to it.
  git fetch origin
  git merge -s ours --no-ff origin/${LATEST_BRANCH}

  # Create and push a git tag
  git tag -f "${HELIDON_VERSION}"
  git push --force origin refs/tags/"${HELIDON_VERSION}":refs/tags/"${HELIDON_VERSION}"

  # Update helidon-4.x branch with this release
  git push origin HEAD:${LATEST_BRANCH}

  echo "======================"
  echo "Created tag:    ${HELIDON_VERSION}"
  echo "Updated branch: ${LATEST_BRANCH}"
  echo "======================"
}

release_build

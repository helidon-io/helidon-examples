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

readonly VERSION=${1}

if [ -z "${VERSION}" ]; then
    echo "usage: $(basename "${0}") <version>"
    exit 1
fi

# arg1: pattern
# arg2: include pattern
search() {
  set +o pipefail
  grep "${1}" -Er . --include "${2}" | cut -d ':' -f 1 | xargs git ls-files | sort | uniq
}

PATH=${PATH}:"${WS_DIR}"/etc/scripts
cd "${WS_DIR}"

# Update parent versions
while read -r pom; do
    if update-version.awk -v version="${VERSION}" "${pom}" > "${pom}.tmp"; then
      echo "Updating ${pom}"
      mv "${pom}.tmp" "${pom}"
    else
      rm -f "${pom}.tmp"
    fi
done < <(find . -name pom.xml -exec git ls-files {} \; | sort | uniq)

# Update helidon.version properties
while read -r pom ; do
  echo "Updating helidon.version in ${pom}"
  sed -e "s#<helidon.version>[a-zA-Z0-9.-]*</helidon.version>#<helidon.version>${VERSION}</helidon.version>#" "${pom}" > "${pom}.tmp"
  mv "${pom}.tmp" "${pom}"
done < <(search "<helidon.version>" pom.xml)

# Update helidonversion property in build.gradle files
while read -r buildgradle ; do
  echo "Updating helidonversion in ${buildgradle}"
  sed -e "s#helidonversion = '[a-zA-Z0-9.-]*'#helidonversion = '${VERSION}'#" "${buildgradle}" > "${buildgradle}.tmp"
  mv "${buildgradle}.tmp" "${buildgradle}"
done < <(find . -name build.gradle -print)


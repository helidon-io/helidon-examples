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

# Updates pom files to use the specified version of Helidon
# usage: updatehelidonversion.sh <n.n.n>

# Path to this script
[ -h "${0}" ] && readonly SCRIPT_PATH="$(readlink "${0}")" || readonly SCRIPT_PATH="${0}"

readonly NEW_VERSION=$1
readonly SCRIPT_DIR=$(dirname ${SCRIPT_PATH})

if [ -z "${NEW_VERSION}" ]; then
    echo "usage: $0 <new-helidon-version>"
    exit 1
fi

if [ -z "${TMPDIR}" ]; then
  readonly TMPDIR="/tmp"
fi

readonly POM_FILES=$(find . -name pom.xml -print)
readonly GRADLE_FILES=$(find . -name build.gradle -print)

for f in ${POM_FILES}; do
    pom_dir=$(dirname $f)
    awk -v gavs=\
io.helidon:helidon-dependencies:${NEW_VERSION},\
io.helidon.applications:helidon-se:${NEW_VERSION},\
io.helidon.applications:helidon-mp:${NEW_VERSION} \
    -f ${SCRIPT_DIR}/updateparent.awk > ${pom_dir}/pom.xml.tmp $f
    if [ $? -eq 0 ]; then
        echo "Updated $f with Helidon version ${NEW_VERSION}"
        mv  ${pom_dir}/pom.xml.tmp $f
    else
        rm -f ${pom_dir}/pom.xml.tmp
    fi
done

# Update helidon.version property in poms
for f in ${POM_FILES}; do
    # first make sure pom has property
    if  grep -q "<helidon.version>" "$f" ; then
        cat $f | sed -e "s#<helidon.version>[a-zA-Z0-9.-]*</helidon.version>#<helidon.version>${NEW_VERSION}</helidon.version>#" > ${TMPDIR}/pom.xml
        mv "${TMPDIR}/pom.xml" $f
        echo "Updated helidon.version in $f with Helidon version ${NEW_VERSION}"
    fi
done

# Update helidonversion property in build.gradle files
for f in ${GRADLE_FILES}; do
    # first make sure file has property
    if  grep -q "helidonversion =" "$f" ; then
        cat $f | sed -e "s#helidonversion = [a-zA-Z0-9.'-]*#helidonversion = '${NEW_VERSION}'#" > ${TMPDIR}/build.gradle
        mv "${TMPDIR}/build.gradle" $f
        echo "Updated helidonversion in $f with Helidon version ${NEW_VERSION}"
    fi
done

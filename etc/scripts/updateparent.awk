#!awk -f
#
# Copyright (c) 2023, 2024 Oracle and/or its affiliates.
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

# Scan the pom file looking for a <parent> that is a Helidon artifact
# and update the version number to the version specified in the "version" variable
#
# You must pass the version: "-v version=n.n.n"
#
# exit code
#   0 new version was applied to file
#   1 new version was not applied to file (this is not necessarily an error)
#
BEGIN {
    if (gavs == "") {
        print "Must provide one or more GAVs using '-v gavs=g1:a1:v1,g2:a2:v2"
        exit 1
    }

    # Split list into array of GAVs
    split(gavs, gavArray, ",")

    for (i in gavArray) {
        gav = gavArray[i]

        # Split a GAV into it's part
        split(gav, gavParts, ":")

        # Map GA to V
        ga = gavParts[1] ":" gavParts[2]
        gaMap[ga] = gavParts[3]
    }

    fileChanged="false"
    inParent="false"
    parentGroupId=""
    parentArtifactId=""
    parentRelativePath="false"
    FS="[<>]"
}

/<parent>/ {
    inParent="true"
}

/<groupId>/ && inParent == "true" {
    parentGroupId=$3
}

/<artifactId>/ && inParent == "true" {
    parentArtifactId=$3
}

/<version>/ && inParent == "true" {
    ga = parentGroupId ":" parentArtifactId
    v = gaMap[ga]

    if (length(v) != 0) {
        printf("%s<version>%s</version>\n", $1, v)
        fileChanged="true"
        next
    }
}

/<relativePath/ && inParent == "true" {
    parentRelativePath="true"
}

/<\/parent>/ {
    inParent="false"
    parentGroupId=""
    parentArtifactId=""
    if (parentRelativePath == "false") {
        printf("%s<relativePath/>\n", $1$1)
    }
}


{
    print $0
}

END {
    if ( fileChanged == "true" ) {
        exit 0
    } else {
        exit 1
    }
}

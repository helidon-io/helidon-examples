#!awk -f
#
# Copyright (c) 2022 Oracle and/or its affiliates.
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
    if (version == "") {
        print "Must provide version using '-v version=n.n.n"
        exit 1
    }

    fileChanged="false"
    inParent="false"
    parentGroupId=""
    parentArtifactId=""
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

    # Change version only for parents that are Helidon artifacts
    if ( (parentGroupId == "io.helidon" && parentArtifactId == "helidon-dependencies") ||
         (parentGroupId == "io.helidon.applications" && parentArtifactId == "helidon-se") ||
         (parentGroupId == "io.helidon.applications" && parentArtifactId == "helidon-mp") ) {

        # Makes sure indentation is correct
        printf("%s<version>%s</version>\n", $1, version)
        fileChanged="true"
        next
    }
}

/<\/parent>/ {
    inParent="false"
    parentGroupId=""
    parentArtifactId=""
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

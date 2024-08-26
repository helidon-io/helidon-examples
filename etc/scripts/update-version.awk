#!/usr/bin/env awk -f
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

BEGIN {
    if (version == "") {
        print "Must provide version '-v version=n.n.n"
        exit 1
    }
    VERSIONS["io.helidon:helidon-dependencies"] = version
    VERSIONS["io.helidon.applications:helidon-se"] = version
    VERSIONS["io.helidon.applications:helidon-mp"] = version
    GROUPID =""
    ARTIFACTID =""
    IN_PARENT ="false"
    RELATIVE_PATH ="false"
    FILE_CHANGED="false"
    FS="[<>]"
}

/<parent>/ {
    IN_PARENT ="true"
}

/<groupId>/ && IN_PARENT == "true" {
    GROUPID =$3
}

/<artifactId>/ && IN_PARENT == "true" {
    ARTIFACTID =$3
}

/<version>/ && IN_PARENT == "true" {
    v = VERSIONS[GROUPID ":" ARTIFACTID]
    if (length(v) != 0) {
        printf("%s<version>%s</version>\n", $1, v)
        FILE_CHANGED="true"
        next
    }
}

/<relativePath/ && IN_PARENT == "true" {
    RELATIVE_PATH ="true"
}

/<\/parent>/ {
    IN_PARENT ="false"
    GROUPID =""
    ARTIFACTID =""
    if (RELATIVE_PATH == "false") {
        printf("%s<relativePath/>\n", $1$1)
    }
}

{
    print $0
}

END {
    if (FILE_CHANGED == "true") {
        exit 0
    } else {
        exit 1
    }
}

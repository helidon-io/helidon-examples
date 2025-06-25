/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.declarative.server;

import io.helidon.common.Reflected;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

/**
 * Data transfer object for result of refresh task call.
 */
// JSON-B uses reflection, so this must be added when using GraalVM native-image
@Reflected
public class RefreshTaskDto {
    private final int count;

    RefreshTaskDto(int count) {
        this.count = count;
    }

    /**
     * Create a new instance.
     *
     * @param count number of invocations of the refresh task.
     * @return a new configured dto
     */
    @JsonbCreator
    public static RefreshTaskDto create(@JsonbProperty("count") int count) {
        return new RefreshTaskDto(count);
    }

    /**
     * Count of invocation.
     *
     * @return count
     */
    public int getCount() {
        return count;
    }
}

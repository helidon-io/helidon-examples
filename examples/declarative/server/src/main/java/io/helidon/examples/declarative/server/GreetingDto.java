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
 * Data transfer object for greeting response.
 */
// JSON-B uses reflection, so this must be added when using GraalVM native-image
@Reflected
public class GreetingDto {
    private final String greeting;
    private final String name;

    GreetingDto(String greeting, String name) {
        this.greeting = greeting;
        this.name = name;
    }

    /**
     * Create a new instance of this dto.
     *
     * @param greeting greeting, such as {@code Hello}
     * @param name     name, such as {@code World}
     * @return a new dto instance
     */
    @JsonbCreator
    public static GreetingDto create(@JsonbProperty("greeting") String greeting, @JsonbProperty("name") String name) {
        return new GreetingDto(greeting, name);
    }

    /**
     * Greeting as configured on the server.
     *
     * @return greeting
     */
    public String getGreeting() {
        return greeting;
    }

    /**
     * Name that is greeted. Either the default (World), or as sent as a path parameter.
     *
     * @return name
     */
    public String getName() {
        return name;
    }
}

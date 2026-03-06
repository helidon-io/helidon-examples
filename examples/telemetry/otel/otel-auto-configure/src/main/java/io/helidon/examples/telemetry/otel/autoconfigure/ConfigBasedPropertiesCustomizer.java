/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
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

package io.helidon.examples.telemetry.otel.autoconfigure;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.helidon.config.Config;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

/**
 * Looks up OpenTelemetry auto-config settings from a Helidon config node.
 */
class ConfigBasedPropertiesCustomizer implements ConfigProperties {

    private final Config config;

    private ConfigBasedPropertiesCustomizer(Config config) {
        this.config = config;
    }

    static ConfigBasedPropertiesCustomizer create(Config config) {
        return new ConfigBasedPropertiesCustomizer(config);
    }

    @Override
    public String getString(String name) {
        return config.get(name).asString().orElse(null);
    }

    @Override
    public Boolean getBoolean(String name) {
        return config.get(name).asBoolean().orElse(null);
    }

    @Override
    public Integer getInt(String name) {
        return config.get(name).asInt().orElse(null);
    }

    @Override
    public Long getLong(String name) {
        return config.get(name).asLong().orElse(null);
    }

    @Override
    public Double getDouble(String name) {
        return config.get(name).asDouble().orElse(null);
    }

    @Override
    public Duration getDuration(String name) {
        return config.get(name).as(Duration.class).orElse(null);
    }

    @Override
    public List<String> getList(String name) {
        return config.get(name).asList(String.class).orElse(List.of());
    }

    @Override
    public Map<String, String> getMap(String name) {
        /*
        Decode key=value pairs separated by commas. If a key or value is empty do not create an entry for that key.
         */

        return Arrays.stream(config.get(name)
                                     .asString()
                                     .orElse("")
                                     .split(","))
                .map(String::trim)
                .filter(assignment -> assignment.contains("="))
                .map(assignment -> assignment.split("=", 2))
                .filter(sides ->
                                sides.length == 2
                                        && !sides[0].trim().isEmpty()
                                        && !sides[1].trim().isEmpty())
                .collect(Collectors.toMap(sides -> sides[0].trim(),
                                          sides -> sides[1].trim(),
                                          (v1, v2) -> v2)); // Last wins
    }
}

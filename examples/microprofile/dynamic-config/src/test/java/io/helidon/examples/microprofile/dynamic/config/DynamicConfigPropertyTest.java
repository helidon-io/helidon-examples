/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.microprofile.dynamic.config;

import io.helidon.microprofile.testing.Configuration;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
@HelidonTest(resetPerTest = true)
@Configuration(configSources = "mp-test-config.properties")
public class DynamicConfigPropertyTest {
    @Inject
    private Config config;
    @Inject
    @ConfigProperty(name = "app.log.level")
    private String appLogLevel;
    @Inject
    @ConfigProperty(name = "app.startup.message")
    private String appStartupMessage;

    @AfterEach
    void tearDownProperties() {
        System.clearProperty("app.log.level");
        System.clearProperty("APP_LOG_LEVEL");

        System.clearProperty("app.startup.message");
        System.clearProperty("APP_STARTUP_MESSAGE");
    }

    @Test
    void testConfigFromPropertiesFile() {
        String expectedAppLogLevel="DEBUG";
        String expectedAppStartupMessage="MP Message from application Test config props.";

        assertEquals(expectedAppLogLevel, appLogLevel);
        assertEquals(expectedAppStartupMessage, appStartupMessage);

        assertEquals(expectedAppLogLevel, config.getValue("app.log.level", String.class));
        assertEquals(expectedAppStartupMessage, config.getValue("app.startup.message", String.class));
    }
}

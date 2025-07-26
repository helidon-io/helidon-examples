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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The DynamicProperties class provides access to application properties and configuration.
 * It injects properties from application configuration or Java VM command arguments.
 *  @author [Dasarathi Rout]
 */
@ApplicationScoped
public class DynamicProperties {

    /**
     * The application startup message injected from configuration.
     */
    @Inject
    @ConfigProperty(name = "app.startup.message")
    private volatile String appStartupMessage;

    /**
     * The configuration instance injected for accessing other properties.
     */
    @Inject
    private Config config;

    /**
     * The current message, initialized with a default value.
     */
    private final AtomicReference<String> currentMessage;

    /**
     * Constructs a new DynamicProperties instance, initializing the current message.
     */
    public DynamicProperties(){
        this.currentMessage = new AtomicReference<>("Default Properties (Before Config Loaded)");
    }

    /**
     * Returns the application startup message.
     * @return the application startup message
     */
    public String getAppStartupMessage(){
        return this.appStartupMessage;
    }

    /**
     * Returns the application log level.
     * @return the application log level as a string
     */
    public String getAppLogLevel(){
        return config.getValue("app.log.level", String.class);
    }
}

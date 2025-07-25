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

@ApplicationScoped
public class DynamicProperties {
    @Inject //From application props config or java vm command arguments
    @ConfigProperty(name = "app.startup.message")
    private volatile String appStartupMessage;

    @Inject
    private Config config;

    private final AtomicReference<String> currentMessage;
    public DynamicProperties(){
        this.currentMessage = new AtomicReference<>("Default Properties (Before Config Loaded)");
    }

    public String getAppStartupMessage(){
        return this.appStartupMessage;
    }

    public String getAppLogLevel(){
        return config.getValue("app.log.level", String.class);
    }
}
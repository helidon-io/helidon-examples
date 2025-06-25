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

import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

/**
 * Main class responsible for starting the service registry.
 * <p>
 * This class is annotated with {@link io.helidon.service.registry.Service.GenerateBinding}, which (when used in combination
 * with Helidon Maven Plugin) generates a binding class that can be used to bootstrap Helidon without usage of reflection
 * and classpath lookup during discovery of services.
 */
// annotation is required to generate application binding
@Service.GenerateBinding
public class Main {
    static {
        // used when building with GraalVM native image to configure logging during build
        LogConfig.initClass();
    }

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // used to configure logging
        LogConfig.configureRuntime();

        ServiceRegistryManager.start(ApplicationBinding.create());

        WebServer webServer = Services.get(WebServer.class);
        System.out.println("Server started on: http://localhost:" + webServer.port() + "/api/business/greet");
    }
}

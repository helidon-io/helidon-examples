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

package io.helidon.examples.quickstart.inject;

import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;

/**
 * Main class to start the application using service registry.
 */
// generate "stub" binding during compilation, so we can use it from this class
// the Maven plugin then overwrites it with proper information
// this should only be done in the Main class, as it needs to gather information from all dependencies of the application
@Service.GenerateBinding
public class Main {
    static {
        // initialize logging at build time (for native-image build)
        LogConfig.initClass();
    }

    private Main() {
    }

    /**
     * Start the application.
     *
     * @param args command line arguments, currently ignored
     */
    public static void main(String[] args) {
        // initialize logging at runtime
        // (if in GraalVM native image, this will re-configure logging with runtime configuration)
        LogConfig.configureRuntime();

        // start the service registry - uses generated application binding to avoid reflection and runtime lookup
        ServiceRegistryManager.start(ApplicationBinding.create());
    }
}

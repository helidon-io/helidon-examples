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

package io.helidon.hol.agentic.assistant;

import io.helidon.hol.agentic.assistant.data.MenuItemsIngestor;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.ServiceRegistryManager;
import io.helidon.service.registry.Services;

/**
 * Main entry point for the coffee-shop agentic assistant application.
 */
@Service.GenerateBinding
public class ApplicationMain {
    private ApplicationMain() {
        throw new UnsupportedOperationException("No instances");
    }

    /**
     * Starts the coffee-shop agentic assistant application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        LogConfig.configureRuntime();

        ServiceRegistryManager.start(ApplicationBinding.create());

        // Build embeddings from coffee menu JSON on startup.
        Services.get(MenuItemsIngestor.class).ingest();

        System.out.println("Coffee Shop Agentic Assistant is running at http://localhost:8080");
    }
}

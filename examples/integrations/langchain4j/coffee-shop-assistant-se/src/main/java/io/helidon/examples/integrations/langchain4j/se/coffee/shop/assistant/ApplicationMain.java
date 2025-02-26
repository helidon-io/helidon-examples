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
package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant;

import io.helidon.common.config.Config;
import io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.ai.MenuItemsIngestor;
import io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.rest.ChatBotService;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

/**
 * Coffee Shop Assistant application.
 */
public class ApplicationMain {

    /**
     * Cannot be instantiated.
     */
    private ApplicationMain() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // Make sure logging is enabled as the first thing
        LogConfig.configureRuntime();

        var config = Services.get(Config.class);

        // Initialize embedding store
        Services.get(MenuItemsIngestor.class)
                .ingest();

        WebServer.builder()
                .config(config.get("server"))
                .routing(routing -> routing.register("/", Services.get(ChatBotService.class)))
                .build()
                .start();
    }
}

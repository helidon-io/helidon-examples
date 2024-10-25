/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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
package io.helidon.examples.webserver.sse;

import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.staticcontent.StaticContentService;

/**
 * This application provides a simple service with a UI to exercise Server Sent Events (SSE).
 */
class Main {

    private Main() {
    }

    /**
     * Executes the example.
     *
     * @param args command line arguments, ignored
     */
    public static void main(String[] args) {
        // load logging configuration
        LogConfig.configureRuntime();

        WebServer server = WebServer.builder()
                .routing(Main::routing)
                .port(8080)
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port());
    }

    /**
     * Updates the routing rules.
     *
     * @param rules routing rules
     */
    static void routing(HttpRules rules) {
        rules.register("/", StaticContentService.builder("WEB")
                        .welcomeFileName("index.html")
                        .build())
                .register("/api", new SseService());
    }
}

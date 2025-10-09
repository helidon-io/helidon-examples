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
package io.helidon.examples.data.mysql;

import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;

/**
 * The Main class serves as the entry point for the application.
 * It demonstrates the usage of Helidon Data in an SE imperative application.
 */
public class Main {

    private Main() {
        throw new UnsupportedOperationException("Instances of Main class are not allowed");
    }

    /**
     * Entry point for the SE application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String... args) {
        LogConfig.configureRuntime();
        WebServer webServer = WebServer.builder()
                .port(8080)
                .routing(routing -> routing.register("/pokemon", new PokemonService()))
                .build();
        webServer.start();
        System.out.println("Server started on: http://localhost:" + webServer.port());
    }

}

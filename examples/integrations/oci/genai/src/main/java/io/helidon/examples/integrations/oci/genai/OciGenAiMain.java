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

package io.helidon.examples.integrations.oci.genai;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

import com.oracle.bmc.model.BmcException;

/**
 * Main class of the example.
 * This example sets up a web server to serve REST API example of how to use OCI Generative AI Service.
 */
public final class OciGenAiMain {
    /**
     * Cannot be instantiated.
     */
    private OciGenAiMain() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // load logging configuration
        LogConfig.configureRuntime();

        // Get default config
        Config config = Services.get(Config.class);

        // Prepare routing for the server
        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(OciGenAiMain::routing)
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port() + "/genai");
    }

    /**
     * Updates HTTP Routing and registers observe providers.
     */
    static void routing(HttpRouting.Builder routing) {
        routing.register("/genai", new GenAiService())
                // OCI SDK error handling
                .error(BmcException.class, (req, res, ex) ->
                        res.status(ex.getStatusCode())
                                .send(ex.getMessage()));
    }
}

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

import java.io.IOException;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SessionTokenAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
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
    public static void main(String[] args) throws IOException {
        // load logging configuration
        LogConfig.configureRuntime();

        // initialize global config from default configuration
        Config config = Config.create();
        Config.global(config);

        // Initialize GenAI client based on OCI Auth as configured in config system
        //ServiceRegistry registry = GlobalServiceRegistry.registry();
        //registry.get(SessionTokenAuthenticationDetailsProvider.class) doens't work
        //BasicAuthenticationDetailsProvider authProvider = registry.get(BasicAuthenticationDetailsProvider.class);
        //assertThat(provider, instanceOf(SessionTokenAuthenticationDetailsProvider.class));
        //SessionTokenAuthenticationDetailsProvider sessionTokenAuthenticationDetailsProvider
        // = (SessionTokenAuthenticationDetailsProvider) provider;
        AuthenticationDetailsProvider authProvider = new SessionTokenAuthenticationDetailsProvider(
                ConfigFileReader.DEFAULT_FILE_PATH, "token");
        GenerativeAiInferenceClient generativeAiInferenceClient = GenerativeAiInferenceClient.builder()
                .region(Region.valueOf(config.get("oci.genai.region").asString().get()))
                .build(authProvider);

        // Prepare routing for the server
        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(routing -> routing
                        .register("/genai", new GenAiService(generativeAiInferenceClient, config))
                        // OCI SDK error handling
                        .error(BmcException.class, (req, res, ex) ->
                                res.status(ex.getStatusCode())
                                        .send(ex.getMessage())))
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port() + "/genai");
    }
}

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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.context.Initialized;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

/**
 * The MessageLogger class is an ApplicationScoped CDI bean that logs a startup message to the console.
 * It observes the initialization of the ApplicationScoped context and prints a message with instructions
 * on how to run the application with different configuration options.
 * @author [Dasarathi Rout]
 */
@ApplicationScoped
public class MessageLogger {
    /**
     * Observes the initialization of the ApplicationScoped context and logs a startup message to the console.
     *
     * @param init the initialization event
     */
    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object init) {
        System.out.println("**********************************************************************************************************************************************");
        System.out.println("*   Ex1 : java -jar target/helidon-examples-microprofile-dynamic-config.jar                                                                  *");
        System.out.println("*   Ex2 : java -jar -Dapp.startup.message='Override Message From Terminal' target/helidon-examples-microprofile-dynamic-config.jar           *");
        System.out.println("*   Ex3 : export=APP_STARTUP_MESSAGE='Override Message From Env Vars'; java -jar target/helidon-examples-microprofile-dynamic-config.jar     *");
        System.out.println("*   REST Endpoint: http://localhost:8080/dynamic/config                                                                                      *");
        System.out.println("*   Config Source Url: http://localhost:9192/api/mp/properties/dyanmic.configs                                                               *");
        System.out.println("**********************************************************************************************************************************************");
    }
}


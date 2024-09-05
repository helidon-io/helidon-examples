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

package io.helidon.examples.webserver.grpc;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.grpc.GrpcRouting;
import io.helidon.webserver.observe.ObserveFeature;
import io.helidon.webserver.observe.health.HealthObserver;

class GrpcMain {

    private GrpcMain() {
    }

    /**
     * Main method.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        LogConfig.configureRuntime();

        // initialize global config from default configuration
        Config config = Config.create();
        Config.global(config);
        Config serverConfig = config.get("server");

        // create a health check to verify gRPC endpoint
        ObserveFeature observe = ObserveFeature.builder()
                .addObserver(HealthObserver.builder()
                                     .details(true)
                                     .addCheck(new StringServiceHealthCheck(serverConfig))
                                     .build())
                .build();

        // start server and register gRPC routing and health check
        WebServer.builder()
                .config(serverConfig)
                .addRouting(GrpcRouting.builder().service(new StringService()))
                .addFeature(observe)
                .build()
                .start();
    }
}

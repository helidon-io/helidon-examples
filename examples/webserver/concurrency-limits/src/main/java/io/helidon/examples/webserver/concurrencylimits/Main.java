/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates.
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

package io.helidon.examples.webserver.concurrencylimits;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;

/**
 * The application main class.
 */
public class Main {

    private static final System.Logger LOGGER = System.getLogger(Main.class.getName());

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(String[] args) {

        // load logging configuration
        LogConfig.configureRuntime();

        // initialize global config from default configuration
        Config config = Services.get(Config.class);

        // Default port uses the fixed limiter
        // "aimd" port uses the AIMD limiter
        WebServerConfig webserverConfig = WebServer.builder()
                .config(config.get("server"))
                .routing(Main::fixedRouting)
                .routing("aimd", Main::aimdRouting)
                .buildPrototype();

        WebServer webserver = webserverConfig.build().start();

        LOGGER.log(System.Logger.Level.INFO, "WEB server is up! http://localhost:" + webserver.port() + "/fixed/sleep"
                + " " + "http://localhost:" + webserver.port("aimd") + "/aimd/sleep");
    }

    /**
     * Updates HTTP Routing.
     */
    static void fixedRouting(HttpRouting.Builder routing) {
        routing.register("/fixed", new SleepService());
    }

    static void aimdRouting(HttpRouting.Builder routing) {
        routing.register("/aimd", new SleepService());
    }
}

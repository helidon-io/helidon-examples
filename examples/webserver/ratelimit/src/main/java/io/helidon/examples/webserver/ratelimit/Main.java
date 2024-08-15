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

package io.helidon.examples.webserver.ratelimit;

import java.util.concurrent.Semaphore;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * The application main class.
 */
public class Main {

    private static final System.Logger LOGGER = System.getLogger(Main.class.getName());

    private static Semaphore rateLimitSem = null;

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
        Config config = Config.create();
        Config.global(config);

        WebServerConfig webserverConfig = WebServer.builder()
                .config(config.get("server"))
                .routing(Main::routing)
                .buildPrototype();

        LOGGER.log(System.Logger.Level.INFO, "WebServer maxConcurrentRequests is " + webserverConfig.maxConcurrentRequests());

        WebServer webserver = webserverConfig.build().start();

        System.out.println("WEB server is up! http://localhost:" + webserver.port() + "/sleep");
    }

    /**
     * Updates HTTP Routing.
     */
    static void routing(HttpRouting.Builder routing) {
        routing.get("/sleep/{seconds}", Main::sleepHandler);
        int rateLimit = Config.global().get("app").get("ratelimit").asInt().orElse(20);
        LOGGER.log(System.Logger.Level.INFO, "         Application rate limit is " + rateLimit);
        rateLimitSem = new Semaphore(rateLimit);
    }

    /**
     * Sleep for a specified number of seconds.
     * The optional path parameter controls the number of seconds to sleep. Defaults to 1
     *
     * @param request  server request
     * @param response server response
     */
    private static void sleepHandler(ServerRequest request, ServerResponse response) {
        int seconds = request.path().pathParameters().first("seconds").asInt().orElse(1);

        try {
            rateLimitSem.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            response.send(String.valueOf(sleep(seconds)));
        } finally {
            rateLimitSem.release();
        }
    }

    /**
     * Sleep current thread.
     *
     * @param seconds number of seconds to sleep
     * @return number of seconds requested to sleep
     */
    private static int sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1_000L);
        } catch (InterruptedException e) {
            LOGGER.log(System.Logger.Level.WARNING, e);
        }
        return seconds;
    }
}

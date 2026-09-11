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
package io.helidon.examples.imperative.data.jdbc;

import io.helidon.common.Api;
import io.helidon.config.Config;
import io.helidon.data.jdbc.JdbcClient;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.http.HttpRouting;

/**
 * Starts the imperative JDBC Pokemon application.
 */
@SuppressWarnings(Api.SUPPRESS_INCUBATING)
public final class Main {

    static {
        LogConfig.initClass();
    }

    private Main() {
        throw new UnsupportedOperationException("Instances of Main class are not allowed");
    }

    /**
     * Starts the web server.
     *
     * @param args command-line arguments supplied to the application
     */
    static void main(String... args) {
        LogConfig.configureRuntime();
        Config config = Services.get(Config.class);

        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing(Main::routing)
                .build()
                .start();

        System.out.println("Server started on: http://localhost:" + server.port());
    }

    static void routing(HttpRouting.Builder routing) {
        Config database = Services.get(Config.class).get("app.database");
        JdbcClient jdbcClient = JdbcClient.builder()
                .connection(connection -> connection
                        .url(database.get("url").asString().get())
                        .username(database.get("username").asString().get())
                        .password(database.get("password").asString().get().toCharArray())
                        .jdbcDriverClassName(database.get("jdbc-driver-class-name").asString().get()))
                .build();
        routing.register("/pokemon", new PokemonService(jdbcClient));
    }
}

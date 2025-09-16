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

package io.helidon.examples.dbclient.health;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.health.DbClientHealthCheck;
import io.helidon.health.HealthCheck;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.observe.ObserveFeature;
import io.helidon.webserver.observe.health.HealthObserver;
import io.helidon.webserver.observe.spi.Observer;

/**
 * The application main class.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        // load logging configuration
        LogConfig.configureRuntime();

        Config config = Config.global();
        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .addFeature(observeFeature(config))
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port() + "/");
    }

    static ObserveFeature observeFeature(Config config) {
        DbClient db = DbClient.create(config.get("db"));
        return ObserveFeature.builder()
                .addObserver(healthObserver(dbHealth(db)))
                .build();
    }

    static Observer healthObserver(HealthCheck... checks) {
        return HealthObserver.builder()
                .details(true)
                .addChecks(checks)
                .build();
    }

    static HealthCheck dbHealth(DbClient db) {
        return DbClientHealthCheck.builder(db)
                .name("db")
                .query()
                .statement("SELECT 0")
                .build();
    }
}

/*
 * Copyright (c) 2025, 2026 Oracle and/or its affiliates.
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

import java.util.Map;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpServer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@Testcontainers(disabledWithoutDocker = true)
@ServerTest
class DbClientHealthTest {

    private static final boolean IS_ARM = System.getProperty("os.arch", "amd64").equals("aarch64");
    private static final DockerImageName IMAGE = DockerImageName.parse("container-registry.oracle.com/mysql/community-server:"
            + (IS_ARM ? "9.7.3-aarch64" : "9.7.3"))
            .asCompatibleSubstituteFor("mysql");

    @Container
    @SuppressWarnings("resource")
    private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>(IMAGE)
            .withUsername("user")
            .withPassword("mysql123")
            .withDatabaseName("db1");

    @SetUpServer
    static void setUp(WebServerConfig.Builder server) {
        Config config = Config.builder()
                .addSource(ConfigSources.create(Map.of("db.connection.url", CONTAINER.getJdbcUrl())))
                .metaConfig()
                .build();
        server.addFeature(Main.observeFeature(config));
    }

    @Test
    void testHealth(Http1Client client) {
        try (var rsp = client.get("/observe/health").request()) {
            assertThat(rsp.status().code(), is(200));
            JsonArray checks = rsp.as(JsonObject.class).getJsonArray("checks");
            assertThat(checks.size(), is(not(nullValue())));
            assertThat(checks, hasItem(allOf(
                    hasEntry("name", Json.createValue("db")),
                    hasEntry("status", Json.createValue("UP")))));
        }
    }
}

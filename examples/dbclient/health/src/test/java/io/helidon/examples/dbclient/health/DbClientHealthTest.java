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
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpServer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ServerTest
class DbClientHealthTest {

    @SetUpServer
    static void setUp(WebServerConfig.Builder server) {
        server.addFeature(Main.observeFeature(Config.global()));
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

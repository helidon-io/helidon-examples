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

package io.helidon.examples.dbclient.postgres;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.testing.junit5.RoutingTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@Testcontainers(disabledWithoutDocker = true)
@RoutingTest
class PokemonPostgresTest {

    private static final JsonBuilderFactory JSON_FACTORY = Json.createBuilderFactory(Map.of());
    private static final ImageFromDockerfile IMAGE = new ImageFromDockerfile("pgsql", false)
            .withFileFromPath(".", Path.of("etc/docker"));

    @Container
    static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer(IMAGE)
            .withUsername("user")
            .withPassword("pgsql123")
            .withDatabaseName("db1");

    @SetUpRoute
    static void setUp(HttpRules routing) {
        Config config = Config.builder()
                .addSource(ConfigSources.create(Map.of("db.connection.url", CONTAINER.getJdbcUrl())))
                .metaConfig()
                .build();
        routing.register("/pokemon", new PokemonService(config.get("db")));
    }

    @Test
    void testCreateDeleteAll(Http1Client client) {
        // create
        try (var rsp = client.post("/pokemon").submit(JSON_FACTORY.createObjectBuilder()
                .add("name", "Raticate")
                .add("type", "Normal/Ice")
                .build())) {
            assertThat(rsp.status().code(), is(201));
        }

        // verify created
        List<String> names = new ArrayList<>();
        try (var rsp = client.get("/pokemon").request()) {
            assertThat(rsp.status().code(), is(200));
            for (JsonValue jsonValue : rsp.as(JsonArray.class)) {
                names.add(jsonValue.asJsonObject().getString("name"));
            }
        }
        assertThat(names, is(List.of("Raticate")));

        // delete
        try (var rsp = client.delete("/pokemon").request()) {
            assertThat(rsp.status().code(), is(200));
        }

        // verify deleted
        names = new ArrayList<>();
        try (var rsp = client.get("/pokemon").request()) {
            assertThat(rsp.status().code(), is(200));
            for (JsonValue jsonValue : rsp.as(JsonArray.class)) {
                names.add(jsonValue.asJsonObject().getString("name"));
            }
        }
        assertThat(names, is(empty()));
    }

    @Test
    void testAddUpdate(Http1Client client) {
        // create
        try (var rsp = client.post("/pokemon").submit(JSON_FACTORY.createObjectBuilder()
                .add("name", "Raticate")
                .add("type", "Normal")
                .build())) {

            assertThat(rsp.status().code(), is(201));
        }

        // verify created
        try (var rsp = client.get("/pokemon/Raticate").request()) {
            assertThat(rsp.status().code(), is(200));

            JsonObject jsonObject = rsp.as(JsonObject.class);
            assertThat(jsonObject.getString("name", null), is("Raticate"));
            assertThat(jsonObject.getString("type", null), is("Normal"));
        }

        // update
        try (var rsp = client.put("/pokemon/Raticate").submit(JSON_FACTORY.createObjectBuilder()
                .add("type", "Ice")
                .build())) {

            assertThat(rsp.status().code(), is(200));
        }

        // verify updated
        try (var rsp = client.get("/pokemon/Raticate").request()) {
            assertThat(rsp.status().code(), is(200));

            JsonObject jsonObject = rsp.as(JsonObject.class);
            assertThat(jsonObject.getString("name", null), is("Raticate"));
            assertThat(jsonObject.getString("type", null), is("Ice"));
        }

        // delete
        try (var rsp = client.delete("/pokemon/Raticate").request()) {
            assertThat(rsp.status().code(), is(200));
        }

        // verify deleted
        try (var rsp = client.get("/pokemon/Raticate").request()) {
            assertThat(rsp.status().code(), is(404));
        }
    }
}

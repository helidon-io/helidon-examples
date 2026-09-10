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

import java.io.StringReader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Verifies the sample against PostgreSQL.
 */
@Testcontainers(disabledWithoutDocker = true)
@ServerTest
class PokemonApplicationTest {
    private static final ImageFromDockerfile IMAGE =
            new ImageFromDockerfile("helidon-imperative-data-jdbc-postgres-test", true)
                    .withFileFromPath(".", Path.of(System.getProperty("basedir", "."), "etc", "docker"));

    @Container
    @SuppressWarnings("resource")
    private static final PokemonPostgresContainer CONTAINER = new PokemonPostgresContainer(IMAGE)
            .withDatabaseName("pokemons")
            .withUsername("user")
            .withPassword("pgsql123")
            .withInitScript("schema.sql");

    private static final List<Pokemon> SEEDED_POKEMON = List.of(
            new Pokemon(8, "Arbok", "Poison"),
            new Pokemon(7, "Ekans", "Poison"),
            new Pokemon(3, "Machop", "Fighting"),
            new Pokemon(6, "Magikarp", "Water"),
            new Pokemon(5, "Meowth", "Normal"),
            new Pokemon(1, "Pikachu", "Electric"),
            new Pokemon(2, "Raichu", "Electric"),
            new Pokemon(11, "Raikou", "Electric"),
            new Pokemon(12, "Regirock", "Rock"),
            new Pokemon(9, "Sandshrew", "Ground"),
            new Pokemon(10, "Sandslash", "Ground"),
            new Pokemon(4, "Snorlax", "Normal"));

    private final Http1Client client;

    PokemonApplicationTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder routing) {
        Main.routing(routing);
    }

    @Test
    void queriesDocumentedEndpoints() {
        assertThat(count(), is(12));
        assertThat(pokemonList(get("/pokemon/all")), is(SEEDED_POKEMON));

        List<Pokemon> normalPokemon = List.of(new Pokemon(5, "Meowth", "Normal"),
                                              new Pokemon(4, "Snorlax", "Normal"));
        assertThat(pokemonList(get("/pokemon/type/Normal")), is(normalPokemon));
        assertThat(pokemonList(get("/pokemon/search/Normal")), is(normalPokemon));
        assertThat(pokemon(get("/pokemon/get/Meowth")), is(new Pokemon(5, "Meowth", "Normal")));
        assertThat(pokemon(get("/pokemon/explicit-mapper/Meowth")),
                   is(new Pokemon(5, "EXPLICIT: Meowth", "Normal")));
        assertThat(pokemon(get("/pokemon/search/Normal/Meowth")),
                   is(new Pokemon(5, "Meowth", "Normal")));
    }

    @Test
    void returnsEmptyListForUnknownType() {
        assertThat(pokemonList(get("/pokemon/type/DoesNotExist")), is(List.of()));
    }

    @Test
    void returnsNotFoundForUnknownPokemon() {
        try (Http1ClientResponse response = client.get("/pokemon/get/DoesNotExist").request()) {
            assertThat("Unexpected response from " + response.lastEndpointUri(), response.status().code(), is(404));
        }
    }

    @Test
    void insertsAndDeletesPokemon() {
        int expectedCount = count();
        String name = "E2E" + UUID.randomUUID().toString().replace("-", "");
        Integer insertedId = null;
        try {
            JsonObject request = Json.createObjectBuilder()
                    .add("name", name)
                    .add("type", "Fire")
                    .build();
            Pokemon inserted = pokemon(post(request));
            insertedId = inserted.id();

            assertThat(insertedId, greaterThanOrEqualTo(20));
            assertThat(inserted, is(new Pokemon(insertedId, name, "Fire")));
            assertThat(count(), is(expectedCount + 1));
            assertThat(pokemon(get("/pokemon/get/" + name)), is(inserted));
            assertThat(delete(insertedId), is("Deleted: 1 values"));
            insertedId = null;
        } finally {
            if (insertedId != null) {
                delete(insertedId);
            }
        }
        assertThat(count(), is(expectedCount));
    }

    @Test
    void updatesPokemon() {
        int expectedCount = count();
        String originalName = "E2E" + UUID.randomUUID().toString().replace("-", "");
        String updatedName = originalName + "Updated";
        Integer insertedId = null;
        try {
            JsonObject request = Json.createObjectBuilder()
                    .add("name", originalName)
                    .add("type", "Fire")
                    .build();
            insertedId = pokemon(post(request)).id();

            JsonObject update = Json.createObjectBuilder()
                    .add("name", updatedName)
                    .add("type", "Water")
                    .build();
            Pokemon updated = pokemon(put(insertedId, update));

            assertThat(updated, is(new Pokemon(insertedId, updatedName, "Water")));
            assertThat(count(), is(expectedCount + 1));
            assertNotFound("/pokemon/get/" + originalName);
            assertThat(pokemon(get("/pokemon/get/" + updatedName)), is(updated));
        } finally {
            if (insertedId != null) {
                delete(insertedId);
            }
        }
        assertThat(count(), is(expectedCount));
    }

    @Test
    void returnsNotFoundWhenUpdatingUnknownId() {
        JsonObject update = Json.createObjectBuilder()
                .add("name", "Missing")
                .add("type", "Water")
                .build();
        try (Http1ClientResponse response = client.put("/pokemon/" + Integer.MAX_VALUE)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(update.toString())) {
            assertThat("Unexpected response from " + response.lastEndpointUri(), response.status().code(), is(404));
        }
    }

    @Test
    void returnsZeroWhenDeletingUnknownId() {
        assertThat(delete(Integer.MAX_VALUE), is("Deleted: 0 values"));
    }

    @Test
    void rejectsNullName() {
        assertBadRequest(Json.createObjectBuilder().addNull("name").add("type", "Fire").build());
    }

    @Test
    void rejectsBlankName() {
        assertBadRequest(Json.createObjectBuilder().add("name", "  ").add("type", "Fire").build());
    }

    @Test
    void rejectsNullType() {
        assertBadRequest(Json.createObjectBuilder().add("name", "Charmander").addNull("type").build());
    }

    @Test
    void rejectsBlankType() {
        assertBadRequest(Json.createObjectBuilder().add("name", "Charmander").add("type", "  ").build());
    }

    private static String successful(Http1ClientResponse response) {
        String body = response.as(String.class);
        assertThat("Unexpected response from " + response.lastEndpointUri() + ": " + body,
                   response.status().code(),
                   is(200));
        return body;
    }

    private static List<Pokemon> pokemonList(String response) {
        JsonArray array = json(response).asJsonArray();
        return array.stream().map(JsonValue::asJsonObject).map(PokemonApplicationTest::pokemon).toList();
    }

    private static Pokemon pokemon(String response) {
        return pokemon(json(response).asJsonObject());
    }

    private static Pokemon pokemon(JsonObject json) {
        return new Pokemon(json.getInt("id"), json.getString("name"), json.getString("type"));
    }

    private static JsonValue json(String value) {
        try (var reader = Json.createReader(new StringReader(value))) {
            return reader.readValue();
        }
    }

    private int count() {
        return Integer.parseInt(get("/pokemon/count"));
    }

    private String get(String path) {
        try (Http1ClientResponse response = client.get(path).request()) {
            return successful(response);
        }
    }

    private void assertNotFound(String path) {
        try (Http1ClientResponse response = client.get(path).request()) {
            assertThat("Unexpected response from " + response.lastEndpointUri(), response.status().code(), is(404));
        }
    }

    private String post(JsonObject request) {
        try (Http1ClientResponse response = client.post("/pokemon")
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(request.toString())) {
            return successful(response);
        }
    }

    private String put(int id, JsonObject request) {
        try (Http1ClientResponse response = client.put("/pokemon/" + id)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(request.toString())) {
            return successful(response);
        }
    }

    private String delete(int id) {
        try (Http1ClientResponse response = client.delete("/pokemon/" + id).request()) {
            return successful(response);
        }
    }

    private void assertBadRequest(JsonObject request) {
        int expectedCount = count();
        try (Http1ClientResponse response = client.post("/pokemon")
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(request.toString())) {
            assertThat("Unexpected response from " + response.lastEndpointUri(), response.status().code(), is(400));
        }
        assertThat(count(), is(expectedCount));
    }

    private record Pokemon(int id, String name, String type) {
    }

    private static final class PokemonPostgresContainer
            extends JdbcDatabaseContainer<PokemonPostgresContainer> {
        private static final int POSTGRES_PORT = 5432;

        private String databaseName = "test";
        private String username = "test";
        private String password = "test";

        private PokemonPostgresContainer(Future<String> image) {
            super(image);
            addExposedPort(POSTGRES_PORT);
            waitingFor(Wait.forListeningPort()
                               .withStartupTimeout(Duration.ofMinutes(5)));
        }

        @Override
        protected void configure() {
            addEnv("POSTGRES_DB", databaseName);
            addEnv("POSTGRES_USER", username);
            addEnv("POSTGRES_PASSWORD", password);
        }

        @Override
        public PokemonPostgresContainer withDatabaseName(String databaseName) {
            this.databaseName = databaseName;
            return this;
        }

        @Override
        public PokemonPostgresContainer withUsername(String username) {
            this.username = username;
            return this;
        }

        @Override
        public PokemonPostgresContainer withPassword(String password) {
            this.password = password;
            return this;
        }

        @Override
        public String getDriverClassName() {
            return "org.postgresql.Driver";
        }

        @Override
        public String getJdbcUrl() {
            return "jdbc:postgresql://%s:%d/%s?quoteReturningIdentifiers=false"
                    .formatted(getHost(), getMappedPort(POSTGRES_PORT), databaseName);
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        protected String getTestQueryString() {
            return "SELECT 1";
        }

        @Override
        public void start() {
            super.start();
            System.setProperty("data.url", getJdbcUrl());
        }
    }
}

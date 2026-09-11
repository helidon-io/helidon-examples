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
package io.helidon.examples.declarative.data.jdbc;

import java.io.StringReader;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.data.DataException;
import io.helidon.data.NoResultException;
import io.helidon.examples.declarative.data.jdbc.model.PokemonRepository;
import io.helidon.examples.declarative.data.jdbc.model.TypeRepository;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.transaction.TxException;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.testing.junit5.ServerTest;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Exercises the HTTP endpoints and generated JDBC repositories with Oracle Database Free.
 */
@Testcontainers(disabledWithoutDocker = true)
@ServerTest
class PokemonApplicationTest {
    private static final DockerImageName IMAGE =
            DockerImageName.parse("container-registry.oracle.com/database/free:23.26.3.0-lite");
    private static final Duration DATABASE_STARTUP_TIMEOUT = Duration.ofMinutes(5);

    @Container
    @SuppressWarnings("resource")
    private static final OracleContainer CONTAINER = new OracleContainer();

    // The /pokemon/all query orders by name, so this fixture follows name order rather than identifier order.
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

    @Test
    void queriesPokemon() {
        assertThat(count(), is(12));
        assertThat(pokemonList(get("/pokemon/all")), is(SEEDED_POKEMON));

        List<Pokemon> normalPokemon = List.of(new Pokemon(5, "Meowth", "Normal"),
                                              new Pokemon(4, "Snorlax", "Normal"));

        assertThat(pokemonList(get("/pokemon/type/Normal")), is(normalPokemon));
        assertThat(pokemonList(get("/pokemon/search/Normal")), is(normalPokemon));
        assertThat(pokemon(get("/pokemon/search/Normal/Meowth")),
                   is(new Pokemon(5, "Meowth", "Normal")));
    }

    @Test
    void returnsEmptyListForUnknownType() {
        assertThat(pokemonList(get("/pokemon/type/DoesNotExist")), is(List.of()));
    }

    @Test
    void returnsNotFoundForUnknownPokemon() {
        assertNotFound("/pokemon/get/DoesNotExist");
    }

    /**
     * Verifies that generated code binds a null repository argument as typed SQL NULL without losing the client.
     */
    @Test
    void bindsNullRepositoryArgumentAsSqlNull() {
        int expectedCount = count();
        PokemonRepository pokemonRepository = Services.get(PokemonRepository.class);

        assertThat(pokemonRepository.findByName(null).isEmpty(), is(true));
        assertThat(count(), is(expectedCount));
    }

    @Test
    void oneThrowsForUnknownTypeAndApplicationRemainsUsable() {
        int expectedCount = count();
        TypeRepository typeRepository = Services.get(TypeRepository.class);

        assertThrows(NoResultException.class, () -> typeRepository.getByName("DoesNotExist"));
        assertThat(count(), is(expectedCount));
    }

    @Test
    void markerSelectsHighestWeight() {
        assertThat(pokemon(get("/pokemon/get/Meowth")), is(new Pokemon(5, "Meowth", "Normal")));
        assertThat(pokemon(get("/pokemon/explicit-mapper/Meowth")),
                   is(new Pokemon(5, "LOW-WEIGHT EXPLICIT: Meowth", "Normal")));
    }

    @Test
    void insertsPokemonThroughTransactionalPath() {
        int expectedCount = count();
        String name = "E2E" + UUID.randomUUID().toString().replace("-", "");
        Integer insertedId = null;
        try {
            JsonObject request = Json.createObjectBuilder()
                    .add("name", name)
                    .add("type", "Fire")
                    .build();

            Pokemon inserted = pokemon(post("/pokemon", request.toString()));
            insertedId = inserted.id();

            assertThat("Expected a generated identifier of at least 20", insertedId, greaterThanOrEqualTo(20));
            assertThat(inserted, is(new Pokemon(insertedId, name, "Fire")));
            assertThat(count(), is(expectedCount + 1));
            assertThat(pokemon(get("/pokemon/get/" + name)), is(inserted));
        } finally {
            if (insertedId != null) {
                delete("/pokemon/" + insertedId);
            }
        }
        assertThat(count(), is(expectedCount));
    }

    @Test
    void updatesPokemonThroughTransactionalPath() {
        int expectedCount = count();
        String originalName = "E2E" + UUID.randomUUID().toString().replace("-", "");
        String updatedName = originalName + "Updated";
        Integer insertedId = null;
        try {
            JsonObject request = Json.createObjectBuilder()
                    .add("name", originalName)
                    .add("type", "Fire")
                    .build();
            insertedId = pokemon(post("/pokemon", request.toString())).id();

            JsonObject update = Json.createObjectBuilder()
                    .add("name", updatedName)
                    .add("type", "Water")
                    .build();
            Pokemon updated = pokemon(put("/pokemon/" + insertedId, update.toString()));

            assertThat(updated, is(new Pokemon(insertedId, updatedName, "Water")));
            assertThat(count(), is(expectedCount + 1));
            assertNotFound("/pokemon/get/" + originalName);
            assertThat(pokemon(get("/pokemon/get/" + updatedName)), is(updated));
        } finally {
            if (insertedId != null) {
                delete("/pokemon/" + insertedId);
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
    void rollsBackInsertWhenTransactionFails() {
        int expectedCount = count();
        String name = "E2E" + UUID.randomUUID().toString().replace("-", "");
        PokemonRepository pokemonRepository = Services.get(PokemonRepository.class);
        TypeRepository typeRepository = Services.get(TypeRepository.class);

        try {
            TxException failure = assertThrows(TxException.class, () -> Tx.transaction(() -> {
                var type = typeRepository.getByName("Fire");
                pokemonRepository.insert(name, type.id());
                throw new IllegalStateException("Deliberate rollback");
            }));

            assertThat(failure.getCause().getMessage(), is("Deliberate rollback"));
            assertThat(pokemonRepository.findByName(name).isEmpty(), is(true));
            assertThat(count(), is(expectedCount));
        } finally {
            pokemonRepository.findByName(name)
                    .ifPresent(pokemon -> pokemonRepository.deleteById(pokemon.id()));
        }
    }

    @Test
    void recoversAfterDuplicateNameConstraintViolation() {
        int expectedCount = count();
        String name = "E2E" + UUID.randomUUID().toString().replace("-", "");
        PokemonRepository pokemonRepository = Services.get(PokemonRepository.class);
        TypeRepository typeRepository = Services.get(TypeRepository.class);

        try {
            TxException failure = assertThrows(TxException.class, () -> Tx.transaction(() -> {
                var type = typeRepository.getByName("Fire");
                pokemonRepository.insert(name, type.id());
                pokemonRepository.insert("Pikachu", type.id());
                return null;
            }));

            assertThat(failure.getCause(), instanceOf(DataException.class));
            DataException cause = (DataException) failure.getCause();
            assertThat(cause.getMessage(), containsString("integrity-constraint violation"));
            assertThat(pokemonRepository.count(), is((long) expectedCount));
            assertThat(pokemonRepository.findByName(name).isEmpty(), is(true));
            assertThat(pokemonRepository.findByName("Pikachu").orElseThrow().type().name(), is("Electric"));
        } finally {
            pokemonRepository.findByName(name)
                    .ifPresent(pokemon -> pokemonRepository.deleteById(pokemon.id()));
        }
    }

    @Test
    void deletesPokemon() {
        int expectedCount = count();
        String name = "E2E" + UUID.randomUUID().toString().replace("-", "");
        Integer insertedId = null;
        try {
            JsonObject request = Json.createObjectBuilder()
                    .add("name", name)
                    .add("type", "Fire")
                    .build();
            insertedId = pokemon(post("/pokemon", request.toString())).id();

            assertThat(delete("/pokemon/" + insertedId), is("Deleted: 1 values"));
            insertedId = null;
            assertThat(count(), is(expectedCount));
        } finally {
            if (insertedId != null) {
                delete("/pokemon/" + insertedId);
            }
        }
    }

    @Test
    void returnsZeroWhenDeletingUnknownId() {
        assertThat(delete("/pokemon/" + Integer.MAX_VALUE), is("Deleted: 0 values"));
    }

    @Test
    void rejectsNullNameBeforeJdbc() {
        assertBadRequest(Json.createObjectBuilder()
                                 .addNull("name")
                                 .add("type", "Fire")
                                 .build());
    }

    @Test
    void rejectsBlankNameBeforeJdbc() {
        assertBadRequest(Json.createObjectBuilder()
                                 .add("name", "  ")
                                 .add("type", "Fire")
                                 .build());
    }

    @Test
    void rejectsNullTypeBeforeJdbc() {
        assertBadRequest(Json.createObjectBuilder()
                                 .add("name", "Charmander")
                                 .addNull("type")
                                 .build());
    }

    @Test
    void rejectsBlankTypeBeforeJdbc() {
        assertBadRequest(Json.createObjectBuilder()
                                 .add("name", "Charmander")
                                 .add("type", "  ")
                                 .build());
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
            assertThat("Unexpected response from " + response.lastEndpointUri(),
                       response.status().code(),
                       is(404));
        }
    }

    private String post(String path, String body) {
        try (Http1ClientResponse response = client.post(path)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(body)) {
            return successful(response);
        }
    }

    private String put(String path, String body) {
        try (Http1ClientResponse response = client.put(path)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(body)) {
            return successful(response);
        }
    }

    private String delete(String path) {
        try (Http1ClientResponse response = client.delete(path).request()) {
            return successful(response);
        }
    }

    private void assertBadRequest(JsonObject request) {
        int expectedCount = count();
        try (Http1ClientResponse response = client.post("/pokemon")
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(request.toString())) {
            assertThat("Unexpected response from " + response.lastEndpointUri(),
                       response.status().code(),
                       is(400));
        }
        assertThat(count(), is(expectedCount));
    }

    private record Pokemon(int id, String name, String type) {
    }

    private static final class OracleContainer extends GenericContainer<OracleContainer> {
        private static final int ORACLE_PORT = 1521;

        private OracleContainer() {
            super(IMAGE);
            withEnv("ORACLE_PWD", "oracle123");
            withExposedPorts(ORACLE_PORT);
            withCopyFileToContainer(MountableFile.forClasspathResource("setup-user.sql"),
                                    "/opt/oracle/scripts/startup/01-setup-user.sql");
            withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"),
                                    "/opt/helidon/schema.sql");
            withCopyFileToContainer(MountableFile.forClasspathResource("run-schema.sql"),
                                    "/opt/helidon/run-schema.sql");
            withStartupAttempts(3);
            waitingFor(Wait.forSuccessfulCommand(
                    "sqlplus -s -L pokemon/changeit@//localhost:1521/FREEPDB1 @/opt/helidon/run-schema.sql")
                               .withStartupTimeout(DATABASE_STARTUP_TIMEOUT));
        }

        @Override
        public void start() {
            super.start();
            System.setProperty("data.url", jdbcUrl());
            System.setProperty("helidon.serialFilter.pattern", "oracle.sql.converter.*");
        }

        private String jdbcUrl() {
            return "jdbc:oracle:thin:@%s:%d/FREEPDB1".formatted(getHost(), getMappedPort(ORACLE_PORT));
        }

    }
}

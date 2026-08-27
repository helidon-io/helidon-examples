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
import java.util.List;
import java.util.UUID;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.data.DataException;
import io.helidon.data.NoResultException;
import io.helidon.data.jdbc.JdbcClient;
import io.helidon.examples.declarative.data.jdbc.model.PokemonRepository;
import io.helidon.examples.declarative.data.jdbc.model.TypeRepository;
import io.helidon.service.registry.Service;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the H2-backed HTTP endpoints and generated JDBC repositories.
 */
@ServerTest
class PokemonApplicationTest {

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

    /**
     * Recreates and seeds the H2 schema before the HTTP tests run.
     */
    @BeforeAll
    static void initializeSchema() {
        JdbcClient jdbcClient = Services.getNamed(JdbcClient.class, Service.Named.DEFAULT_NAME);
        new SchemaInitializer(jdbcClient).initialize();
    }

    @Test
    void queriesPokemon() {
        // Verify GET /pokemon/count returns the size of the seeded data set.
        assertEquals(12, count());

        // Verify GET /pokemon/all returns every seeded Pokemon in the expected order.
        assertEquals(SEEDED_POKEMON, pokemonList(get("/pokemon/all")));

        List<Pokemon> normalPokemon = List.of(new Pokemon(5, "Meowth", "Normal"),
                                              new Pokemon(4, "Snorlax", "Normal"));

        // Verify GET /pokemon/type/{type} finds Pokemon using the type query.
        assertEquals(normalPokemon, pokemonList(get("/pokemon/type/Normal")));

        // Verify GET /pokemon/search/{term} binds one value to both named parameters.
        assertEquals(normalPokemon, pokemonList(get("/pokemon/search/Normal")));

        // Verify GET /pokemon/search/{type}/{name} binds both positional query parameters.
        assertEquals(new Pokemon(5, "Meowth", "Normal"),
                     pokemon(get("/pokemon/search/Normal/Meowth")));
    }

    @Test
    void returnsEmptyListForUnknownType() {
        // A list terminal represents a query with no rows as an empty JSON array.
        assertEquals(List.of(), pokemonList(get("/pokemon/type/DoesNotExist")));
    }

    @Test
    void returnsNotFoundForUnknownPokemon() {
        // Helidon maps an empty Optional response to HTTP 404.
        assertNotFound("/pokemon/get/DoesNotExist");
    }

    @Test
    void oneThrowsForUnknownTypeAndApplicationRemainsUsable() {
        int expectedCount = count();
        TypeRepository typeRepository = Services.get(TypeRepository.class);

        // A non-optional repository result uses one(), which rejects a query with no rows.
        assertThrows(NoResultException.class, () -> typeRepository.getByName("DoesNotExist"));

        // A successful endpoint query proves that the failed terminal released its JDBC resources.
        assertEquals(expectedCount, count());
    }

    @Test
    void usesConfiguredRowMappers() {
        // Verify GET /pokemon/get/{name} returns the Pokemon with the requested name.
        assertEquals(new Pokemon(5, "Meowth", "Normal"), pokemon(get("/pokemon/get/Meowth")));

        // Verify GET /pokemon/explicit-mapper/{name} applies the explicitly selected mapper.
        assertEquals(new Pokemon(5, "LOW-WEIGHT EXPLICIT: Meowth", "Normal"),
                     pokemon(get("/pokemon/explicit-mapper/Meowth")));
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

            // Verify POST /pokemon inserts and returns a Pokemon with a generated identifier.
            Pokemon inserted = pokemon(post("/pokemon", request.toString()));
            insertedId = inserted.id();

            assertTrue(insertedId >= 20, "Expected a generated identifier of at least 20");
            assertEquals(new Pokemon(insertedId, name, "Fire"), inserted);

            // Verify GET /pokemon/count reflects the inserted Pokemon.
            assertEquals(expectedCount + 1, count());

            // Verify a subsequent query can observe the committed Pokemon.
            assertEquals(inserted, pokemon(get("/pokemon/get/" + name)));
        } finally {
            if (insertedId != null) {
                // Remove the test Pokemon after validation or if validation failed after insertion.
                delete("/pokemon/" + insertedId);
            }
        }
        // Cleanup must restore the committed row count for tests that share this application.
        assertEquals(expectedCount, count());
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
                // Complete an insert before deliberately failing the surrounding transaction.
                pokemonRepository.insert(name, type.id());
                throw new IllegalStateException("Deliberate rollback");
            }));

            // Reaching the deliberate failure proves that the insert itself completed successfully.
            assertEquals("Deliberate rollback", failure.getCause().getMessage());
            // The generated repository must not observe the rolled-back row.
            assertTrue(pokemonRepository.findByName(name).isEmpty());
            assertEquals(expectedCount, count());
        } finally {
            // Protect later tests from contamination if rollback behavior regresses.
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
                // This successful insert must be rolled back when the following insert fails.
                pokemonRepository.insert(name, type.id());
                // Pikachu is seeded, and its name is protected by a unique constraint.
                pokemonRepository.insert("Pikachu", type.id());
                return null;
            }));

            // Verify that JDBC classified and translated the real database failure.
            DataException cause = assertInstanceOf(DataException.class, failure.getCause());
            assertTrue(cause.getMessage().contains("integrity-constraint violation"));

            // The first operation after the failure verifies that the connection and transaction state were released.
            assertEquals(expectedCount, pokemonRepository.count());
            // Neither the earlier insert nor the seeded row may change after rollback.
            assertTrue(pokemonRepository.findByName(name).isEmpty());
            assertEquals("Electric", pokemonRepository.findByName("Pikachu").orElseThrow().type().name());
        } finally {
            // Protect later tests from contamination if rollback behavior regresses.
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
            // Each delete test owns its fixture and does not depend on test execution order.
            JsonObject request = Json.createObjectBuilder()
                    .add("name", name)
                    .add("type", "Fire")
                    .build();
            insertedId = pokemon(post("/pokemon", request.toString())).id();

            // Verify DELETE /pokemon/{id} removes the inserted Pokemon.
            assertEquals("Deleted: 1 values", delete("/pokemon/" + insertedId));
            insertedId = null;

            // Verify GET /pokemon/count returns to its value before the test.
            assertEquals(expectedCount, count());
        } finally {
            if (insertedId != null) {
                // Remove the test Pokemon if validation failed before deletion completed.
                delete("/pokemon/" + insertedId);
            }
        }
    }

    @Test
    void returnsZeroWhenDeletingUnknownId() {
        // An update that matches no row succeeds with update count zero.
        assertEquals("Deleted: 0 values", delete("/pokemon/" + Integer.MAX_VALUE));
    }

    @Test
    void rejectsNullNameBeforeJdbc() {
        // Reject a null name before the request reaches the repository.
        assertBadRequest(Json.createObjectBuilder()
                                 .addNull("name")
                                 .add("type", "Fire")
                                 .build());
    }

    @Test
    void rejectsBlankNameBeforeJdbc() {
        // Reject a blank name before the request reaches the repository.
        assertBadRequest(Json.createObjectBuilder()
                                 .add("name", "  ")
                                 .add("type", "Fire")
                                 .build());
    }

    @Test
    void rejectsNullTypeBeforeJdbc() {
        // Reject a null type before the request reaches the repository.
        assertBadRequest(Json.createObjectBuilder()
                                 .add("name", "Charmander")
                                 .addNull("type")
                                 .build());
    }

    @Test
    void rejectsBlankTypeBeforeJdbc() {
        // Reject a blank type before the request reaches the repository.
        assertBadRequest(Json.createObjectBuilder()
                                 .add("name", "Charmander")
                                 .add("type", "  ")
                                 .build());
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
            assertEquals(404,
                         response.status().code(),
                         () -> "Unexpected response from " + response.lastEndpointUri());
        }
    }

    private String post(String path, String body) {
        try (Http1ClientResponse response = client.post(path)
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
            assertEquals(400,
                         response.status().code(),
                         () -> "Unexpected response from " + response.lastEndpointUri());
        }
        // Rejected input must not change committed state.
        assertEquals(expectedCount, count());
    }

    private static String successful(Http1ClientResponse response) {
        String body = response.as(String.class);
        assertEquals(200,
                     response.status().code(),
                     () -> "Unexpected response from " + response.lastEndpointUri() + ": " + body);
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

    private record Pokemon(int id, String name, String type) {
    }
}

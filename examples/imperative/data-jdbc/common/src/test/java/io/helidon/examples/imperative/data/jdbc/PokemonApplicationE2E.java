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
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PokemonApplicationE2E {

    private static final String BASE_URL = normalizedBaseUrl(System.getProperty("base-url", "http://localhost:8080"));
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
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

    private final Http1Client client = Http1Client.builder()
            .baseUri(BASE_URL)
            .connectTimeout(REQUEST_TIMEOUT)
            .readTimeout(REQUEST_TIMEOUT)
            .build();

    @Test
    void validateRunningApplication() throws Exception {
        // Wait for GET /pokemon/count to confirm that the application is ready.
        awaitApplication();

        // Verify GET /pokemon/count returns the size of the seeded data set.
        assertEquals(12, count());

        // Verify GET /pokemon/all returns every seeded Pokemon in the expected order.
        assertEquals(SEEDED_POKEMON, pokemonList(get("/pokemon/all")));

        List<Pokemon> normalPokemon = List.of(new Pokemon(5, "Meowth", "Normal"),
                                              new Pokemon(4, "Snorlax", "Normal"));

        // Verify GET /pokemon/type/{type} finds Pokemon using the derived repository query.
        assertEquals(normalPokemon, pokemonList(get("/pokemon/type/Normal")));

        // Verify GET /pokemon/search/{type} finds Pokemon using the declared query.
        assertEquals(normalPokemon, pokemonList(get("/pokemon/search/Normal")));

        // Verify GET /pokemon/get/{name} returns the Pokemon with the requested name.
        assertEquals(new Pokemon(5, "Meowth", "Normal"), pokemon(get("/pokemon/get/Meowth")));

        // Verify GET /pokemon/explicit-mapper/{name} applies the explicitly selected mapper.
        assertEquals(new Pokemon(5, "LOW-WEIGHT EXPLICIT: Meowth", "Normal"),
                     pokemon(get("/pokemon/explicit-mapper/Meowth")));

        // Verify GET /pokemon/search/{type}/{name} binds both positional query parameters.
        assertEquals(new Pokemon(5, "Meowth", "Normal"),
                     pokemon(get("/pokemon/search/Normal/Meowth")));

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
            assertEquals(13, count());

            // Verify DELETE /pokemon/{id} removes the inserted Pokemon.
            String deleted = delete("/pokemon/" + insertedId);
            assertEquals("Deleted: 1 values", deleted);
            insertedId = null;

            // Verify GET /pokemon/count returns to the seeded data-set size.
            assertEquals(12, count());
        } finally {
            if (insertedId != null) {
                // Remove the test Pokemon with DELETE /pokemon/{id} if validation failed after insertion.
                delete("/pokemon/" + insertedId);
            }
        }
    }

    /**
     * Invokes {@code GET /pokemon/count} until the application is ready or the startup deadline expires.
     *
     * @throws InterruptedException if interrupted while waiting between attempts
     */
    private void awaitApplication() throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        Throwable lastFailure = null;
        while (System.nanoTime() < deadline) {
            try (Http1ClientResponse response = client.get("/pokemon/count").request()) {
                if (response.status().code() == 200) {
                    return;
                }
                lastFailure = new IllegalStateException("HTTP status " + response.status().code());
            } catch (RuntimeException e) {
                lastFailure = e;
            }
            Thread.sleep(250);
        }
        fail("Application did not become ready at " + BASE_URL, lastFailure);
    }

    /**
     * Invokes {@code GET /pokemon/count} and returns the current number of Pokemon.
     *
     * @return current number of Pokemon
     */
    private int count() {
        return Integer.parseInt(get("/pokemon/count"));
    }

    /**
     * Invokes a GET endpoint and verifies that it succeeds.
     *
     * @param path endpoint path
     * @return response body
     */
    private String get(String path) {
        try (Http1ClientResponse response = client.get(path).request()) {
            return successful(response);
        }
    }

    /**
     * Invokes a POST endpoint with a JSON request and verifies that it succeeds.
     *
     * @param path endpoint path
     * @param body JSON request body
     * @return response body
     */
    private String post(String path, String body) {
        try (Http1ClientResponse response = client.post(path)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(body)) {
            return successful(response);
        }
    }

    /**
     * Invokes a DELETE endpoint and verifies that it succeeds.
     *
     * @param path endpoint path
     * @return response body
     */
    private String delete(String path) {
        try (Http1ClientResponse response = client.delete(path).request()) {
            return successful(response);
        }
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
        return array.stream().map(JsonValue::asJsonObject).map(PokemonApplicationE2E::pokemon).toList();
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

    private static String normalizedBaseUrl(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record Pokemon(int id, String name, String type) {
    }
}

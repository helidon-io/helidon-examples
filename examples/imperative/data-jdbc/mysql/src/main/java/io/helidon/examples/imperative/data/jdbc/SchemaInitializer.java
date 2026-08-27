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

import java.util.List;

import io.helidon.data.jdbc.JdbcClient;

/**
 * Creates and populates the example schema before HTTP routing starts.
 */
final class SchemaInitializer {

    private static final List<String> SCHEMA_STATEMENTS = List.of(
            "DROP TABLE IF EXISTS POKEMON",
            "DROP TABLE IF EXISTS TYPE",
            "CREATE TABLE TYPE (ID INTEGER NOT NULL PRIMARY KEY, NAME VARCHAR(255) UNIQUE NOT NULL)",
            """
                    CREATE TABLE POKEMON (
                        ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        NAME VARCHAR(255) UNIQUE NOT NULL,
                        TYPE_ID INTEGER NOT NULL
                    ) AUTO_INCREMENT=20
                    """,
            "ALTER TABLE POKEMON ADD CONSTRAINT FK_POKEMON_TYPE_ID FOREIGN KEY (TYPE_ID) REFERENCES TYPE (ID)");

    private static final List<String> TYPE_NAMES = List.of(
            "Normal",
            "Fighting",
            "Flying",
            "Poison",
            "Ground",
            "Rock",
            "Bug",
            "Ghost",
            "Steel",
            "Fire",
            "Water",
            "Grass",
            "Electric",
            "Psychic",
            "Ice",
            "Dragon",
            "Dark",
            "Fairy");

    private static final List<PokemonSeed> POKEMON = List.of(
            new PokemonSeed(1, "Pikachu", 13),
            new PokemonSeed(2, "Raichu", 13),
            new PokemonSeed(3, "Machop", 2),
            new PokemonSeed(4, "Snorlax", 1),
            new PokemonSeed(5, "Meowth", 1),
            new PokemonSeed(6, "Magikarp", 11),
            new PokemonSeed(7, "Ekans", 4),
            new PokemonSeed(8, "Arbok", 4),
            new PokemonSeed(9, "Sandshrew", 5),
            new PokemonSeed(10, "Sandslash", 5),
            new PokemonSeed(11, "Raikou", 13),
            new PokemonSeed(12, "Regirock", 6));

    private SchemaInitializer() {
    }

    /**
     * Recreates the example schema and inserts its sample data.
     *
     * @param jdbcClient registry managed JDBC client
     */
    static void initialize(JdbcClient jdbcClient) {
        for (String sql : SCHEMA_STATEMENTS) {
            jdbcClient.create(sql).execute();
        }
        for (int index = 0; index < TYPE_NAMES.size(); index++) {
            jdbcClient.create("INSERT INTO TYPE (ID, NAME) VALUES (?, ?)")
                    .bind(1, index + 1)
                    .bind(2, TYPE_NAMES.get(index))
                    .execute();
        }
        for (PokemonSeed pokemon : POKEMON) {
            jdbcClient.create("INSERT INTO POKEMON (ID, NAME, TYPE_ID) VALUES (?, ?, ?)")
                    .bind(1, pokemon.id())
                    .bind(2, pokemon.name())
                    .bind(3, pokemon.typeId())
                    .execute();
        }
    }

    /**
     * Sample Pokemon row inserted during schema setup.
     *
     * @param id Pokemon identifier
     * @param name Pokemon name
     * @param typeId type identifier
     */
    private record PokemonSeed(int id, String name, int typeId) {
    }
}


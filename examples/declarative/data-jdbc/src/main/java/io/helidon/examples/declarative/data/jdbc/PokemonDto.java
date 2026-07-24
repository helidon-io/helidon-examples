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

import java.util.Optional;

import io.helidon.examples.declarative.data.jdbc.model.Pokemon;
import io.helidon.json.binding.Json;

/**
 * HTTP representation of a Pokémon.
 *
 * @param id database identifier, empty when a client requests an insert
 * @param name Pokémon name
 * @param type Pokémon type name
 */
@Json.Entity
public record PokemonDto(Optional<Integer> id,
                         String name,
                         String type) {

    /**
     * Keeps the JDBC model separate from the representation sent over HTTP.
     *
     * @param pokemon database projection
     * @return HTTP representation
     */
    static PokemonDto create(Pokemon pokemon) {
        return new PokemonDto(Optional.of(pokemon.id()), pokemon.name(), pokemon.type().name());
    }
}

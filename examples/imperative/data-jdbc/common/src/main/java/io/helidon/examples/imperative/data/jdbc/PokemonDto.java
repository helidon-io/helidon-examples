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

import java.util.Optional;

import io.helidon.examples.imperative.data.jdbc.model.Pokemon;
import io.helidon.json.binding.Json;

/**
 * JSON HTTP representation of a Pokemon.
 *
 * @param id database identifier, empty when a client requests an insert
 * @param name Pokemon name
 * @param type Pokemon type name
 */
@Json.Entity
public record PokemonDto(Optional<Integer> id,
                         String name,
                         String type) {

    /**
     * Converts a database record to its HTTP representation.
     *
     * @param pokemon database record
     * @return HTTP representation
     */
    static PokemonDto create(Pokemon pokemon) {
        return new PokemonDto(Optional.of(pokemon.id()), pokemon.name(), pokemon.type().name());
    }
}

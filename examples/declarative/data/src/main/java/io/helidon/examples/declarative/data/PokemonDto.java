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
package io.helidon.examples.declarative.data;

import java.util.Optional;

import io.helidon.examples.declarative.data.model.Pokemon;
import io.helidon.json.binding.Json;

/**
 * A pokémon DAO object.
 * <p>
 * Used to map HTTP request Pokemon data.
 *
 * @param id   id of the Pokemon, may be empty when creating a new one
 * @param name the name of the pokémon
 * @param type the name of the pokémon type
 */
@Json.Entity
public record PokemonDto(Optional<Integer> id,
                         String name,
                         String type) {

    // separation of data layer from API layer
    static PokemonDto create(Pokemon pokemon) {
        return new PokemonDto(Optional.of(pokemon.getId()), pokemon.getName(), pokemon.getType().getName());
    }
}

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
package io.helidon.examples.microprofile.data;

import java.util.List;
import java.util.Optional;

import io.helidon.data.Data;

/**
 * {@link Pokemon} entity data repository interface.
 * <p>
 * {@code PokemonRepository} interface acts as a data access layer, encapsulating the logic for interacting
 * with the {@link Pokemon} entity data, and also provides basic CRUD (Create, Read, Update, Delete) operations
 * for the {@link Pokemon} entity.
 *
 * @see Data.CrudRepository
 * @see Pokemon
 */
@Data.Repository
public interface PokemonRepository extends Data.CrudRepository<Pokemon, Integer> {

    /**
     * Retrieves a list of {@link Pokemon} entities ordered by its {@code name}.
     * <p>
     * Query defined by method name: return list of {@link Pokemon} entities ordered by {@code name} property.
     *
     * @return a list of {@link Pokemon} entities
     */
    List<Pokemon> listOrderByName();

    /**
     * Retrieves a list of {@link Pokemon} entities associated with a specific {@link Type} name.
     * <p>
     * Query defined by method name: return unordered list of {@link Pokemon} entities with {@code type.name}
     * property matching the {@code typeName} method argument.
     *
     * @param typeName the name of the {@link Type}
     * @return a list of {@link Pokemon} entities with the specified type name
     */
    List<Pokemon> listByType_Name(String typeName);

    /**
     * Retrieves a {@link Pokemon} entity by its name.
     * <p>
     * Query defined by method name: return single {@link Pokemon} entity with {@code name} property
     * matching the {@code name} method argument or {@link Optional#empty()} when no such entity exists.
     *
     * @param name the name of the pet
     * @return an {@link Optional} containing the {@link Pokemon} entity if found, or an empty
     *         {@link Optional} if not found
     * @throws io.helidon.transaction.TxException when multiple entities were found
     */
    Optional<Pokemon> findByName(String name);

}

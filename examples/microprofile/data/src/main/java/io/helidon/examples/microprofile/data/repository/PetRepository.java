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
package io.helidon.examples.microprofile.data.repository;

import java.util.List;
import java.util.Optional;

import io.helidon.data.Data;
import io.helidon.data.PageRequest;
import io.helidon.data.Slice;
import io.helidon.examples.microprofile.data.model.Pet;

/**
 * {@link Pet} entity data repository interface.
 * <p>
 * {@code PetRepository} interface acts as a data access layer, encapsulating the logic for interacting
 * with the {@link Pet} entity data, and also provides basic CRUD (Create, Read, Update, Delete) operations
 * for the {@link Pet} entity.
 *
 * @see Data.CrudRepository
 * @see Pet
 */
@Data.Repository
public interface PetRepository extends Data.CrudRepository<Pet, Integer> {

    /**
     * Retrieves a list of {@link Pet} entities ordered by its {@code name}.
     * <p>
     * Query defined by method name: return list of {@link Pet} entities ordered by {@code name} property.
     *
     * @return a list of {@link Pet} entities
     */
    List<Pet> listOrderByName();

    /**
     * Retrieves paginated list of {@link Pet} entities ordered by its {@code name}.
     * <p>
     * Query defined by method name: return list of {@link Pet} entities ordered by {@code name} property.
     *
     * @param request pageable query result request
     * @return paginated list of {@link Pet} entities
     */
    Slice<Pet> listOrderByName(PageRequest request);

    /**
     * Retrieves a list of {@link Pet} entities associated with a specific {@code Breed} name.
     * <p>
     * Query defined by method name: return unordered list of {@link Pet} entities with {@code breed.name}
     * property matching the {@code breedName} method argument.
     *
     * @param breedName the name of the {@code Breed}
     * @return a list of {@link Pet} entities with the specified breed name
     */
    List<Pet> listByBreed_Name(String breedName);

    /**
     * Retrieves a {@link Pet} entity by its name.
     * <p>
     * Query defined by method name: return single {@link Pet} entity with {@code name} property
     * matching the {@code name} method argument or {@link Optional#empty()} when no such entity exists.
     *
     * @param name the name of the pet
     * @return an {@link Optional} containing the {@link Pet} entity if found, or an empty
     *         {@link Optional} if not found
     * @throws io.helidon.transaction.TxException when multiple entities were found
     */
    Optional<Pet> findByName(String name);

}

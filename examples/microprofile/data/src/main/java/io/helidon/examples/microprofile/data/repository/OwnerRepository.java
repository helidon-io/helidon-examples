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

import io.helidon.data.Data;
import io.helidon.examples.microprofile.data.model.Owner;

/**
 * {@link Owner} entity data repository interface.
 * <p>
 * {@code OwnerRepository} interface acts as a data access layer, encapsulating the logic for interacting
 * with the {@link Owner} entity data.
 *
 * @see Owner
 */
@Data.Repository
public interface OwnerRepository extends Data.CrudRepository<Owner, Integer> {

    /**
     * Retrieves a {@link Owner} entity by its name.
     * <p>
     * This method executes a custom query to find an owner with the specified name.
     * If no owner is found with the given name, it throws a {@link io.helidon.transaction.TxException}.
     *
     * @param name the name of the owner to retrieve
     * @return the {@link Owner} entity with the specified name
     * @throws io.helidon.transaction.TxException if no owner is found with the given name
     */
    @Data.Query("SELECT o FROM Owner o WHERE o.name = :name")
    Owner ownerByName(String name);

    /**
     * Retrieves a list of {@link Owner} entities ordered by their names.
     * <p>
     * This method executes a custom query to retrieve a list of owners sorted alphabetically by name.
     *
     * @return a list of {@link Owner} entities ordered by name
     */
    @Data.Query("SELECT o FROM Owner o ORDER BY o.name")
    List<Owner> ownersOrderedByName();


    /**
     * Retrieves a list of owner names whose pets have a breed with the specified name.
     * <p>
     * This method executes a custom query to find the names of owners whose pets have a breed
     * matching the provided breed name.
     *
     * @param breedName the name of the breed to search for
     * @return a list of owner names whose pets have a breed with the specified name
     */
    @Data.Query("SELECT DISTINCT p.owner.name FROM Pet p WHERE p.breed.name = :breedName")
    List<String> queryNameByPetBreed(String breedName);

}

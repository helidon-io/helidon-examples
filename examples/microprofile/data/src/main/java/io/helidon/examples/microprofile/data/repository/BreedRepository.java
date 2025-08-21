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
import io.helidon.examples.microprofile.data.model.Breed;

/**
 * {@link Breed} entity data repository interface.
 * <p>
 * <p>
 * This interface acts as a data access layer, encapsulating the logic for interacting
 * with the {@link Breed} entity data.
 *
 * @see Breed
 */
@Data.Repository
public interface BreedRepository extends Data.CrudRepository<Breed, Integer> {

    /**
     * Retrieves a {@link Breed} entity by its name.
     *
     * @param name the name of the breed
     * @return the {@link Breed} entity if found
     * @throws io.helidon.transaction.TxException when Breed with provided name was not found
     */
    Breed getByName(String name);

    /**
     * Retrieves a list of {@link Breed} entities ordered by its {@code name}.
     * <p>
     * The query is defined by the method name and returns a list of {@link Breed} entities
     * ordered by the {@code name} property.
     *
     * @return a list of {@link Breed} entities
     */
    List<Breed> listOrderByName();

}

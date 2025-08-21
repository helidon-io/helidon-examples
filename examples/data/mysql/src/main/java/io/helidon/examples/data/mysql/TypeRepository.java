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
package io.helidon.examples.data.mysql;

import io.helidon.data.Data;

/**
 * {@link Type} entity data repository interface.
 * <p>
 * {@code TypeRepository} interface acts as a data access layer, encapsulating the logic for interacting
 * with the {@link Type} entity data and provides user defined operations for the {@link Type} entity.
 *
 * @see Data.GenericRepository
 * @see Type
 */
@Data.Repository
public interface TypeRepository extends Data.GenericRepository<Type, Integer> {

    /**
     * Retrieves a {@link Type} entity by its name.
     *
     * @param name the name of the breed
     * @return the {@link Type} entity if found
     * @throws io.helidon.transaction.TxException when Breed with provided name was not found
     */
    Type getByName(String name);

}

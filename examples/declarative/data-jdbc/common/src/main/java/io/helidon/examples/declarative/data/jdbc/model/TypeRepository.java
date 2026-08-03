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
package io.helidon.examples.declarative.data.jdbc.model;

import io.helidon.data.Data;
import io.helidon.data.jdbc.Jdbc;

/**
 * Defines the declarative JDBC query for Pokémon types.
 */
@Data.Repository
@Data.Provider("jdbc")
public interface TypeRepository {

    /**
     * Retrieves a type by name.
     *
     * @param name type name
     * @return matching type
     */
    @Jdbc.Statement("SELECT ID AS id, NAME AS name FROM TYPE WHERE NAME = :name")
    @Jdbc.Execution(Jdbc.ExecutionType.QUERY)
    Type getByName(String name);
}

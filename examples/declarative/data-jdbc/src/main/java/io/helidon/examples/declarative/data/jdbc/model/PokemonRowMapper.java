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

import io.helidon.data.jdbc.JdbcClient;
import io.helidon.service.registry.Service;

/**
 * Maps one joined Pokémon and type row to the nested {@link Pokemon} model.
 * <p>
 * Repository methods select this mapper by its exact {@code JdbcClient.RowMapper<Pokemon>} service contract using the
 * {@code @Jdbc.RowMapper} marker annotation. The generated repository receives this singleton from the
 * Service Registry.
 */
@Service.Singleton
public final class PokemonRowMapper implements JdbcClient.RowMapper<Pokemon> {

    /**
     * Creates the stateless mapper service.
     */
    public PokemonRowMapper() {
    }

    /**
     * Maps the current row to a Pokémon.
     *
     * @param row current callback-scoped row
     * @return mapped Pokémon
     */
    @Override
    public Pokemon map(JdbcClient.Row row) {
        Type type = new Type(row.required("typeId", Integer.class),
                             row.required("typeName", String.class));
        return new Pokemon(row.required("id", Integer.class),
                           row.required("name", String.class),
                           type);
    }
}

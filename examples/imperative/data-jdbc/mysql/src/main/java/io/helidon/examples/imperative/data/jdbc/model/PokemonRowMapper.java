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
package io.helidon.examples.imperative.data.jdbc.model;

import io.helidon.common.Weight;
import io.helidon.common.Weighted;
import io.helidon.data.jdbc.JdbcClient;

/**
 * Maps joined Pokemon and type JDBC rows to the nested {@link Pokemon} model.
 */
@Weight(Weighted.DEFAULT_WEIGHT + 10)
public final class PokemonRowMapper implements JdbcClient.RowMapper<Pokemon> {

    /**
     * Maps the current row to a Pokemon.
     *
     * @param row row from the query result
     * @return mapped Pokemon
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

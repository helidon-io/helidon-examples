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
package io.helidon.examples.declarative.data.jdbc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import io.helidon.common.Weight;
import io.helidon.common.Weighted;
import io.helidon.data.jdbc.JdbcClient;
import io.helidon.examples.declarative.data.jdbc.model.Pokemon;
import io.helidon.examples.declarative.data.jdbc.model.PokemonAlternateRowMapper;
import io.helidon.examples.declarative.data.jdbc.model.PokemonRowMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

class PokemonMapperSelectionTest {

    @Test
    void higherWeightOverridesAlphabeticalServiceTypeOrder() {
        assertThat(PokemonAlternateRowMapper.class.getName(),
                   lessThan(PokemonRowMapper.class.getName()));
        double alternateWeight = PokemonAlternateRowMapper.class.getAnnotation(Weight.class).value();
        double preferredWeight = PokemonRowMapper.class.getAnnotation(Weight.class).value();

        assertThat(alternateWeight, is(Weighted.DEFAULT_WEIGHT - 10));
        assertThat(preferredWeight, is(Weighted.DEFAULT_WEIGHT + 10));
        assertThat(preferredWeight, greaterThan(alternateWeight));
    }

    @Test
    void alternateMapperProducesRecognizableResult() {
        JdbcClient.Row row = row(Map.of("id", 52,
                                        "name", "Meowth",
                                        "typeId", 1,
                                        "typeName", "Normal"));

        Pokemon preferred = new PokemonRowMapper().map(row);
        Pokemon alternate = new PokemonAlternateRowMapper().map(row);

        assertThat(preferred.name(), is("Meowth"));
        assertThat(alternate.name(), is("LOW-WEIGHT EXPLICIT: Meowth"));
        assertThat(alternate.type().name(), is("Normal"));
    }

    @Test
    void generatedBindingUsesWeightForMarkerAndConcreteTypeForExplicitSelection() throws Exception {
        String repository = generatedSource("model/PokemonRepository__Jdbc.java");

        assertThat(repository, containsString("JdbcClient.RowMapper<Pokemon> pokemonRowMapper"));
        assertThat(repository, containsString("PokemonAlternateRowMapper pokemonAlternateRowMapper"));
        assertThat(repository, containsString(".map(pokemonRowMapper).optional()"));
        assertThat(repository, containsString(".map(pokemonAlternateRowMapper).optional()"));
    }

    @Test
    void generatedRepositoryUsesCurrentBindingAndGeneratedKeyContracts() throws Exception {
        String repository = generatedSource("model/PokemonRepository__Jdbc.java");

        assertThat(repository, containsString("jdbcStatement.bindNull(1, JDBCType.VARCHAR)"));
        assertThat(repository, containsString("jdbcStatement.bindNull(2, JDBCType.VARCHAR)"));
        assertThat(repository, containsString("jdbcStatement.bind(1, term)"));
        assertThat(repository, containsString("jdbcStatement.bind(2, term)"));
        assertThat(repository, containsString("jdbcStatement.bind(1, typeName)"));
        assertThat(repository, containsString("jdbcStatement.bind(2, name)"));
        assertThat(repository,
                   containsString(".generatedKeys().addColumn(\"ID\").map("
                                          + "row -> row.required(1, Integer.class)).one()"));
    }

    @Test
    void generatedEndpointInterceptsTheTransactionalInsert() throws Exception {
        String endpoint = generatedSource("PokemonEndpoint__Intercepted.java");

        assertThat(endpoint, containsString("insertPokemon_0_invoker.invoke(pokemonDto)"));
    }

    private static JdbcClient.Row row(Map<String, Object> values) {
        return new JdbcClient.Row() {
            @Override
            public <T> Optional<T> optional(int index, Class<T> type) {
                throw new UnsupportedOperationException("This mapper uses column labels");
            }

            @Override
            public <T> Optional<T> optional(String label, Class<T> type) {
                return Optional.ofNullable(type.cast(values.get(label)));
            }

            @Override
            public <T> T required(int index, Class<T> type) {
                throw new UnsupportedOperationException("This mapper uses column labels");
            }

            @Override
            public <T> T required(String label, Class<T> type) {
                return optional(label, type)
                        .orElseThrow(() -> new IllegalStateException("Missing sample value: " + label));
            }
        };
    }

    private static String generatedSource(String relativePath) throws Exception {
        String generatedRoot = "target/generated-sources/annotations/io/helidon/examples/declarative/data/jdbc/";
        return Files.readString(Path.of(generatedRoot + relativePath));
    }
}

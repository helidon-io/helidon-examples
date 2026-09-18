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
package io.helidon.examples.imperative.data.jdbc.postgres;

import java.util.List;
import java.util.Optional;

import io.helidon.common.Api;
import io.helidon.data.Data;
import io.helidon.data.jdbc.JdbcClient;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;

/**
 * Stores Pokemon using a registry managed {@link JdbcClient}.
 */
@Service.Singleton
@SuppressWarnings({Api.SUPPRESS_PREVIEW, Api.SUPPRESS_INCUBATING})
final class PokemonStore {
    private static final JdbcClient.RowMapper<Type> TYPE_MAPPER =
            row -> new Type(row.get("id", Integer.class), row.get("name", String.class));

    private final JdbcClient jdbcClient;
    private final JdbcClient.RowMapper<Pokemon> pokemonRowMapper = new PokemonRowMapper();

    /**
     * Creates the store with the configured registry managed JDBC client.
     *
     * @param jdbcClient configured JDBC client
     */
    @Service.Inject
    PokemonStore(@Data.ProviderType("jdbc")
                 @Service.Named(Main.POKEMON_CLIENT)
                 JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Resolves the requested type and inserts the Pokemon in one local JDBC transaction.
     *
     * @param name Pokemon name
     * @param typeName type name
     * @return inserted Pokemon with its generated identifier
     */
    Pokemon insert(String name, String typeName) {
        return Tx.transaction(() -> {
            Type type = getTypeByName(typeName);
            int id = insertRow(name, type.id());
            return new Pokemon(id, name, type);
        });
    }

    /**
     * Resolves the new type and updates the Pokemon in one local JDBC transaction.
     *
     * @param id Pokemon identifier
     * @param name new Pokemon name
     * @param typeName new type name
     * @return updated Pokemon, or an empty optional when the identifier does not exist
     */
    Optional<Pokemon> update(int id, String name, String typeName) {
        return Tx.transaction(() -> {
            Type type = getTypeByName(typeName);
            long updated = updateRow(id, name, type.id());
            if (updated == 0) {
                return Optional.empty();
            }
            return Optional.of(new Pokemon(id, name, type));
        });
    }

    /**
     * Retrieves all Pokemon ordered by name.
     *
     * @return all Pokemon
     */
    List<Pokemon> listOrderByName() {
        String sql = """
                SELECT p.ID AS id,
                       p.NAME AS name,
                       p.TYPE_ID AS typeId,
                       t.NAME AS typeName
                FROM POKEMON p
                JOIN TYPE t ON t.ID = p.TYPE_ID
                ORDER BY p.NAME
                """;
        return jdbcClient.create(sql)
                .map(pokemonRowMapper)
                .list();
    }

    /**
     * Retrieves Pokemon having the requested type name.
     *
     * @param typeName type name
     * @return matching Pokemon
     */
    List<Pokemon> listByTypeName(String typeName) {
        String sql = """
                SELECT p.ID AS id,
                       p.NAME AS name,
                       p.TYPE_ID AS typeId,
                       t.NAME AS typeName
                FROM POKEMON p
                JOIN TYPE t ON t.ID = p.TYPE_ID
                WHERE t.NAME = ?
                ORDER BY p.NAME
                """;
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, typeName);
        return statement.map(pokemonRowMapper).list();
    }

    /**
     * Retrieves Pokemon whose name or type matches one search term.
     *
     * @param term Pokemon or type name
     * @return matching Pokemon
     */
    List<Pokemon> listByNameOrType(String term) {
        String sql = """
                SELECT p.ID AS id,
                       p.NAME AS name,
                       p.TYPE_ID AS typeId,
                       t.NAME AS typeName
                FROM POKEMON p
                JOIN TYPE t ON t.ID = p.TYPE_ID
                WHERE p.NAME = ? OR t.NAME = ?
                ORDER BY p.NAME
                """;
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, term);
        statement.bind(2, term);
        return statement.map(pokemonRowMapper).list();
    }

    /**
     * Retrieves a Pokemon by name.
     *
     * @param name Pokemon name
     * @return matching Pokemon, if present
     */
    Optional<Pokemon> findByName(String name) {
        String sql = """
                SELECT p.ID AS id,
                       p.NAME AS name,
                       p.TYPE_ID AS typeId,
                       t.NAME AS typeName
                FROM POKEMON p
                JOIN TYPE t ON t.ID = p.TYPE_ID
                WHERE p.NAME = ?
                """;
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, name);
        return statement.map(pokemonRowMapper).optional();
    }

    /**
     * Retrieves a Pokemon by type and name.
     *
     * @param typeName type name bound to the first position
     * @param name Pokemon name bound to the second position
     * @return matching Pokemon, if present
     */
    Optional<Pokemon> findByTypeAndName(String typeName, String name) {
        String sql = """
                SELECT p.ID AS id,
                       p.NAME AS name,
                       p.TYPE_ID AS typeId,
                       t.NAME AS typeName
                FROM POKEMON p
                JOIN TYPE t ON t.ID = p.TYPE_ID
                WHERE t.NAME = ? AND p.NAME = ?
                """;
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, typeName);
        statement.bind(2, name);
        return statement.map(pokemonRowMapper).optional();
    }

    /**
     * Counts all Pokemon rows.
     *
     * @return number of Pokemon
     */
    long count() {
        String sql = "SELECT COUNT(*) FROM POKEMON";
        return jdbcClient.create(sql)
                .map(long.class)
                .one();
    }

    /**
     * Deletes a Pokemon by identifier.
     *
     * @param id Pokemon identifier
     * @return number of deleted rows
     */
    long deleteById(int id) {
        String sql = "DELETE FROM POKEMON WHERE ID = ?";
        return jdbcClient.create(sql)
                .bind(1, id)
                .execute();
    }

    /**
     * Inserts a Pokemon and retrieves the identifier generated by the database.
     *
     * @param name Pokemon name
     * @param typeId type identifier
     * @return generated Pokemon identifier
     */
    int insertRow(String name, int typeId) {
        String sql = "INSERT INTO POKEMON (NAME, TYPE_ID) VALUES (?, ?)";
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, name);
        statement.bind(2, typeId);
        return statement.generatedKeys()
                .addColumn("ID")
                .map(row -> row.get(1, Integer.class))
                .one();
    }

    /**
     * Retrieves a Pokemon type by name.
     *
     * @param name type name
     * @return matching type
     */
    Type getTypeByName(String name) {
        String sql = "SELECT ID AS id, NAME AS name FROM TYPE WHERE NAME = ?";
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, name);
        return statement.map(TYPE_MAPPER).one();
    }

    private long updateRow(int id, String name, int typeId) {
        String sql = "UPDATE POKEMON SET NAME = ?, TYPE_ID = ? WHERE ID = ?";
        return jdbcClient.create(sql)
                .bind(1, name)
                .bind(2, typeId)
                .bind(3, id)
                .execute();
    }
}

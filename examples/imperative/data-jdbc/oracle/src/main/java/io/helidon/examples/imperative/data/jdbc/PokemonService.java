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
package io.helidon.examples.imperative.data.jdbc;

import java.util.List;
import java.util.Optional;

import io.helidon.data.jdbc.JdbcClient;
import io.helidon.examples.imperative.data.jdbc.model.Pokemon;
import io.helidon.examples.imperative.data.jdbc.model.PokemonAlternateRowMapper;
import io.helidon.examples.imperative.data.jdbc.model.PokemonRowMapper;
import io.helidon.examples.imperative.data.jdbc.model.Type;
import io.helidon.http.BadRequestException;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Exposes Pokemon operations using imperative HTTP routing and a standalone {@link JdbcClient}.
 */
@SuppressWarnings("helidon:api:preview")
final class PokemonService implements HttpService {
    private static final JdbcClient.RowMapper<Type> TYPE_MAPPER =
            row -> new Type(row.required("id", Integer.class), row.required("name", String.class));

    private final JdbcClient jdbcClient;
    private final JdbcClient.RowMapper<Pokemon> pokemonRowMapper = new PokemonRowMapper();
    private final JdbcClient.RowMapper<Pokemon> pokemonAlternateRowMapper = new PokemonAlternateRowMapper();

    /**
     * Creates the HTTP service with the standalone JDBC client.
     *
     * @param jdbcClient configured JDBC client
     */
    PokemonService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Registers the Pokemon HTTP endpoints.
     *
     * @param rules routing rules to update
     */
    @Override
    public void routing(HttpRules rules) {
        rules.get("/all", this::all)
                .get("/type/{name}", this::type)
                .get("/search/{term}", this::search)
                .get("/get/{name}", this::pokemon)
                .get("/explicit-mapper/{name}", this::pokemonWithExplicitMapper)
                .get("/search/{type}/{name}", this::pokemonByTypeAndName)
                .get("/count", this::count)
                .post("/", Handler.create(PokemonDto.class, this::insert))
                .delete("/{id}", this::delete);
    }

    /**
     * Resolves the requested type and inserts the Pokemon using separate JDBC operations.
     *
     * @param pokemonDto requested Pokemon
     * @return inserted Pokemon with its generated identifier
     */
    PokemonDto insertPokemon(PokemonDto pokemonDto) {
        Type type = getByName(pokemonDto.type());
        int id = insert(pokemonDto.name(), type.id());
        return PokemonDto.create(new Pokemon(id, pokemonDto.name(), type));
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
     * Retrieves a Pokemon by name using the alternate mapper.
     *
     * @param name Pokemon name
     * @return alternately mapped Pokemon, if present
     */
    Optional<Pokemon> findByNameWithAlternateMapper(String name) {
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
        return statement.map(pokemonAlternateRowMapper).optional();
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
     * Inserts a Pokemon and retrieves the identifier generated by the database.
     *
     * @param name Pokemon name
     * @param typeId type identifier
     * @return generated Pokemon identifier
     */
    int insert(String name, int typeId) {
        String sql = "INSERT INTO POKEMON (NAME, TYPE_ID) VALUES (?, ?)";
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, name);
        statement.bind(2, typeId);
        return statement.generatedKeys()
                .addColumn("ID")
                .map(row -> row.required(1, Integer.class))
                .one();
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
     * Retrieves a Pokemon type by name.
     *
     * @param name type name
     * @return matching type
     */
    Type getByName(String name) {
        String sql = "SELECT ID AS id, NAME AS name FROM TYPE WHERE NAME = ?";
        JdbcClient.Statement statement = jdbcClient.create(sql);
        statement.bind(1, name);
        return statement.map(TYPE_MAPPER).one();
    }

    /**
     * Returns every Pokemon ordered by name.
     */
    private void all(ServerRequest request, ServerResponse response) {
        response.send(listOrderByName()
                              .stream()
                              .map(PokemonDto::create)
                              .toList());
    }

    /**
     * Returns Pokemon having the type supplied in the request path.
     */
    private void type(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(listByTypeName(name)
                              .stream()
                              .map(PokemonDto::create)
                              .toList());
    }

    /**
     * Returns Pokemon whose name or type matches the supplied search term.
     */
    private void search(ServerRequest request, ServerResponse response) {
        String term = request.path().pathParameters().get("term");
        response.send(listByNameOrType(term)
                              .stream()
                              .map(PokemonDto::create)
                              .toList());
    }

    /**
     * Returns the Pokemon having the name supplied in the request path.
     */
    private void pokemon(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(findByName(name).map(PokemonDto::create));
    }

    /**
     * Returns a Pokemon mapped with the recognizable alternate row mapper.
     */
    private void pokemonWithExplicitMapper(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(findByNameWithAlternateMapper(name).map(PokemonDto::create));
    }

    /**
     * Returns the Pokemon matching both path parameters.
     */
    private void pokemonByTypeAndName(ServerRequest request, ServerResponse response) {
        String type = request.path().pathParameters().get("type");
        String name = request.path().pathParameters().get("name");
        response.send(findByTypeAndName(type, name).map(PokemonDto::create));
    }

    /**
     * Returns the number of stored Pokemon.
     */
    private void count(ServerRequest request, ServerResponse response) {
        response.send(count());
    }

    /**
     * Reads a Pokemon from JSON and returns the inserted representation.
     */
    private void insert(PokemonDto pokemonDto, ServerResponse response) {
        if (pokemonDto.name() == null || pokemonDto.name().isBlank()) {
            throw new BadRequestException("Pokemon name must not be null or blank");
        }
        if (pokemonDto.type() == null || pokemonDto.type().isBlank()) {
            throw new BadRequestException("Pokemon type must not be null or blank");
        }
        response.send(insertPokemon(pokemonDto));
    }

    /**
     * Deletes the Pokemon identified by the request path.
     */
    private void delete(ServerRequest request, ServerResponse response) {
        int id = request.path()
                .pathParameters()
                .first("id")
                .asInt()
                .orElseThrow(() -> new BadRequestException("No Pokemon id"));
        response.send("Deleted: " + deleteById(id) + " values");
    }

}

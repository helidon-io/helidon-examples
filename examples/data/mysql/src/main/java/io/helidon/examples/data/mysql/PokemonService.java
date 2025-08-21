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

import io.helidon.http.BadRequestException;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

class PokemonService implements HttpService {

    // Helidon Data repository interface providing Pokemon entity operations.
    private final PokemonRepository pokemonRepository = Services.get(PokemonRepository.class);
    // Helidon Data repository interface providing Pokemon entity operations.
    private final TypeRepository typeRepository = Services.get(TypeRepository.class);

    // Pokemon endpoint routing rules
    @Override
    public void routing(HttpRules rules) {
        rules.get("/all", this::all)
                .get("/type/{name}", this::type)
                .get("/get/{name}", this::pokemon)
                .post("/", Handler.create(PokemonDto.class, this::insert))
                .delete("/{id}", this::delete);
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link Pokemon} entities.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void all(ServerRequest request, ServerResponse response) {
        response.send(pokemonRepository.listOrderByName());
    }

    /**
     * Handles the {@code GET /type/{name}} request and returns a list of {@link Pokemon} entities
     * associated with a specific breed name.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void type(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(pokemonRepository.listByType_Name(name));
    }

    /**
     * Handles the {@code GET /get/{name}} request and returns a {@link Pokemon} entity by its name.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void pokemon(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(pokemonRepository.findByName(name));
    }

    /**
     * Handles the {@code POST /} request and inserts a new {@link Pokemon} entity.
     * <p>
     * Pokémon entity content is supplied as JSON object and translated to {@link PokemonDto} class by HTTP
     * request processing layer.
     * Pokémon type record must already exist. Pokémon entity must not exist in the database.
     *
     * @param pokemonDto the pokémon to insert into the database
     * @param response   the server response
     */
    private void insert(PokemonDto pokemonDto, ServerResponse response) {
        // Pokemon data modification task is executed as single database transaction
        response.send(Tx.transaction(() -> {
            // Retrieve Type entity for provided name
            Type type = typeRepository.getByName(pokemonDto.type());
            // Prepare and insert new Pokemon entity into the database
            Pokemon pokemonEntity = new Pokemon(pokemonDto.name(), type);
            return pokemonRepository.insert(pokemonEntity);
        }));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes a {@link Pokemon} entity by its ID.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void delete(ServerRequest request, ServerResponse response) {
        int id = request.path()
                .pathParameters()
                .first("id").map(Integer::parseInt)
                .orElseThrow(() -> new BadRequestException("No pokémon id"));
        // Rely on internal transaction handling for this simple data modification task
        response.send(new DmlResult(pokemonRepository.deleteById(id), "DELETE"));
    }

}

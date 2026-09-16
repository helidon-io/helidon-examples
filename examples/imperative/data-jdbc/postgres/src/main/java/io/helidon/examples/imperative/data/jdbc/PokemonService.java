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

import io.helidon.common.Api;
import io.helidon.http.BadRequestException;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Exposes Pokemon operations using imperative HTTP routing.
 */
@Service.Singleton
@SuppressWarnings({Api.SUPPRESS_PREVIEW, Api.SUPPRESS_INCUBATING})
final class PokemonService implements HttpService {
    private final PokemonStore pokemonStore;

    /**
     * Creates the HTTP service with the Pokemon store.
     *
     * @param pokemonStore Pokemon store
     */
    @Service.Inject
    PokemonService(PokemonStore pokemonStore) {
        this.pokemonStore = pokemonStore;
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
                .get("/search/{type}/{name}", this::pokemonByTypeAndName)
                .get("/count", this::count)
                .post("/", Handler.create(PokemonDto.class, this::insert))
                .put("/{id}", this::update)
                .delete("/{id}", this::delete);
    }

    private static int pokemonId(ServerRequest request) {
        return request.path()
                .pathParameters()
                .first("id")
                .asInt()
                .orElseThrow(() -> new BadRequestException("No Pokemon id"));
    }

    private static void validate(PokemonDto pokemonDto) {
        if (pokemonDto.name() == null || pokemonDto.name().isBlank()) {
            throw new BadRequestException("Pokemon name must not be null or blank");
        }
        if (pokemonDto.type() == null || pokemonDto.type().isBlank()) {
            throw new BadRequestException("Pokemon type must not be null or blank");
        }
    }

    /**
     * Returns every Pokemon ordered by name.
     */
    private void all(ServerRequest request, ServerResponse response) {
        response.send(pokemonStore.listOrderByName()
                              .stream()
                              .map(PokemonDto::create)
                              .toList());
    }

    /**
     * Returns Pokemon having the type supplied in the request path.
     */
    private void type(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(pokemonStore.listByTypeName(name)
                              .stream()
                              .map(PokemonDto::create)
                              .toList());
    }

    /**
     * Returns Pokemon whose name or type matches the supplied search term.
     */
    private void search(ServerRequest request, ServerResponse response) {
        String term = request.path().pathParameters().get("term");
        response.send(pokemonStore.listByNameOrType(term)
                              .stream()
                              .map(PokemonDto::create)
                              .toList());
    }

    /**
     * Returns the Pokemon having the name supplied in the request path.
     */
    private void pokemon(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(pokemonStore.findByName(name).map(PokemonDto::create));
    }

    /**
     * Returns the Pokemon matching both path parameters.
     */
    private void pokemonByTypeAndName(ServerRequest request, ServerResponse response) {
        String type = request.path().pathParameters().get("type");
        String name = request.path().pathParameters().get("name");
        response.send(pokemonStore.findByTypeAndName(type, name).map(PokemonDto::create));
    }

    /**
     * Returns the number of stored Pokemon.
     */
    private void count(ServerRequest request, ServerResponse response) {
        response.send(pokemonStore.count());
    }

    /**
     * Reads a Pokemon from JSON and returns the inserted representation.
     */
    private void insert(PokemonDto pokemonDto, ServerResponse response) {
        validate(pokemonDto);
        response.send(PokemonDto.create(pokemonStore.insert(pokemonDto.name(), pokemonDto.type())));
    }

    /**
     * Updates the Pokemon identified by the request path.
     */
    private void update(ServerRequest request, ServerResponse response) {
        int id = pokemonId(request);
        PokemonDto pokemonDto = request.content().as(PokemonDto.class);
        validate(pokemonDto);
        response.send(pokemonStore.update(id, pokemonDto.name(), pokemonDto.type()).map(PokemonDto::create));
    }

    /**
     * Deletes the Pokemon identified by the request path.
     */
    private void delete(ServerRequest request, ServerResponse response) {
        int id = pokemonId(request);
        response.send("Deleted: " + pokemonStore.deleteById(id) + " values");
    }

}

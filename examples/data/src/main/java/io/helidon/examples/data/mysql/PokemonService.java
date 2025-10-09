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

import java.util.stream.Collector;

import io.helidon.http.BadRequestException;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

class PokemonService implements HttpService {

    private final PokemonRepository pokemonRepository = Services.get(PokemonRepository.class);
    private final TypeRepository typeRepository = Services.get(TypeRepository.class);

    @Override
    public void routing(HttpRules rules) {
        rules.get("/all", this::all)
                .get("/type/{name}", this::type)
                .get("/get/{name}", this::pokemon)
                .post("/", Handler.create(JsonObject.class, this::insert))
                .delete("/{id}", this::delete);
    }

    private void all(ServerRequest request, ServerResponse response) {
        response.send(pokemonRepository.streamOrderByName()
                              .map(pokemon -> Json.createObjectBuilder()
                                      .add("id", pokemon.getId())
                                      .add("name", pokemon.getName())
                                      .add("type", pokemon.getType().getName())
                                      .build())
                              .collect(Collector.of(Json::createArrayBuilder,
                                                    JsonArrayBuilder::add,
                                                    JsonArrayBuilder::add,
                                                    JsonArrayBuilder::build)));
    }

    private void type(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(pokemonRepository.streamByType_Name(name)
                              .map(pokemon -> Json.createObjectBuilder()
                                      .add("id", pokemon.getId())
                                      .add("name", pokemon.getName())
                                      .add("type", pokemon.getType().getName())
                                      .build())
                              .collect(Collector.of(Json::createArrayBuilder,
                                                    JsonArrayBuilder::add,
                                                    JsonArrayBuilder::add,
                                                    JsonArrayBuilder::build)));
    }

    private void pokemon(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        pokemonRepository.findByName(name)
                .ifPresentOrElse(
                        pokemon -> response.send(Json.createObjectBuilder()
                                                         .add("id", pokemon.getId())
                                                         .add("name", pokemon.getName())
                                                         .add("type", pokemon.getType().getName())
                                                         .build()),
                        () -> response.send(JsonObject.EMPTY_JSON_OBJECT));
    }

    private void insert(JsonObject pokemonJson, ServerResponse response) {
        Pokemon pokemon = Tx.transaction(() -> {
            Type type = typeRepository.getByName(pokemonJson.getString("type"));
            Pokemon pokemonEntity = new Pokemon(pokemonJson.getString("name"), type);
            return pokemonRepository.insert(pokemonEntity);
        });
        response.send(Json.createObjectBuilder()
                              .add("id", pokemon.getId())
                              .add("name", pokemon.getName())
                              .add("type", pokemon.getType().getName())
                              .build());
    }

    private void delete(ServerRequest request, ServerResponse response) {
        int id = request.path()
                .pathParameters()
                .first("id").map(Integer::parseInt)
                .orElseThrow(() -> new BadRequestException("No pokémon id"));
        response.send("Deleted: " + pokemonRepository.deleteById(id) + " values");
    }

}

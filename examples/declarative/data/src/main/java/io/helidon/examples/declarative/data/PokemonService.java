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
package io.helidon.examples.declarative.data;

import java.util.stream.Collector;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@SuppressWarnings("deprecation")
@Http.Path("/pokemon")
@Service.Singleton
@RestServer.Endpoint
class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final TypeRepository typeRepository;

    @Service.Inject
    PokemonService(PokemonRepository pokemonRepository,
                   TypeRepository typeRepository) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
    }

    @Http.GET
    @Http.Path("/all")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    JsonArray all() {
        return pokemonRepository.streamOrderByName()
                .map(pokemon -> Json.createObjectBuilder()
                        .add("id", pokemon.getId())
                        .add("name", pokemon.getName())
                        .add("type", pokemon.getType().getName())
                        .build())
                .collect(Collector.of(Json::createArrayBuilder,
                                      JsonArrayBuilder::add,
                                      JsonArrayBuilder::add,
                                      JsonArrayBuilder::build));
    }

    @Http.GET
    @Http.Path("/type/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    JsonArray type(@Http.PathParam("name") String name) {
        return pokemonRepository.streamByType_Name(name)
                .map(pokemon -> Json.createObjectBuilder()
                        .add("id", pokemon.getId())
                        .add("name", pokemon.getName())
                        .add("type", pokemon.getType().getName())
                        .build())
                .collect(Collector.of(Json::createArrayBuilder,
                                      JsonArrayBuilder::add,
                                      JsonArrayBuilder::add,
                                      JsonArrayBuilder::build));
    }

    @Http.GET
    @Http.Path("/get/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    JsonObject pokemon(@Http.PathParam("name") String name) {
        return pokemonRepository.findByName(name)
                .map(pokemon -> Json.createObjectBuilder()
                        .add("id", pokemon.getId())
                        .add("name", pokemon.getName())
                        .add("type", pokemon.getType().getName())
                        .build())
                .orElse(JsonObject.EMPTY_JSON_OBJECT);
    }

    @Http.POST
    @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    JsonObject insert(@Http.Entity JsonObject pokemonJson) {
        Pokemon pokemon = insertPokemon(pokemonJson);
        return Json.createObjectBuilder()
                .add("id", pokemon.getId())
                .add("name", pokemon.getName())
                .add("type", pokemon.getType().getName())
                .build();
    }

    @Tx.Required
    Pokemon insertPokemon(JsonObject pokemonJson) {
        Type type = typeRepository.getByName(pokemonJson.getString("type"));
        return pokemonRepository.insert(new Pokemon(pokemonJson.getString("name"), type));
    }

    @Http.DELETE
    @Http.Path("/{id}")
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    String delete(@Http.PathParam("id") int id) {
        return "Deleted: " + pokemonRepository.deleteById(id) + " values";
    }

}

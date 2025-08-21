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

import java.util.List;
import java.util.Optional;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;

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
    List<Pokemon> all() {
        return pokemonRepository.listOrderByName();
    }

    @Http.GET
    @Http.Path("/type/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<Pokemon> type(@Http.PathParam("name") String name) {
        return pokemonRepository.listByType_Name(name);
    }

    @Http.GET
    @Http.Path("/get/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    Optional<Pokemon> pokemon(@Http.PathParam("name") String name) {
        return pokemonRepository.findByName(name);
    }

    @Http.POST
    @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    Pokemon insert(@Http.Entity PokemonDto pokemonDto) {
        return insertPokemon(pokemonDto);
    }

    @Tx.Required
    Pokemon insertPokemon(PokemonDto pokemonDto) {
        Type type = typeRepository.getByName(pokemonDto.type());
        return pokemonRepository.insert(new Pokemon(pokemonDto.name(), type));
    }

    @Http.DELETE
    @Http.Path("/{id}")
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    String delete(@Http.PathParam("id") int id) {
        return "Deleted: " + pokemonRepository.deleteById(id) + " values";
    }

}

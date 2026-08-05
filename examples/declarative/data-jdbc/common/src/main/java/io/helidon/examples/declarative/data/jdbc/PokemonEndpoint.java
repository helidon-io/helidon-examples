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

import java.util.List;
import java.util.Optional;

import io.helidon.common.Api;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.examples.declarative.data.jdbc.model.Pokemon;
import io.helidon.examples.declarative.data.jdbc.model.PokemonRepository;
import io.helidon.examples.declarative.data.jdbc.model.Type;
import io.helidon.examples.declarative.data.jdbc.model.TypeRepository;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;

/**
 * Exposes Pokemon operations backed by JDBC repositories.
 */
@SuppressWarnings(Api.SUPPRESS_INCUBATING) // Helidon Declarative is an incubating feature.
@Http.Path("/pokemon")
@Service.Singleton
@RestServer.Endpoint
class PokemonEndpoint {

    private final PokemonRepository pokemonRepository;
    private final TypeRepository typeRepository;

    /**
     * Creates the endpoint with its JDBC repositories.
     *
     * @param pokemonRepository provides Pokemon data
     * @param typeRepository provides Pokemon type data
     */
    @Service.Inject
    PokemonEndpoint(PokemonRepository pokemonRepository,
                    TypeRepository typeRepository) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
    }

    /**
     * Lists all Pokemon in name order.
     *
     * @return all Pokemon
     */
    @Http.GET
    @Http.Path("/all")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<PokemonDto> all() {
        return pokemonRepository.listOrderByName()
                .stream()
                .map(PokemonDto::create)
                .toList();
    }

    /**
     * Lists Pokemon having the requested type.
     *
     * @param name type name
     * @return matching Pokemon
     */
    @Http.GET
    @Http.Path("/type/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<PokemonDto> type(@Http.PathParam("name") String name) {
        return pokemonRepository.listByTypeName(name)
                .stream()
                .map(PokemonDto::create)
                .toList();
    }

    /**
     * Lists Pokemon whose name or type matches one term.
     *
     * @param term Pokemon or type name
     * @return matching Pokemon
     */
    @Http.GET
    @Http.Path("/search/{term}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<PokemonDto> search(@Http.PathParam("term") String term) {
        return pokemonRepository.listByNameOrType(term)
                .stream()
                .map(PokemonDto::create)
                .toList();
    }

    /**
     * Looks up a Pokemon by name.
     *
     * @param name Pokemon name
     * @return matching Pokemon, if present
     */
    @Http.GET
    @Http.Path("/get/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    Optional<PokemonDto> pokemon(@Http.PathParam("name") String name) {
        return pokemonRepository.findByName(name)
                .map(PokemonDto::create);
    }

    /**
     * Looks up a Pokemon with the alternate row mapper selected explicitly.
     *
     * @param name Pokemon name
     * @return Pokemon mapped by the alternate mapper, if present
     */
    @Http.GET
    @Http.Path("/explicit-mapper/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    Optional<PokemonDto> pokemonWithExplicitMapper(@Http.PathParam("name") String name) {
        return pokemonRepository.findByNameWithAlternateMapper(name)
                .map(PokemonDto::create);
    }

    /**
     * Looks up a Pokemon by type and name using positional SQL parameters.
     *
     * @param type type name
     * @param name Pokemon name
     * @return matching Pokemon, if present
     */
    @Http.GET
    @Http.Path("/search/{type}/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    Optional<PokemonDto> pokemonByTypeAndName(@Http.PathParam("type") String type,
                                              @Http.PathParam("name") String name) {
        return pokemonRepository.findByTypeAndName(type, name)
                .map(PokemonDto::create);
    }

    /**
     * Counts all Pokemon.
     *
     * @return number of Pokemon rows
     */
    @Http.GET
    @Http.Path("/count")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    long count() {
        return pokemonRepository.count();
    }

    /**
     * Adds a Pokemon and returns its generated identifier.
     *
     * @param pokemonDto requested Pokemon
     * @return inserted Pokemon
     */
    @Http.POST
    @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    PokemonDto insert(@Http.Entity PokemonDto pokemonDto) {
        return insertPokemon(pokemonDto);
    }

    /**
     * Resolves the type and inserts the Pokemon in one local JDBC transaction.
     *
     * @param pokemonDto requested Pokemon
     * @return inserted Pokemon
     */
    @Tx.Required
    PokemonDto insertPokemon(PokemonDto pokemonDto) {
        Type type = typeRepository.getByName(pokemonDto.type());
        int id = pokemonRepository.insert(pokemonDto.name(), type.id());
        return PokemonDto.create(new Pokemon(id, pokemonDto.name(), type));
    }

    /**
     * Deletes a Pokemon by identifier.
     *
     * @param id Pokemon identifier
     * @return text containing the number of deleted rows
     */
    @Http.DELETE
    @Http.Path("/{id}")
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    String delete(@Http.PathParam("id") int id) {
        return "Deleted: " + pokemonRepository.deleteById(id) + " values";
    }
}

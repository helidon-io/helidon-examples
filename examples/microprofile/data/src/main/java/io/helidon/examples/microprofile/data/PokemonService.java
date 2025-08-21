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
package io.helidon.examples.microprofile.data;

import java.util.List;
import java.util.Optional;

import io.helidon.common.media.type.MediaTypes;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

/**
 * Service class responsible for handling HTTP requests related to {@link Pokemon} entities.
 * <p>
 * This class provides endpoint handlers for operations on {@link Pokemon} entities.
 *
 * @see PokemonRepository
 * @see TypeRepository
 */
@Path("/pokemon")
public class PokemonService {

    private final PokemonRepository pokemonRepository;
    private final TypeRepository typeRepository;

    /**
     * Constructs a new {@link PokemonService} instance.
     *
     * @param pokemonRepository {@link Pokemon} entity data repository
     * @param typeRepository {@link Type} entity data repository
     */
    @Inject
    public PokemonService(PokemonRepository pokemonRepository,
                          TypeRepository typeRepository) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link Pokemon} entities.
     *
     * @return the list of all {@link Pokemon} entities
     */
    @GET
    @Path("/all")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<Pokemon> all() {
        return pokemonRepository.listOrderByName();
    }

    /**
     * Handles the {@code GET /type/{name}} request and returns a list of {@link Pokemon} entities
     * associated with a specific type name.
     *
     * @param name the name of the pokémon's type
     * @return the list of {@link Pokemon} entities associated with a specific type name
     */
    @GET
    @Path("/type/{name}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<Pokemon> type(@PathParam("name") String name) {
        return pokemonRepository.listByType_Name(name);
    }

    /**
     * Handles the {@code GET /get/{name}} request and returns a {@link Pokemon} entity by its name.
     *
     * @param name the name of the pokémon
     * @return a {@link Pokemon} entity with matching name or {@link Optional#empty()} when no such entity exists
     */
    @GET
    @Path("/get/{name}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public Optional<Pokemon> pokemon(@PathParam("name") String name) {
        return pokemonRepository.findByName(name);
    }

    /**
     * Handles the {@code POST /} request and inserts a new {@link Pokemon} entity.
     * <p>
     * Pet entity content is supplied as JSON object.
     * Pokémon type record must already exist. Pokémon entity must not exist in the database.
     *
     * @param pokemonDto the pokémon to insert into the database
     * @return the new {@link Pokemon} entity
     */
    @POST
    @Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public Pokemon insert(PokemonDto pokemonDto) {
        return insertPokemon(pokemonDto);
    }

    @Transactional
    Pokemon insertPokemon(PokemonDto pokemonDto) {
        Type type = typeRepository.getByName(pokemonDto.type());
        return pokemonRepository.insert(new Pokemon(pokemonDto.name(), type));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes a {@link Pokemon} entity by its ID.
     *
     * @param id the id of the pokémon
     * @return the {@link Pokemon} entity delete operation result
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaTypes.TEXT_PLAIN_VALUE)
    public String delete(@PathParam("id") int id) {
        return "Deleted: " + pokemonRepository.deleteById(id) + " values";
    }

}

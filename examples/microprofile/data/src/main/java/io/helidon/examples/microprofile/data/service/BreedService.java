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
package io.helidon.examples.microprofile.data.service;

import java.util.List;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.examples.microprofile.data.model.Breed;
import io.helidon.examples.microprofile.data.repository.BreedRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

/**
 * Service class responsible for handling HTTP requests related to {@link Breed} entities.
 * <p>
 * This class provides endpoint handlers for operations on {@link Breed} entities.
 *
 * @see BreedRepository
 */
@Path("/breed")
public class BreedService {

    // Helidon Data repository interface providing Breed entity operations.
    private final BreedRepository breedRepository;

    /**
     * Constructs a new {@link BreedService} instance.
     *
     * @param breedRepository {@link Breed} entity data repository
     */
    @Inject
    public BreedService(BreedRepository breedRepository) {
        this.breedRepository = breedRepository;
    }


    /**
     * Handles the {@code GET /} request and returns the index page with endpoint information.
     *
     * @return the index page with endpoint information
     */
    @GET
    @Produces(MediaTypes.TEXT_PLAIN_VALUE)
    public String index() {
        return """
                     Breed entity endpoint:
                          GET /breed/all     - List all owners
                          POST /breed/{name} - Insert new breed with provided name
                          DELETE /breed/{id} - Delete breed with provided ID
               """;
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link Breed} entities.
     *
     * @return the list of all {@link Breed} entities
     */
    @GET
    @Path("/all")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<Breed> all() {
        return breedRepository.listOrderByName();
    }

    /**
     * Handles the {@code POST /{name}} request and inserts a new {@link Breed} entity with
     * the provided name.
     *
     * @param name the name of the breed
     * @return the new {@link Breed} entity
     */
    @POST
    @Path("/{name}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Transactional
    public Breed insert(@PathParam("name") String name) {
        return breedRepository.insert(new Breed(name));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes a {@link Breed} entity by its ID.
     *
     * @param id the id of the breed
     * @return the {@link Breed} entity delete operation result
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public DmlResult delete(@PathParam("id") int id) {
        return new DmlResult(breedRepository.deleteById(id), "DELETE");
    }

}

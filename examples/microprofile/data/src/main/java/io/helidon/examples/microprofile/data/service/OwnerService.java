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
import io.helidon.examples.microprofile.data.model.Owner;
import io.helidon.examples.microprofile.data.repository.OwnerRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

/**
 * Service class responsible for handling HTTP requests related to {@link Owner} entities.
 * <p>
 * This class provides endpoint handlers for operations on {@link Owner} entities.
 *
 * @see OwnerRepository
 */
@Path("/owner")
public class OwnerService {

    // Helidon Data repository interface providing Owner entity operations.
    private final OwnerRepository ownerRepository;

    /**
     * Constructs a new {@link OwnerService} instance.
     *
     * @param ownerRepository {@link Owner} entity data repository
     */
    @Inject
    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
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
                     Owner entity endpoint:
                          GET /owner/all           - List all owners
                          GET /owner/names/{breed} - List all owners names with pet of specific breed
                          POST /owner/{name}       - Insert new owner with provided name
                          DELETE /owner/{id}       - Delete owner with provided ID
               """;
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link Owner} entities.
     *
     * @return the list of all {@link Owner} entities
     */
    @GET
    @Path("/all")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<Owner> all() {
        return ownerRepository.ownersOrderedByName();
    }

    /**
     * Handles the {@code GET /names/{breed}} request and returns a list of owner names whose pets
     * have a breed with the specified name.
     *
     * @param breed the name of the pet's breed
     * @return the list of owner names
     */
    @GET
    @Path("/names/{breed}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<String> namesByBreed(@PathParam("breed") String breed) {
        return ownerRepository.queryNameByPetBreed(breed);
    }

    /**
     * Handles the {@code POST /{name}} request and inserts a new {@link Owner} entity with
     * the provided name.
     *
     * @param name the name of the owner
     * @return the new {@link Owner} entity
     */
    @POST
    @Path("/{name}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Transactional
    public Owner insert(@PathParam("name") String name) {
        return ownerRepository.insert(new Owner(name));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes the {@link Owner} entity with
     * the specified ID.
     *
     * @param id the id of the owner
     * @return the {@link Owner} entity delete operation result
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public DmlResult delete(@PathParam("id") int id) {
        return new DmlResult(ownerRepository.deleteById(id), "DELETE");
    }

}

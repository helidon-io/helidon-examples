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
import java.util.Optional;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.data.PageRequest;
import io.helidon.examples.microprofile.data.model.Breed;
import io.helidon.examples.microprofile.data.model.Owner;
import io.helidon.examples.microprofile.data.model.Pet;
import io.helidon.examples.microprofile.data.repository.BreedRepository;
import io.helidon.examples.microprofile.data.repository.OwnerRepository;
import io.helidon.examples.microprofile.data.repository.PetRepository;

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
 * Service class responsible for handling HTTP requests related to {@link Pet} entities.
 * <p>
 * This class provides endpoint handlers for operations on {@link Pet} entities.
 *
 * @see PetRepository
 * @see BreedRepository
 * @see OwnerRepository
 */
@Path("/pet")
public class PetService {

    // Helidon Data repository interface providing Pet entity operations.
    private final PetRepository petRepository;
    // Helidon Data repository interface providing Breed entity operations.
    private final BreedRepository breedRepository;
    // Helidon Data repository interface providing Owner entity operations.
    private final OwnerRepository ownerRepository;

    /**
     * Constructs a new {@link PetService} instance.
     *
     * @param petRepository {@link Pet} entity data repository
     * @param breedRepository {@link Breed} entity data repository
     * @param ownerRepository {@link Owner} entity data repository
     */
    @Inject
    public PetService(PetRepository petRepository,
               BreedRepository breedRepository,
               OwnerRepository ownerRepository) {
        this.petRepository = petRepository;
        this.breedRepository = breedRepository;
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
                     Pet entity endpoint:
                          GET /pet/all            - List all pets
                          GET /pet/all/{page}     - List all pets as pages of size 5
                          GET /pet/breed/{name}   - List all pets of specific breed
                          GET /pet/get/{name}     - Retrieve pet with specific name
                          POST /pet               - Insert new pet: { "name":<name>,
                                                                      "weight":<weight>,
                                                                      "birth":<birth_date>,
                                                                      "owner":<owner_name>,
                                                                      "breed":<breed_name> }
                          DELETE /pet/{id}        - Delete pet with specific ID
                """;
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link Pet} entities.
     *
     * @return the list of all {@link Pet} entities
     */
    @GET
    @Path("/all")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<Pet> all() {
        return petRepository.listOrderByName();
    }

    /**
     * Handles the {@code GET /all/{page}} request and returns a paginated list of all {@link Pet}
     * entities.
     * <p>
     * The page size is fixed at 5. The page index starts from 0.
     *
     * @param page the page number, starting from {@code 0}
     * @return the paginated list of all {@link Pet} entities
     */
    @GET
    @Path("/all/{page}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public PageDto<Pet> allPage(@PathParam("page") int page) {
        return PageDto.create(
                        petRepository.listOrderByName(PageRequest.create(page, 5)));
    }

    /**
     * Handles the {@code GET /breed/{name}} request and returns a list of {@link Pet} entities
     * associated with a specific breed name.
     *
     * @param name the name of the pet's breed
     * @return the list of {@link Pet} entities associated with a specific breed name
     */
    @GET
    @Path("/breed/{name}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public List<Pet> breed(@PathParam("name") String name) {
        return petRepository.listByBreed_Name(name);
    }

    /**
     * Handles the {@code GET /get/{name}} request and returns a {@link Pet} entity by its name.
     *
     * @param name the name of the pet
     * @return a {@link Pet} entity with matching name or {@link Optional#empty()} when no such entity exists
     */
    @GET
    @Path("/get/{name}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public Optional<Pet> pet(@PathParam("name") String name) {
        return petRepository.findByName(name);
    }

    /**
     * Handles the {@code POST /} request and inserts a new {@link Pet} entity.
     * <p>
     * Pet entity content is supplied as JSON object and translated to {@link PetDto} class by HTTP
     * request processing layer.
     * Pet breed and owner records must already exist. Pet entity must not exist in the database.
     *
     * @param petDto the pet to insert into the database
     * @return the new {@link Pet} entity
     */
    @POST
    @Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Transactional
    public Pet insert(PetDto petDto) {
        // Pet data modification task is executed as single database transaction
        // Retrieve Breed and Owner entities for provided names
        Breed breed = breedRepository.getByName(petDto.breed());
        Owner owner = ownerRepository.ownerByName(petDto.owner());
        // Prepare and insert new Pet entity into the database
        Pet petEntity = new Pet(petDto.name(),
                                petDto.weight(),
                                petDto.birth(),
                                owner,
                                breed);
        return petRepository.insert(petEntity);
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes a {@link Pet} entity by its ID.
     *
     * @param id the id of the pet
     * @return the {@link Pet} entity delete operation result
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaTypes.APPLICATION_JSON_VALUE)
    public DmlResult delete(@PathParam("id") int id) {
        return new DmlResult(petRepository.deleteById(id), "DELETE");
    }

}

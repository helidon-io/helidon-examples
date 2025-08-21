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
package io.helidon.examples.declarative.data.service;

import java.util.List;
import java.util.Optional;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.data.PageRequest;
import io.helidon.examples.declarative.data.model.Breed;
import io.helidon.examples.declarative.data.model.Owner;
import io.helidon.examples.declarative.data.model.Pet;
import io.helidon.examples.declarative.data.repository.BreedRepository;
import io.helidon.examples.declarative.data.repository.OwnerRepository;
import io.helidon.examples.declarative.data.repository.PetRepository;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;

/**
 * Service class responsible for handling HTTP requests related to {@link Pet} entities.
 * <p>
 * This class provides endpoint handlers for operations on {@link Pet} entities.
 *
 * @see PetRepository
 * @see BreedRepository
 * @see OwnerRepository
 */
@SuppressWarnings("deprecation")
@Http.Path("/pet")
@Service.Singleton
@RestServer.Endpoint
class PetService {

    // Helidon Data repository interface providing Pet entity operations.
    private final PetRepository petRepository;
    // Helidon Data repository interface providing Breed entity operations.
    private final BreedRepository breedRepository;
    // Helidon Data repository interface providing Owner entity operations.
    private final OwnerRepository ownerRepository;

    @Service.Inject
    PetService(PetRepository petRepository,
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
    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    String index() {
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
    @Http.GET
    @Http.Path("/all")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<Pet> all() {
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
    @Http.GET
    @Http.Path("/all/{page}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    PageDto<Pet> allPage(@Http.PathParam("page") int page) {
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
    @Http.GET
    @Http.Path("/breed/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<Pet> breed(@Http.PathParam("name") String name) {
        return petRepository.listByBreed_Name(name);
    }

    /**
     * Handles the {@code GET /get/{name}} request and returns a {@link Pet} entity by its name.
     *
     * @param name the name of the pet
     * @return a {@link Pet} entity with matching name or {@link Optional#empty()} when no such entity exists
     */
    @Http.GET
    @Http.Path("/get/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    Optional<Pet> pet(@Http.PathParam("name") String name) {
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
    @Http.POST
    @Http.Consumes(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Tx.Required
    Pet insert(@Http.Entity PetDto petDto) {
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
    @Http.DELETE
    @Http.Path("/{id}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    DmlResult delete(@Http.PathParam("id") int id) {
        return new DmlResult(petRepository.deleteById(id), "DELETE");
    }

}

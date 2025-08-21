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

import io.helidon.common.media.type.MediaTypes;
import io.helidon.examples.declarative.data.model.Breed;
import io.helidon.examples.declarative.data.repository.BreedRepository;
import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.RestServer;

/**
 * Service class responsible for handling HTTP requests related to {@link Breed} entities.
 * <p>
 * This class provides endpoint handlers for operations on {@link Breed} entities.
 *
 * @see BreedRepository
 */
@SuppressWarnings("deprecation")
@Http.Path("/breed")
@Service.Singleton
@RestServer.Endpoint
class BreedService {

    // Helidon Data repository interface providing Breed entity operations.
    private final BreedRepository breedRepository;

    @Service.Inject
    BreedService(BreedRepository breedRepository) {
        this.breedRepository = breedRepository;
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
    @Http.GET
    @Http.Path("/all")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    List<Breed> all() {
        return breedRepository.listOrderByName();
    }

    /**
     * Handles the {@code POST /{name}} request and inserts a new {@link Breed} entity with
     * the provided name.
     *
     * @param name the name of the breed
     * @return the new {@link Breed} entity
     */
    @Http.POST
    @Http.Path("/{name}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Tx.Required
    Breed insert(@Http.PathParam("name") String name) {
        return breedRepository.insert(new Breed(name));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes a {@link Breed} entity by its ID.
     *
     * @param id the id of the breed
     * @return the {@link Breed} entity delete operation result
     */
    @Http.DELETE
    @Http.Path("/{id}")
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    DmlResult delete(@Http.PathParam("id") int id) {
        return new DmlResult(breedRepository.deleteById(id), "DELETE");
    }

}

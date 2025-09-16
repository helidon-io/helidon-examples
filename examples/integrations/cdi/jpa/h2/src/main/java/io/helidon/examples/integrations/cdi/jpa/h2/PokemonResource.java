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
package io.helidon.examples.integrations.cdi.jpa.h2;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A simple JPA based CRUD resource.
 */
@Path("/pokemon")
@SuppressWarnings("checkstyle:MissingJavadocMethod")
public class PokemonResource {

    @PersistenceContext(unitName = "pu1")
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Pokemon> list() {
        return em.createNamedQuery("list", Pokemon.class).getResultList();
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Pokemon get(@PathParam("name") String name) {
        TypedQuery<Pokemon> query = em.createNamedQuery("get", Pokemon.class);
        List<Pokemon> list = query.setParameter("name", name).getResultList();
        if (list.isEmpty()) {
            throw new NotFoundException("Unable to find pokemon: " + name);
        }
        return list.getFirst();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRED)
    public Response create(Pokemon pokemon) {
        try {
            em.persist(pokemon);
            return Response.status(201).build();
        } catch (Exception e) {
            throw new BadRequestException("Unable to create pokemon: " + pokemon.getName());
        }
    }

    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(Transactional.TxType.REQUIRED)
    public Response update(@PathParam("name") String name, Pokemon update) {
        Pokemon pokemon = get(name);
        pokemon.setType(update.getType());
        em.persist(pokemon);
        return Response.status(200).build();
    }

    @DELETE
    @Path("/{name}")
    @Transactional(Transactional.TxType.REQUIRED)
    public void delete(@PathParam("name") String name) {
        Pokemon pokemon = get(name);
        em.remove(pokemon);
    }

    @DELETE
    @Transactional(Transactional.TxType.REQUIRED)
    public void deleteAll() {
        em.createNamedQuery("deleteAll").executeUpdate();
    }
}


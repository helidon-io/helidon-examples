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

import java.util.ArrayList;
import java.util.List;

import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@HelidonTest
class PokemonH2Test {

    static Pokemon pokemon(String name, String type) {
        Pokemon pokemon = new Pokemon();
        pokemon.setName(name);
        pokemon.setType(type);
        return pokemon;
    }

    static final GenericType<List<Pokemon>> POKEMON_LIST = new GenericType<>() {
    };

    @Test
    void testCreateDeleteAll(WebTarget target) {
        // create
        try (var rsp = target.path("/pokemon").request()
                .post(Entity.entity(pokemon("Raticate", "Normal/Ice"), MediaType.APPLICATION_JSON_TYPE))) {

            assertThat(rsp.getStatus(), is(201));
        }

        // verify created
        List<String> names = new ArrayList<>();
        try (var rsp = target.path("/pokemon").request().get()) {

            assertThat(rsp.getStatus(), is(200));
            for (Pokemon pokemon : rsp.readEntity(POKEMON_LIST)) {
                names.add(pokemon.getName());
            }
            assertThat(names, is(List.of("Raticate")));
        }

        // delete
        try (var rsp = target.path("/pokemon").request().delete()) {
            assertThat(rsp.getStatus(), is(204));
        }

        // verify deleted
        names = new ArrayList<>();
        try (var rsp = target.path("/pokemon").request().get()) {

            assertThat(rsp.getStatus(), is(200));
            for (Pokemon pokemon : rsp.readEntity(POKEMON_LIST)) {
                names.add(pokemon.getName());
            }
            assertThat(names, is(empty()));
        }
    }

    @Test
    void testAddUpdate(WebTarget target) {
        // create
        try (var rsp = target.path("/pokemon").request()
                .post(Entity.entity(pokemon("Pikachu", "Electric"), MediaType.APPLICATION_JSON_TYPE))) {

            assertThat(rsp.getStatus(), is(201));
        }

        // verify created
        try (var rsp = target.path("/pokemon/Pikachu").request().get()) {

            assertThat(rsp.getStatus(), is(200));
            Pokemon pokemon = rsp.readEntity(Pokemon.class);
            assertThat(pokemon.getName(), is("Pikachu"));
            assertThat(pokemon.getType(), is("Electric"));
        }

        // update
        try (var rsp =  target.path("/pokemon/Pikachu").request()
                .put(Entity.entity(pokemon("Pikachu", "Normal"), MediaType.APPLICATION_JSON_TYPE))) {

            assertThat(rsp.getStatus(), is(200));
        }

        // verify updated
        try (var rsp = target.path("/pokemon/Pikachu").request().get()) {

            assertThat(rsp.getStatus(), is(200));
            Pokemon pokemon = rsp.readEntity(Pokemon.class);
            assertThat(pokemon.getName(), is("Pikachu"));
            assertThat(pokemon.getType(), is("Normal"));
        }

        // delete
        try (var rsp =  target.path("/pokemon/Pikachu").request().delete()) {
            assertThat(rsp.getStatus(), is(204));
        }

        // verify deleted
        try (var rsp = target.path("/pokemon/Pikachu").request().get()) {
            assertThat(rsp.getStatus(), is(404));
        }
    }
}

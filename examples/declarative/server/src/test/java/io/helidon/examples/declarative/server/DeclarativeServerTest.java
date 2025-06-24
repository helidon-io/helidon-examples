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

package io.helidon.examples.declarative.server;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
public class DeclarativeServerTest {
    private final Http1Client client;

    public DeclarativeServerTest(Http1Client client) {
        this.client = client;
    }

    @Test
    void testRefreshTask() {
        var response = client.get("/api/business/greet")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(RefreshTaskDto.class);

        assertThat(response.status(), is(Status.OK_200));
        RefreshTaskDto entity = response.entity();
        // just make sure we got a value
        assertThat(entity.getCount(), notNullValue());
    }

    @Test
    void testGreet() {
        var response = client.get("/api/business/greet")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("Hello World"));
    }

    @Test
    void testGreetNamed() {
        var response = client.get("/api/business/greet/Test")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("Hello Test"));
    }

    @Test
    void testGreetJson() {
        var response = client.get("/api/business/greet")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(GreetingDto.class);

        assertThat(response.status(), is(Status.OK_200));
        GreetingDto entity = response.entity();
        assertThat(entity.getGreeting(), is("Hello"));
        assertThat(entity.getName(), is("World"));
    }

    @Test
    void testGreetNamedJson() {
        var response = client.get("/api/business/greet/Test")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(GreetingDto.class);

        assertThat(response.status(), is(Status.OK_200));
        GreetingDto entity = response.entity();
        assertThat(entity.getGreeting(), is("Hello"));
        assertThat(entity.getName(), is("Test"));
    }

    @Test
    void testUpdateGreeting() {
        var response = client.post("/api/business/greet")
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola");

        assertThat(response.status(), is(Status.NO_CONTENT_204));
        response.close();

        try {
            var entity = client.get("/api/business/greet")
                    .accept(MediaTypes.TEXT_PLAIN)
                    .requestEntity(String.class);
            assertThat(entity, is("Hola World"));
        } finally {
            client.post("/api/business/greet")
                    .contentType(MediaTypes.TEXT_PLAIN)
                    .submit("Hello")
                    .close();
        }
    }
}

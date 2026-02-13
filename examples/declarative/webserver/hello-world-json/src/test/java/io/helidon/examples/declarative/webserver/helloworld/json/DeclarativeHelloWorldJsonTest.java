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

package io.helidon.examples.declarative.webserver.helloworld.json;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
public class DeclarativeHelloWorldJsonTest {
    private final Http1Client client;

    public DeclarativeHelloWorldJsonTest(Http1Client client) {
        this.client = client;
    }

    @Test
    void testHelloWorld() {
        var response = client.get("/hello")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(GreetingDto.class);

        assertThat(response.status(), is(Status.OK_200));
        GreetingDto entity = response.entity();
        assertThat(entity.greeting(), is("Hello"));
        assertThat(entity.name(), is("World"));
    }

    @Test
    void testHelloNamed() {
        var response = client.get("/hello/Test")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(GreetingDto.class);

        assertThat(response.status(), is(Status.OK_200));
        GreetingDto entity = response.entity();
        assertThat(entity.greeting(), is("Hello"));
        assertThat(entity.name(), is("Test"));
    }

    @Test
    void testUpdateGreeting() {
        var response = client.post("/hello")
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola");

        assertThat(response.status(), is(Status.NO_CONTENT_204));
        response.close();

        try {
            var entity = client.get("/hello")
                    .accept(MediaTypes.APPLICATION_JSON)
                    .requestEntity(GreetingDto.class);
            assertThat(entity.greeting(), is("Hola"));
            assertThat(entity.name(), is("World"));
        } finally {
            client.post("/hello")
                    .contentType(MediaTypes.TEXT_PLAIN)
                    .submit("Hello")
                    .close();
        }
    }
}

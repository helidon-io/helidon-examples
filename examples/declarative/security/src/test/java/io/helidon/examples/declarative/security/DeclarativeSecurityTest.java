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

package io.helidon.examples.declarative.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
class DeclarativeSecurityTest {
    private final Http1Client client;

    public DeclarativeSecurityTest(Http1Client client) {
        this.client = client;
    }

    @Test
    void testHelloWorld() {
        var response = client.get("/hello")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("Hello World"));
    }

    @Test
    void testHelloNamed() {
        var response = client.get("/hello/Test")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("Hello Test"));
    }

    @Test
    void testUpdateGreetingOk() {
        /*
        We have two users - john (admin) & jack (user), password is "changeit" for both
        The method is annotated as @RoleValidator.Roles("admin") - i.e. only john should be able to update a greeting
         */
        var response = client.post("/hello")
                .header(HeaderNames.AUTHORIZATION, "basic " + basicAuth("john"))
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola", String.class);

        assertThat(response.status(), is(Status.NO_CONTENT_204));

        try {
            var entity = client.get("/hello")
                    .accept(MediaTypes.TEXT_PLAIN)
                    .requestEntity(String.class);
            assertThat(entity, is("Hola World"));
        } finally {
            // make sure we reset to original greeting even if the above assertion fail
            client.post("/hello")
                    .contentType(MediaTypes.TEXT_PLAIN)
                    .header(HeaderNames.AUTHORIZATION, "basic " + basicAuth("john"))
                    .submit("Hello")
                    .close();
        }
    }

    @Test
    void testUpdateGreetingBadRole() {
        /*
        We have two users - john (admin) & jack (user), password is "changeit" for both
        The method is annotated as @RoleValidator.Roles("admin") - i.e. only john should be able to update a greeting
         */
        var response = client.post("/hello")
                .header(HeaderNames.AUTHORIZATION, "basic " + basicAuth("jack"))
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola", String.class);

        assertThat(response.status(), is(Status.FORBIDDEN_403));
    }

    @Test
    void testUpdateGreetingNoUser() {
        /*
        We have two users - john (admin) & jack (user), password is "changeit" for both
        The method is annotated as @RoleValidator.Roles("admin") - i.e. only john should be able to update a greeting
         */
        var response = client.post("/hello")
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola", String.class);

        assertThat(response.status(), is(Status.UNAUTHORIZED_401));
    }

    private String basicAuth(String name) {
        String text = name + ":changeit";
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }
}

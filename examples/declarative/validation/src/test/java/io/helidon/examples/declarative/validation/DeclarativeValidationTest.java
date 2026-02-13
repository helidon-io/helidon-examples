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

package io.helidon.examples.declarative.validation;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
public class DeclarativeValidationTest {
    private final Http1Client client;

    public DeclarativeValidationTest(Http1Client client) {
        this.client = client;
    }

    @Test
    void testValid() {
        var response = client.post("/validate")
                .accept(MediaTypes.APPLICATION_JSON)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(new MyDto("goodexample", 14), MyDto.class);

        assertThat(response.status(), is(Status.OK_200));
        MyDto entity = response.entity();
        assertThat(entity.name(), is("goodexample"));
        assertThat(entity.age(), is(14));
    }

    @Test
    void testInvalidNameEmpty() {
        var response = client.post("/validate")
                .accept(MediaTypes.APPLICATION_JSON)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(new MyDto("", 14), String.class);

        // we have the dangerous "include-entity" enabled in application.yaml for this example
        // it can be enabled for testing, and disabled for runtime
        assertThat(response.status(), is(Status.BAD_REQUEST_400));
        String entity = response.entity();
        assertThat(entity, startsWith("Constraint validation failed: is blank"));
    }

    @Test
    void testInvalidNamePattern() {
        var response = client.post("/validate")
                .accept(MediaTypes.APPLICATION_JSON)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(new MyDto("good", 14), String.class);

        // we have the dangerous "include-entity" enabled in application.yaml for this example
        // it can be enabled for testing, and disabled for runtime
        assertThat(response.status(), is(Status.BAD_REQUEST_400));
        String entity = response.entity();
        assertThat(entity, startsWith("Constraint validation failed: does not match pattern"));
    }


    @Test
    void testInvalidAgeMin() {
        var response = client.post("/validate")
                .accept(MediaTypes.APPLICATION_JSON)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(new MyDto("goodexample", -1), String.class);

        // we have the dangerous "include-entity" enabled in application.yaml for this example
        // it can be enabled for testing, and disabled for runtime
        assertThat(response.status(), is(Status.BAD_REQUEST_400));
        String entity = response.entity();
        assertThat(entity, startsWith("Constraint validation failed: -1 is less than 0"));
    }

    @Test
    void testInvalidAgeMax() {
        var response = client.post("/validate")
                .accept(MediaTypes.APPLICATION_JSON)
                .contentType(MediaTypes.APPLICATION_JSON)
                .submit(new MyDto("goodexample", 121), String.class);

        // we have the dangerous "include-entity" enabled in application.yaml for this example
        // it can be enabled for testing, and disabled for runtime
        assertThat(response.status(), is(Status.BAD_REQUEST_400));
        String entity = response.entity();
        assertThat(entity, startsWith("Constraint validation failed: 121 is more than 120"));
    }
}

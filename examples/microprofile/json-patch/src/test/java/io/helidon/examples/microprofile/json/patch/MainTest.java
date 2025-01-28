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
package io.helidon.examples.microprofile.json.patch;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@HelidonTest
public class MainTest {

    private final WebTarget target;

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    public MainTest(WebTarget target) {
        this.target = target;
    }

    @Test
    void testJsonPatch() {
        JsonObject json = target.path("patch").request().get(JsonObject.class);
        assertThat(json.isEmpty(), is(true));

        JsonObject payload = Json.createObjectBuilder().add("foo", "bar").build();
        try (Response response = target.path("patch")
                .request()
                .put(Entity.json(payload))) {
            assertThat(response.getStatus(), is(Status.NO_CONTENT_204.code()));
        }

        json = target.path("patch").request().get(JsonObject.class);
        assertThat(payload, is(json));

        JsonObject patch = Json.createObjectBuilder()
                .add("op", "replace")
                .add("path", "/foo")
                .add("value", "123")
                .build();
        JsonArray jsonPatch = Json.createArrayBuilder().add(patch).build();
        try (Response response = target.path("patch")
                .request()
                .post(Entity.entity(jsonPatch, MediaTypes.APPLICATION_JSON_PATCH_JSON.text()))) {
            assertThat(response.getStatus(), is(Status.NO_CONTENT_204.code()));
        }

        json = target.path("patch").request().get(JsonObject.class);
        assertThat(json.getString("foo"), is("123"));
    }
}

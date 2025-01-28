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
package io.helidon.examples.media.json.patch;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
class MainTest {

    private final Http1Client client;

    protected MainTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        Main.routing(builder);
    }

    @Test
    void testJsonPatch() {
        JsonObject json = client.get("patch").request(JsonObject.class).entity();
        assertThat(json.isEmpty(), is(true));

        JsonObject payload = Json.createObjectBuilder().add("foo", "bar").build();
        try (Http1ClientResponse response = client.put("patch")
                .submit(payload)) {
            assertThat(response.status(), is(Status.OK_200));
        }

        json = client.get("patch").request(JsonObject.class).entity();
        assertThat(payload, is(json));

        JsonObject patch = Json.createObjectBuilder()
                .add("op", "replace")
                .add("path", "/foo")
                .add("value", "123")
                .build();
        JsonArray jsonPatch = Json.createArrayBuilder().add(patch).build();
        try (Http1ClientResponse response = client.post("patch")
                .header(HeaderNames.CONTENT_TYPE, MediaTypes.APPLICATION_JSON_PATCH_JSON.text())
                .submit(jsonPatch)) {
            assertThat(response.status(), is(Status.OK_200));
        }

        json = client.get("patch").request(JsonObject.class).entity();
        assertThat(json.getString("foo"), is("123"));
    }
}

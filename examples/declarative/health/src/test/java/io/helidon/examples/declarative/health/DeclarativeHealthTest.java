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

package io.helidon.examples.declarative.health;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@ServerTest
class DeclarativeHealthTest {
    private final Http1Client client;

    public DeclarativeHealthTest(Http1Client client) {
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
    void testUpdateGreeting() {
        var response = client.post("/hello")
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola");

        assertThat(response.status(), is(Status.NO_CONTENT_204));
        response.close();

        try {
            var entity = client.get("/hello")
                    .accept(MediaTypes.TEXT_PLAIN)
                    .requestEntity(String.class);
            assertThat(entity, is("Hola World"));
        } finally {
            client.post("/hello")
                    .contentType(MediaTypes.TEXT_PLAIN)
                    .submit("Hello")
                    .close();
        }
    }

    @Test
    void testHealthOk() {
        var response = client.get("/observe/health")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(JsonObject.class);

        assertThat(response.status(), is(Status.OK_200));

        var entity = response.entity();
        assertThat(entity.stringValue("status", "wrong"), is("UP"));
        var checks = entity.arrayValue("checks")
                .map(JsonArray::values)
                .orElseGet(List::of);
        assertThat(checks, hasSize(1));
        var check = checks.getFirst().asObject();
        assertThat(check.stringValue("name", "wrong"), is("greeting"));
        assertThat(check.stringValue("status", "wrong"), is("UP"));
    }

    @Test
    void testHealthNok() {
        var postResponse = client.post("/hello")
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola", String.class);

        assertThat(postResponse.status(), is(Status.NO_CONTENT_204));

        try {
            var response = client.get("/observe/health")
                    .accept(MediaTypes.APPLICATION_JSON)
                    .request(JsonObject.class);

            assertThat(response.status(), is(Status.SERVICE_UNAVAILABLE_503));

            var entity = response.entity();
            assertThat(entity.stringValue("status", "wrong"), is("DOWN"));
            var checks = entity.arrayValue("checks")
                    .map(JsonArray::values)
                    .orElseGet(List::of);
            assertThat(checks, hasSize(1));
            var check = checks.getFirst().asObject();
            assertThat(check.stringValue("name", "wrong"), is("greeting"));
            assertThat(check.stringValue("status", "wrong"), is("DOWN"));
            var data = check.objectValue("data").orElseThrow();
            assertThat(data.stringValue("greeting", "wrong"), is("Hola"));
        } finally {
            client.post("/hello")
                    .contentType(MediaTypes.TEXT_PLAIN)
                    .submit("Hello")
                    .close();
        }
    }
}

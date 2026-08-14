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

package io.helidon.examples.declarative.cors;

import java.util.List;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.json.JsonArray;
import io.helidon.json.JsonObject;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static io.helidon.common.testing.http.junit5.HttpHeaderMatcher.hasHeaderValue;
import static io.helidon.common.testing.http.junit5.HttpHeaderMatcher.noHeader;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_MAX_AGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@ServerTest
class DeclarativeCorsTest {
    private final Http1Client client;

    public DeclarativeCorsTest(Http1Client client) {
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
    void testAppCorsOk() {
        // let's do a pre-flight check against CORS configuration in HelloWorldEndpoint.java (annotated)
        var response = client.options("/hello")
                .header(HeaderNames.ORIGIN, "http://www.example.com")
                .header(HeaderNames.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));

        var headers = response.headers();
        assertThat(headers, hasHeaderValue(ACCESS_CONTROL_ALLOW_ORIGIN, is("http://www.example.com")));
        assertThat(headers, hasHeaderValue(ACCESS_CONTROL_ALLOW_METHODS, is("POST")));
        assertThat(headers, hasHeaderValue(ACCESS_CONTROL_MAX_AGE, is("60")));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_HEADERS));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void testAppCorsBadOrigin() {
        // let's do a pre-flight check against CORS configuration in application.yaml
        var response = client.options("/hello")
                .header(HeaderNames.ORIGIN, "http://www.foo.com")
                .header(HeaderNames.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .request(String.class);

        assertThat(response.status(), is(Status.FORBIDDEN_403));

        var headers = response.headers();
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_METHODS));
        assertThat(headers, noHeader(ACCESS_CONTROL_MAX_AGE));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_HEADERS));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void testHealthOk() {
        try (var response = client.get("/observe/health")
                .accept(MediaTypes.APPLICATION_JSON)
                .request()) {
            assertThat(response.status(), is(Status.NO_CONTENT_204));
            assertThat(response.entity().hasEntity(), is(false));
        }
    }

    @Test
    void testHealthCorsOk() {
        // let's do a pre-flight check against CORS configuration in application.yaml
        var response = client.options("/observe/health")
                .header(HeaderNames.ORIGIN, "http://www.example.com")
                .header(HeaderNames.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));

        var headers = response.headers();
        assertThat(headers, hasHeaderValue(ACCESS_CONTROL_ALLOW_ORIGIN, is("http://www.example.com")));
        assertThat(headers, hasHeaderValue(ACCESS_CONTROL_ALLOW_METHODS, is("GET")));
        assertThat(headers, hasHeaderValue(ACCESS_CONTROL_MAX_AGE, is("3600")));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_HEADERS));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void testHealthCorsBadOrigin() {
        // let's do a pre-flight check against CORS configuration in application.yaml
        var response = client.options("/observe/health")
                .header(HeaderNames.ORIGIN, "http://www.foo.com")
                .header(HeaderNames.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .request(String.class);

        assertThat(response.status(), is(Status.FORBIDDEN_403));

        var headers = response.headers();
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_ORIGIN));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_METHODS));
        assertThat(headers, noHeader(ACCESS_CONTROL_MAX_AGE));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_HEADERS));
        assertThat(headers, noHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }
}

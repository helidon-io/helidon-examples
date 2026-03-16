/*
 * Copyright (c) 2020, 2026 Oracle and/or its affiliates.
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

package io.helidon.examples.cors;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Headers;
import io.helidon.http.WritableHeaders;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientRequest;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.helidon.common.testing.http.junit5.HttpHeaderMatcher.hasHeader;
import static io.helidon.common.testing.http.junit5.HttpHeaderMatcher.hasHeaderValue;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.helidon.http.HeaderNames.ACCESS_CONTROL_REQUEST_METHOD;
import static io.helidon.http.HeaderNames.HOST;
import static io.helidon.http.HeaderNames.ORIGIN;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("HttpUrlsUsage")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ServerTest
public class MainTest {

    private final Http1Client client;

    MainTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    public static void setup(HttpRouting.Builder routing) {
        Main.routing(routing);
    }

    @Order(1) // Make sure this runs before the greeting message changes so responses are deterministic.
    @Test
    public void testHelloWorld() {
        try (Http1ClientResponse response = client.get("/greet")
                .accept(MediaTypes.APPLICATION_JSON)
                .request()) {

            assertThat(response.status().code(), is(200));

            String payload = GreetingMessage.fromRest(response.entity().as(JsonObject.class)).getMessage();
            assertThat(payload, is("Hello World!"));
        }

        try (Http1ClientResponse response = client.get("/greet/Joe")
                .accept(MediaTypes.APPLICATION_JSON)
                .request()) {

            assertThat(response.status().code(), is(200));

            String payload = GreetingMessage.fromRest(response.entity().as(JsonObject.class)).getMessage();
            assertThat(payload, is("Hello Joe!"));
        }

        try (Http1ClientResponse response = client.put("/greet/greeting")
                .accept(MediaTypes.APPLICATION_JSON)
                .submit(new GreetingMessage("Hola").forRest())) {

            assertThat(response.status().code(), is(204));
        }

        try (Http1ClientResponse response = client.get("/greet/Jose")
                .accept(MediaTypes.APPLICATION_JSON)
                .request()) {

            assertThat(response.status().code(), is(200));

            String payload = GreetingMessage.fromRest(response.entity().as(JsonObject.class)).getMessage();
            assertThat(payload, is("Hola Jose!"));
        }

        try (Http1ClientResponse response = client.get("/observe/health").request()) {
            assertThat(response.status().code(), is(204));
        }

        try (Http1ClientResponse response = client.get("/observe/metrics").request()) {
            assertThat(response.status().code(), is(200));
        }
    }

    @Order(10)
    // Run after the non-CORS tests (so the greeting is Hola) but before the CORS test that changes the greeting again.
    @Test
    void testAnonymousGreetWithCors() {
        try (Http1ClientResponse response = client.get()
                .path("/greet")
                .accept(MediaTypes.APPLICATION_JSON)
                .headers(it -> it
                        .set(ORIGIN, "http://foo.com")
                        .set(HOST, "here.com"))
                .request()) {

            assertThat(response.status().code(), is(200));
            String payload = GreetingMessage.fromRest(response.entity().as(JsonObject.class)).getMessage();
            assertThat(payload, containsString("Hola World"));
            Headers responseHeaders = response.headers();
            assertThat(responseHeaders, hasHeaderValue(ACCESS_CONTROL_ALLOW_ORIGIN, is("http://foo.com")));
        }
    }

    @Order(11) // Run after the non-CORS tests but before other CORS tests.
    @Test
    void testGreetingChangeWithCors() {
        // Send the pre-flight request and check the response.
        WritableHeaders<?> preFlightHeaders = WritableHeaders.create();
        try (Http1ClientResponse response = client.options()
                .path("/greet/greeting")
                .headers(it -> it
                        .set(ORIGIN, "http://foo.com")
                        .set(HOST, "here.com")
                        .set(ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .request()) {

            assertThat(response.headers(), hasHeader(ACCESS_CONTROL_ALLOW_METHODS, "PUT", "DELETE", "GET"));
            assertThat(response.headers(), hasHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "http://foo.com"));
        }

        // Send the follow-up request.
        GreetingMessage payload = new GreetingMessage("Cheers");
        try (Http1ClientResponse response = client.put("/greet/greeting")
                .accept(MediaTypes.APPLICATION_JSON)
                .headers(headers -> {
                    headers.set(ORIGIN, "http://foo.com");
                    headers.set(HOST, "here.com");
                    preFlightHeaders.forEach(headers::add);
                }).submit(payload.forRest())) {

            assertThat(response.status().code(), is(204));

            assertThat(response.headers(), hasHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "http://foo.com"));
        }
    }

    @Order(12) // Run after CORS test changes greeting to Cheers.
    @Test
    void testNamedGreetWithCors() {
        try (Http1ClientResponse response = client.get()
                .path("/greet/Maria")
                .headers(headers -> headers
                        .set(ORIGIN, "http://foo.com")
                        .set(HOST, "here.com"))
                .request()) {
            assertThat("HTTP response", response.status().code(), is(200));
            String payload = GreetingMessage.fromRest(response.entity().as(JsonObject.class)).getMessage();
            assertThat(payload, containsString("Cheers Maria"));

            assertThat(response.headers(), hasHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "http://foo.com"));
        }
    }

    @Order(100) // After all other tests, so we can rely on deterministic greetings.
    @Test
    void testGreetingChangeWithCorsAndOtherOrigin() {
        Http1ClientRequest request = client.put()
                .path("/greet/greeting");
        request.headers(headers -> {
            headers.set(ORIGIN, "http://other.com");
            headers.set(HOST, "here.com");
        });

        GreetingMessage payload = new GreetingMessage("Ahoy");
        try (Http1ClientResponse response = request.submit(payload.forRest())) {
            // Result depends on whether we are using overrides or not.
            assertThat("HTTP response3", response.status().code(), is(403));
        }
    }
}

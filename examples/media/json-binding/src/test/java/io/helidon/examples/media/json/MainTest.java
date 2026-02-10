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

package io.helidon.examples.media.json;

import java.util.Optional;

import io.helidon.http.Status;
import io.helidon.json.JsonObject;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
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
    void testValueRoute() {
        try (Http1ClientResponse response = client.get("/value").request()) {
            assertThat(response.status(), is(Status.OK_200));
            JsonObject json = response.as(JsonObject.class);
            Optional<String> message = json.stringValue("message");
            assertThat(message.isPresent(), is(true));
            assertThat(message.get(), is("Hello World!"));
        }
    }

    @Test
    void testObjectRoute() {
        try (Http1ClientResponse response = client.get("/object").request()) {
            assertThat(response.status(), is(Status.OK_200));
            var greetingsResponse = response.as(GreetingsObjectService.GreetingsResponse.class);
            assertThat(greetingsResponse.message(), is("Hello World!"));
        }
    }

}

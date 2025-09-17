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

package io.helidon.examples.telemetry.se.otelconfig;

import java.util.List;

import io.helidon.http.Status;
import io.helidon.telemetry.testing.tracing.JsonLogConverter;
import io.helidon.webclient.api.ClientResponseTyped;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

abstract class AbstractMainTest {

    private final Http1Client client;

    protected AbstractMainTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        io.helidon.examples.telemetry.se.otelconfig.Main.routing(builder);
    }

    @Test
    void testGreeting() {
        ClientResponseTyped<JsonObject> response = client.get("/greet").request(JsonObject.class);
        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.entity().getString("message"), is("Hello World!"));
    }

    @Test
    void testSimpleGreet() {
        ClientResponseTyped<String> response = client.get("/simple-greet").request(String.class);
        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.entity(), is("Hello World!"));
    }

    @Test
    void testSpanForNamedGreeting() throws Exception {


        try (JsonLogConverter converter = JsonLogConverter.create()) {
            ClientResponseTyped<String> response = client.get("/greeting/Joe").request(String.class);

            List<JsonLogConverter.LogResourceScopeSpans> scopeSpans = converter.resourceSpans(2);

            for (var scopeSpan : scopeSpans) {
                assertThat("String attributes",
                           scopeSpan.resource().attributes(),
                           allOf(hasEntry("service.name", "otel-config-example"),
                                 hasEntry("x", "x-value")));
                assertThat("Numeric attributes",
                           scopeSpan.resource().attributes(),
                           hasEntry("y", 9));
            }

            var childSpan = scopeSpans.get(0).scopeSpans().getFirst().logSpans().getFirst();
            var parentSpan = scopeSpans.get(1).scopeSpans().getFirst().logSpans().getFirst();

            assertThat("Parent of child span", childSpan.parentSpanId(), is(parentSpan.spanId()));

        }
    }

}

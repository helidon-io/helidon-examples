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

package io.helidon.examples.declarative.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.HeaderNames;
import io.helidon.http.Status;
import io.helidon.telemetry.testing.tracing.JsonLogConverter;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;

@ServerTest
class DeclarativeTracingTest {
    private final Http1Client client;

    DeclarativeTracingTest(Http1Client client) {
        this.client = client;
    }

    @Test
    void testNamedGreetingTracing() throws Exception {
        String userAgent = "DeclarativeTracingTest";

        try (JsonLogConverter converter = JsonLogConverter.create()) {
            var response = client.get("/hello/Joe")
                    .accept(MediaTypes.TEXT_PLAIN)
                    .header(HeaderNames.USER_AGENT, userAgent)
                    .request(String.class);

            assertThat(response.status(), is(Status.OK_200));
            assertThat(response.entity(), is("Hello Joe"));

            List<JsonLogConverter.LogSpan> spans = spans(converter.resourceSpans(3));

            Map<String, JsonLogConverter.LogSpan> spansByName = spans.stream()
                    .collect(Collectors.toMap(JsonLogConverter.LogSpan::name, span -> span));

            JsonLogConverter.LogSpan httpRequest = spansByName.get("HTTP Request");
            JsonLogConverter.LogSpan contentWrite = spansByName.get("content-write");
            JsonLogConverter.LogSpan greetName = spansByName.get("greet-name");

            assertThat(httpRequest, notNullValue());
            assertThat(contentWrite, notNullValue());
            assertThat(greetName, notNullValue());
            assertThat(httpRequest.traceId(), is(contentWrite.traceId()));
            assertThat(httpRequest.traceId(), is(greetName.traceId()));
            assertThat(contentWrite.parentSpanId(), is(httpRequest.spanId()));
            assertThat(greetName.parentSpanId(), is(httpRequest.spanId()));
            assertThat(greetName.attributes(), hasEntry("example", "declarative-tracing"));
            assertThat(greetName.attributes(), hasEntry("route", "named"));
            assertThat(greetName.attributes(), hasEntry("userAgent", userAgent));
        }
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

    private static List<JsonLogConverter.LogSpan> spans(List<JsonLogConverter.LogResourceScopeSpans> resourceSpans) {
        List<JsonLogConverter.LogSpan> result = new ArrayList<>();
        for (JsonLogConverter.LogResourceScopeSpans resourceSpan : resourceSpans) {
            assertThat(resourceSpan.resource().attributes(),
                       hasEntry("service.name", "helidon-examples-declarative-tracing"));
            for (JsonLogConverter.LogScopeSpans scopeSpans : resourceSpan.scopeSpans()) {
                result.addAll(scopeSpans.logSpans().stream()
                                      .map(JsonLogConverter.LogSpan.class::cast)
                                      .toList());
            }
        }
        return result;
    }
}

/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.webserver.sse;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.json.JsonObject;

import io.helidon.http.sse.SseEvent;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.sse.SseSource;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

import static io.helidon.http.HeaderValues.ACCEPT_EVENT_STREAM;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
class SseServiceTest {

    private final Http1Client client;

    SseServiceTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRules rules) {
        rules.register(new SseService());
    }

    @Test
    void testSseText() throws InterruptedException {
        try (var response = client.get("/sse_text")
                .queryParam("count", "3")
                .header(ACCEPT_EVENT_STREAM).request()) {
            var latch = new CountDownLatch(3);
            var events = new ArrayList<SseEvent>();
            response.source(SseSource.TYPE, event -> {
                events.add(event);
                latch.countDown();
            });
            assertThat(latch.await(5, TimeUnit.SECONDS), is(true));
            assertThat(events.size(), is(3));
            for (int i = 0; i < 3; i++) {
                var event = events.get(i);
                assertThat(event.comment().orElse(null), is("comment#" + i));
                assertThat(event.name().orElse(null), is("my-event"));
                assertThat(event.data(String.class), is("data#" + i));
            }
        }
    }

    @Test
    void testSseJson() throws InterruptedException {
        try (var response = client.get("/sse_json")
                .queryParam("count", "3")
                .header(ACCEPT_EVENT_STREAM).request()) {
            var latch = new CountDownLatch(3);
            var events = new ArrayList<JsonObject>();
            response.source(SseSource.TYPE, event -> {
                events.add(event.data(JsonObject.class));
                latch.countDown();
            });
            assertThat(latch.await(5, TimeUnit.SECONDS), is(true));
            assertThat(events.size(), is(3));
            for (int i = 0; i < 3; i++) {
                var event = events.get(i);
                assertThat(event, is(notNullValue()));
                assertThat(event.getString("data", null), is("data#" + i));
            }
        }
    }
}

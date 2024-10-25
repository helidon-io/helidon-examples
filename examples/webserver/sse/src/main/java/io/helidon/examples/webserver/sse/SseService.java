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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.helidon.http.sse.SseEvent;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

import jakarta.json.spi.JsonProvider;

/**
 * An HTTP that sends SSE.
 */
class SseService implements HttpService {

    private static final JsonProvider JSON_PROVIDER = JsonProvider.provider();

    @Override
    public void routing(HttpRules httpRules) {
        httpRules.get("/sse_text", this::sseText)
                .get("/sse_json", this::sseJson);
    }

    void sseText(ServerRequest req, ServerResponse res) throws InterruptedException {
        int count = req.query().first("count").asInt().orElse(1);
        int delay = req.query().first("delay").asInt().orElse(0);
        try (SseSink sseSink = res.sink(SseSink.TYPE)) {
            for (int i = 0; i < count; i++) {
                sseSink.emit(SseEvent.builder()
                        .comment("comment#" + i)
                        .name("my-event")
                        .data("data#" + i)
                        .build());

                if (delay > 0) {
                    Thread.sleep(Duration.ofSeconds(delay));
                }
            }
        }
    }

    void sseJson(ServerRequest req, ServerResponse res) throws InterruptedException {
        int count = req.query().first("count").asInt().orElse(1);
        int delay = req.query().first("delay").asInt().orElse(0);
        try (SseSink sseSink = res.sink(SseSink.TYPE)) {
            for (int i = 0; i < count; i++) {
                sseSink.emit(SseEvent.create(JSON_PROVIDER.createObjectBuilder()
                        .add("data", "data#" + i)
                        .build()));

                if (delay > 0) {
                    Thread.sleep(Duration.ofSeconds(delay));
                }
            }
        }
    }
}

/*
 * Copyright (c) 2025, 2026 Oracle and/or its affiliates.
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
package io.helidon.examples.dbclient.tracing;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.webclient.api.ClientResponseTyped;
import io.helidon.webclient.api.Proxy;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.WebServerConfig;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpServer;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@Testcontainers(disabledWithoutDocker = true)
@ServerTest
class DbClientTracingTest {

    private static final DockerImageName IMAGE = DockerImageName.parse("cr.jaegertracing.io/jaegertracing/jaeger:2.10.0");

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> CONTAINER = new GenericContainer<>(IMAGE)
            .withExposedPorts(14250, 16686)
            .waitingFor(Wait.forListeningPorts(14250, 16686)
                                .withStartupTimeout(Duration.ofMinutes(2)));

    private final Http1Client jaegerClient = Http1Client.builder()
            .baseUri(jaegerUrl())
            .proxy(Proxy.noProxy())
            .build();

    static String jaegerUrl() {
        return "http://localhost:%d".formatted(CONTAINER.getMappedPort(16686));
    }

    static int tracingPort() {
        return CONTAINER.getMappedPort(14250);
    }

    @SetUpServer
    static void setUp(WebServerConfig.Builder server) {
        Config config = Config.builder()
                .addSource(ConfigSources.create(Map.of(
                        "tracing.port", String.valueOf(tracingPort()),
                        "tracing.span-processor-type", "simple")))
                .metaConfig()
                .build();
        server.addFeature(Main.observeFeature(config));
        server.routing(Main::routing);
    }

    @Test
    void testTracing(Http1Client client) {
        // create
        try (var rsp = client.post("/db/foo").submit("bar")) {
            assertThat(rsp.status().code(), is(201));
        }

        // verify created
        try (var rsp = client.get("/db/foo").request()) {
            assertThat(rsp.status().code(), is(200));
            assertThat(rsp.entity().as(String.class), is("bar"));
        }

        // update
        try (var rsp = client.put("/db/foo").submit("bob")) {
            assertThat(rsp.status().code(), is(200));
        }

        // verify updated
        try (var rsp = client.get("/db/foo").request()) {
            assertThat(rsp.status().code(), is(200));
            assertThat(rsp.entity().as(String.class), is("bob"));
        }

        // delete
        try (var rsp = client.delete("/db/foo").request()) {
            assertThat(rsp.status().code(), is(200));
        }

        // verify deleted
        try (var rsp = client.get("/db/foo").request()) {
            assertThat(rsp.status().code(), is(404));
        }

        try {
            checkTraces();
        } catch (Throwable t) {
            try {
                Thread.sleep(Duration.ofSeconds(2).toMillis());
                checkTraces();
            } catch (Throwable tx) {
                // ignore the second run, just throw the first failure if fails again
                throw t;
            }
        }
    }

    private void checkTraces() {
        // there is a delay between the requests and the time Jaeger collects all the traces, let's retry once
        ClientResponseTyped<JsonObject> response = jaegerClient.get("/api/traces")
                .accept(MediaTypes.APPLICATION_JSON)
                .queryParam("service", "helidon-examples-dbclient-tracing")
                .request(JsonObject.class);

        assertThat(response.status().code(), is(200));
        JsonObject jsonObject = response.entity();

        List<JsonValue> tags = Optional.ofNullable(jsonObject.getJsonArray("data")).stream()
                .flatMap(Collection::stream)
                .map(JsonValue::asJsonObject)
                .flatMap(it -> Optional.ofNullable(it.getJsonArray("spans")).stream())
                .flatMap(Collection::stream)
                .map(JsonValue::asJsonObject)
                .flatMap(it -> Optional.ofNullable(it.getJsonArray("tags")).stream())
                .flatMap(Collection::stream)
                .toList();

        assertThat(tags, hasItem(allOf(
                hasEntry("key", Json.createValue("component")),
                hasEntry("value", Json.createValue("dbclient")))));
    }
}

/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates.
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

package io.helidon.examples.metrics.filtering.se;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import io.helidon.webserver.WebServer;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class MainTest {

    private static WebServer webServer;
    private static WebClient webClient;
    private static final JsonBuilderFactory JSON_BUILDER = Json.createBuilderFactory(Collections.emptyMap());
    private static final JsonObject TEST_JSON_OBJECT;

    static {
        TEST_JSON_OBJECT = JSON_BUILDER.createObjectBuilder()
                .add("greeting", "Hola")
                .build();
    }

    @BeforeAll
    public static void startTheServer() {
        webServer = Main.startServer().await();

        webClient = WebClient.builder()
                .baseUri("http://localhost:" + webServer.port())
                .addMediaSupport(JsonpSupport.create())
                .build();
    }

    @AfterAll
    public static void stopServer() {
        if (webServer != null) {
            webServer.shutdown()
                    .await(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testHelloWorld() {
        JsonObject jsonObject;
        WebClientResponse response;

        jsonObject = webClient.get()
                .path("/greet")
                .request(JsonObject.class)
                .await();
        assertThat(jsonObject.getString("message"), is("Hello World!"));

        jsonObject = webClient.get()
                .path("/greet/Joe")
                .request(JsonObject.class)
                .await();
        assertThat(jsonObject.getString("message"), is("Hello Joe!"));

        response = webClient.put()
                .path("/greet/greeting")
                .submit(TEST_JSON_OBJECT)
                .await();
        assertThat(response.status().code(), is(204));

        jsonObject = webClient.get()
                .path("/greet/Joe")
                .request(JsonObject.class)
                .await();
        assertThat(jsonObject.getString("message"), is("Hola Joe!"));

        response = webClient.get()
                .path("/metrics")
                .request()
                .await();
        assertThat(response.status().code(), is(200));
    }

    @Test
    public void testMetrics() {
        WebClientResponse response;

        String get = webClient.get()
                .path("/greet")
                .request(String.class)
                .await();

        assertThat(get, containsString("Hello World!"));

        get = webClient.get()
                .path("/greet/Joe")
                .request(String.class)
                .await();

        assertThat(get, containsString("Hello Joe!"));

        String openMetricsOutput = webClient.get()
                .path("/metrics/application")
                .request(String.class)
                .await();

        assertThat("Metrics output", openMetricsOutput, not(containsString(GreetService.TIMER_FOR_GETS)));
        assertThat("Metrics output", openMetricsOutput, containsString(GreetService.COUNTER_FOR_PERSONALIZED_GREETINGS));
    }
}
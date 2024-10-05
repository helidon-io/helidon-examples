/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates.
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
package io.helidon.examples.media.form;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.helidon.common.parameters.Parameters;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Tests {@link io.helidon.examples.media.form.FormService}.
 */
@TestMethodOrder(OrderAnnotation.class)
@ServerTest
public class FormServiceTest {
    private final Http1Client client;

    FormServiceTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        Main.routing(builder);
    }

    @Test
    @Order(1)
    public void testPost() {
        Map<String, List<String>> data = Map.of("foo", List.of("bar1", "bar2"));
        try (Http1ClientResponse response = client.post("/api")
                                                  .followRedirects(false)
                                                  .submit(Parameters.create("form", data))) {
            assertThat(response.status(), is(Status.MOVED_PERMANENTLY_301));
        }
    }

    @Test
    @Order(3)
    public void testList() {
        try (Http1ClientResponse response = client.get("/api").request()) {
            assertThat(response.status(), is(Status.OK_200));
            JsonObject json = response.as(JsonObject.class);
            assertThat(json, is(notNullValue()));
            Map<String, List<String>> data = json.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            e -> e.getValue().asJsonArray().getValuesAs(JsonString::getString)));
            assertThat(data, is(Map.of("foo", List.of("bar1", "bar2"))));
        }
    }
}

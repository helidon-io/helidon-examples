/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.helidon.common.parameters.Parameters;
import io.helidon.http.Header;
import io.helidon.http.HeaderNames;
import io.helidon.http.HeaderValues;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObjectBuilder;

import static io.helidon.http.Status.MOVED_PERMANENTLY_301;

/**
 * Form service.
 */
public final class FormService implements HttpService {
    private static final Header UI_LOCATION = HeaderValues.createCached(HeaderNames.LOCATION, "/ui");
    private final JsonBuilderFactory jsonFactory = Json.createBuilderFactory(Map.of());
    private final Map<String, List<String>> data = new ConcurrentHashMap<>();

    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::list)
             .post("/", this::post);
    }

    private void list(ServerRequest req, ServerResponse res) {
        JsonObjectBuilder jsonBuilder = jsonFactory.createObjectBuilder();
        data.forEach((k, list) -> jsonBuilder.add(k, jsonFactory.createArrayBuilder(list)));
        res.send(jsonBuilder.build());
    }

    private void post(ServerRequest req, ServerResponse res) {
        Parameters form = req.content().asOptional(Parameters.class).orElseThrow();
        form.names().forEach(name -> data.compute(name, (k, v) -> addAll(v, form.all(k))));
        res.status(MOVED_PERMANENTLY_301)
           .header(UI_LOCATION)
           .send();
    }

    private static List<String> addAll(List<String> list, List<String> values) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.addAll(values);
        return list;
    }
}

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
package io.helidon.examples.media.json.patch;

import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.spi.JsonProvider;

/**
 * Http Service managing storage accessible at {@code /patch} endpoint.
 */
public class JsonPatchService implements HttpService {

    private static final JsonProvider JSON_PROVIDER = JsonProvider.provider();
    private final Storage storage = new Storage();

    @Override
    public void routing(HttpRules rules) {
        rules.get(this::get)
                .put(this::put)
                .post(this::patch);
    }

    private void get(ServerRequest req, ServerResponse res) {
        res.send(storage.object());
    }

    private void put(ServerRequest req, ServerResponse res) {
        JsonObject jsonObject = req.content().as(JsonObject.class);
        storage.object(jsonObject);
        res.send();
    }

    private void patch(ServerRequest req, ServerResponse res) {
        JsonArray operations = req.content().as(JsonArray.class);
        JsonPatch patch = JSON_PROVIDER.createPatch(operations);
        JsonObject jsonObject = patch.apply(storage.object());
        storage.object(jsonObject);
        res.send();
    }
}

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
package io.helidon.examples.microprofile.json.patch;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.spi.JsonProvider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

/**
 * Resource managing storage accessible at {@code /patch} endpoint.
 */
@Path("/patch")
@ApplicationScoped
public class JsonPatchResource {
    private static final JsonProvider JSON_PROVIDER = JsonProvider.provider();
    private final Storage storage = new Storage();

    /**
     * Get JSON object from storage.
     *
     * @return JSON object
     */
    @GET
    public JsonObject get() {
        return storage.object();
    }

    /**
     * Put a new JSON object in storage.
     *
     * @param jsonObject new JSON object
     */
    @PUT
    @Consumes("application/json")
    public void put(JsonObject jsonObject) {
        storage.object(jsonObject);
    }

    /**
     * Patch the JSON object in storage.
     *
     * @param operations JSON patch
     */
    @POST
    @Consumes("application/json-patch+json")
    public void patch(JsonArray operations) {
        JsonPatch patch = JSON_PROVIDER.createPatch(operations);
        JsonObject object = patch.apply(storage.object());
        storage.object(object);
    }
}

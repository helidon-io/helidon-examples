/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.microprofile.dynamic.config;

import java.util.Collections;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Provides a REST endpoint for retrieving dynamic configuration properties.
 *
 * @author [Dasarathi Rout]
 */
@Path("/dynamic")
@ApplicationScoped
public class DynamicConfigEndpoint {
    private static final Logger LOGGER = Logger.getLogger(DynamicConfigEndpoint.class.getName());
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    @Inject
    DynamicProperties dynamicProperties;

    /**
     * Retrieves the current dynamic configuration properties.
     *
     * @return a JSON object containing the startup message and application log level.
     */
    @Path("/config")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject getConfig() throws Exception {
        return JSON.createObjectBuilder()
                .add("startupMessage", dynamicProperties.getAppStartupMessage())
                .add("appLogLevel", dynamicProperties.getAppLogLevel())
                .add("appOptionalMessage", dynamicProperties.getOptionalMessage())
                .add("getDefaultMessage", dynamicProperties.getDefaultMessage())
                .add("getUrlConfigSources",dynamicProperties.getUrlConfigSources().toString())
                .build();
    }
}

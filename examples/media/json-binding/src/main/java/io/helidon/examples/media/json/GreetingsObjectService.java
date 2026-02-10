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

package io.helidon.examples.media.json;

import java.util.concurrent.atomic.AtomicReference;

import io.helidon.config.Config;
import io.helidon.http.Status;
import io.helidon.json.binding.Json;
import io.helidon.service.registry.Services;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * A simple service to greet you. Examples:
 * <p>
 * Get default greeting message:
 * {@code curl -X GET http://localhost:8080/object}
 * <p>
 * Get greeting message for Joe:
 * {@code curl -X GET http://localhost:8080/object/Joe}
 * <p>
 * Change greeting
 * {@code curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/object/greeting}
 * <p>
 * The message is returned as a JSON object.
 */
public class GreetingsObjectService implements HttpService {

    /**
     * The config value for the key {@code greeting}.
     */
    private final AtomicReference<String> greeting = new AtomicReference<>();

    GreetingsObjectService() {
        this(Services.get(Config.class).get("app"));
    }

    GreetingsObjectService(Config appConfig) {
        greeting.set(appConfig.get("greeting").asString().orElse("Ciao"));
    }

    /**
     * A service registers itself by updating the routing rules.
     *
     * @param rules the routing rules.
     */
    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::getDefaultMessageHandler)
                .get("/{name}", this::getMessageHandler)
                .put("/greeting", this::updateGreetingHandler);
    }

    /**
     * Return a worldly greeting message.
     *
     * @param serverRequest the server request
     * @param serverResponse the server response
     */
    private void getDefaultMessageHandler(ServerRequest serverRequest, ServerResponse serverResponse) {
        sendObjectResponse(serverResponse, "World");
    }

    /**
     * Return a greeting message using the name that was provided.
     *
     * @param serverRequest the server request
     * @param serverResponse the server response
     */
    private void getMessageHandler(ServerRequest serverRequest, ServerResponse serverResponse) {
        String name = serverRequest.path().pathParameters().get("name");
        sendObjectResponse(serverResponse, name);
    }

    private void sendObjectResponse(ServerResponse serverResponse, String name) {
        String msg = String.format("%s %s!", greeting.get(), name);

        GreetingsResponse greetingsResponse = new GreetingsResponse(msg);
        serverResponse.send(greetingsResponse);
    }

    private void updateGreetingFromJson(GreetingsUpdate greetingsUpdate, ServerResponse response) {
        if (greetingsUpdate.greetings() == null) {
            response.status(Status.BAD_REQUEST_400)
                    .send(new GreetingsError("No greeting provided"));
            return;
        }

        greeting.set(greetingsUpdate.greetings());
        response.status(Status.NO_CONTENT_204).send();
    }

    /**
     * Set the greeting to use in future messages.
     *
     * @param request the server request
     * @param response the server response
     */
    private void updateGreetingHandler(ServerRequest request,
                                       ServerResponse response) {
        updateGreetingFromJson(request.content().as(GreetingsUpdate.class), response);
    }

    @Json.Entity
    record GreetingsResponse(String message) {
    }

    @Json.Entity
    record GreetingsUpdate(String greetings) {
    }

    @Json.Entity
    record GreetingsError(String error) {
    }
}

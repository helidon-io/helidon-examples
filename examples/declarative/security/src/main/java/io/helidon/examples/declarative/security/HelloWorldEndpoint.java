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

package io.helidon.examples.declarative.security;

import java.util.concurrent.atomic.AtomicReference;

import io.helidon.common.Api;
import io.helidon.common.Default;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.config.Configuration;
import io.helidon.http.Http;
import io.helidon.http.Status;
import io.helidon.security.abac.role.RoleValidator;
import io.helidon.security.annotations.Authorized;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings(Api.SUPPRESS_INCUBATING) // Helidon declarative is an incubating feature
@RestServer.Endpoint // webserver declarative endpoint
@Http.Path("/hello") // path to serve this endpoint on
@Service.Singleton // service registry scope (must be singleton)
class HelloWorldEndpoint {
    private final AtomicReference<String> greeting = new AtomicReference<>();

    @Service.Inject
    HelloWorldEndpoint(@Default.Value("Ciao") @Configuration.Value("app.greeting") String greeting) {
        this.greeting.set(greeting);
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    String hello() {
        return greeting.get() + " World";
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/{name}")
    String hello(@Http.PathParam("name") String name) {
        return greeting.get() + " " + name;
    }

    @Http.Consumes(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.POST
    @RestServer.Status(Status.NO_CONTENT_204_CODE)
    @Authorized
    @RoleValidator.Roles("admin")
    void updateGreeting(@Http.Entity String newGreeting) {
        greeting.set(newGreeting);
    }
}

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

package io.helidon.examples.declarative.server;

import java.util.concurrent.atomic.AtomicReference;

import io.helidon.common.Default;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.config.Configuration;
import io.helidon.http.Http;
import io.helidon.http.Status;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

@SuppressWarnings("deprecation")
@RestServer.Endpoint
@Http.Path("/api/business")
@Service.Singleton
class BusinessServiceEndpoint {
    private final ScheduledTasks scheduledTasks;
    private final AtomicReference<String> greeting = new AtomicReference<>();

    @Service.Inject
    BusinessServiceEndpoint(ScheduledTasks scheduledTasks,
                            @Default.Value("Ciao") @Configuration.Value("app.greeting") String greeting) {
        this.scheduledTasks = scheduledTasks;
        this.greeting.set(greeting);
    }

    @Http.GET
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Path("/refresh")
    RefreshTaskDto refreshTask() {
        return new RefreshTaskDto(scheduledTasks.counter());
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/greet")
    String greeting() {
        return greeting.get() + " World";
    }

    @Http.GET
    @Http.Produces(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.Path("/greet/{name}")
    String greeting(@Http.PathParam("name") String name) {
        return greeting.get() + " " + name;
    }

    @Http.GET
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Path("/greet")
    GreetingDto jsonGreeting() {
        return new GreetingDto(greeting.get(), "World");
    }

    @Http.GET
    @Http.Produces(MediaTypes.APPLICATION_JSON_VALUE)
    @Http.Path("/greet/{name}")
    GreetingDto jsonGreeting(@Http.PathParam("name") String name) {
        return new GreetingDto(greeting.get(), name);
    }

    @Http.Consumes(MediaTypes.TEXT_PLAIN_VALUE)
    @Http.POST
    @Http.Path("/greet")
    @RestServer.Status(Status.NO_CONTENT_204_CODE)
    void updateGreeting(@Http.Entity String newGreeting) {
        greeting.set(newGreeting);
    }
}

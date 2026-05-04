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

package io.helidon.examples.declarative.webclient;

import java.util.concurrent.atomic.AtomicReference;

import io.helidon.config.Configuration;
import io.helidon.http.Http;
import io.helidon.http.Status;
import io.helidon.service.registry.Service;
import io.helidon.webserver.http.RestServer;

/**
 * Server side implementation of the {@link io.helidon.examples.declarative.webclient.HelloWorldApi}.
 */
@SuppressWarnings("deprecation")
@RestServer.Endpoint // webserver declarative endpoint
@Service.Singleton // service registry scope (must be singleton)
class HelloWorldServerEndpoint implements HelloWorldApi {
    private final AtomicReference<String> greeting = new AtomicReference<>();

    @Service.Inject
    HelloWorldServerEndpoint(@Configuration.Value("${app.greeting:Ciao}") String greeting) {
        this.greeting.set(greeting);
    }

    @Override
    public GreetingDto hello() {
        return new GreetingDto(greeting.get(), "World");
    }

    @Override
    public GreetingDto hello(String name) {
        return new GreetingDto(greeting.get(), name);
    }

    @Override
    @RestServer.Status(Status.NO_CONTENT_204_CODE)
    public void updateGreeting(@Http.Entity String newGreeting) {
        greeting.set(newGreeting);
    }
}

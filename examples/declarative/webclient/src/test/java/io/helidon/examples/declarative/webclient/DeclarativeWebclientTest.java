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

import io.helidon.service.registry.ServiceRegistry;
import io.helidon.webclient.api.RestClient;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ServerTest
public class DeclarativeWebclientTest {
    private final HelloWorldClientEndpoint client;

    public DeclarativeWebclientTest(ServiceRegistry registry, @RestClient.Client HelloWorldClientEndpoint client) {
        this.client = client;
    }

    @Test
    void testHelloWorld() {
        var entity = client.hello();

        assertThat(entity.greeting(), is("Hello"));
        assertThat(entity.name(), is("World"));
    }

    @Test
    void testHelloNamed() {
        var entity = client.hello("Test");

        assertThat(entity.greeting(), is("Hello"));
        assertThat(entity.name(), is("Test"));
    }

    @Test
    void testUpdateGreeting() {
        client.updateGreeting("Hola");

        try {
            var entity = client.hello();
            assertThat(entity.greeting(), is("Hola"));
            assertThat(entity.name(), is("World"));
        } finally {
            client.updateGreeting("Hello");
        }
    }
}

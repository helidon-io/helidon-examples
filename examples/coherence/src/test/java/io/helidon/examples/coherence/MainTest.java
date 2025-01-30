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
package io.helidon.examples.coherence;

import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ServerTest
public class MainTest {

    private final Http1Client client;

    protected MainTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void setUp(HttpRouting.Builder builder) {
        Main.routing(builder);
    }

    @Test
    void test() {
        Person person = new Person("Franck", "Helidon", "02/19/2019", "123-45-6789", 0);
        try (Http1ClientResponse response = client.post("creditscore").submit(person)) {
            assertThat(response.status(), is(Status.OK_200));
        }
    }
}

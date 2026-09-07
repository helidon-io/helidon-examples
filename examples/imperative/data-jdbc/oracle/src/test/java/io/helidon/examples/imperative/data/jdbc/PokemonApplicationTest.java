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
package io.helidon.examples.imperative.data.jdbc;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.http1.Http1ClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Verifies the Oracle sample with UCP backed by H2 in Oracle compatibility mode.
 */
@ServerTest
@SuppressWarnings("helidon:api:preview")
class PokemonApplicationTest {
    private static final String TEST_DATABASE_URL =
            "jdbc:h2:mem:pokemons;MODE=Oracle;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1";

    private final Http1Client client;

    PokemonApplicationTest(Http1Client client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder routing) {
        Main.routing(routing);
    }

    @BeforeAll
    static void initializeSchema() throws Exception {
        try (Connection connection = DriverManager.getConnection(TEST_DATABASE_URL, "sa", "");
             var script = new InputStreamReader(
                     Objects.requireNonNull(PokemonApplicationTest.class.getResourceAsStream("/schema.sql")), UTF_8)) {
            RunScript.execute(connection, script);
        }
    }

    @Test
    void queriesSeededDataWithNamedDataSource() {
        try (Http1ClientResponse response = client.get("/pokemon/count").request()) {
            String body = response.as(String.class);
            assertThat("Unexpected response from " + response.lastEndpointUri() + ": " + body,
                       response.status().code(),
                       is(200));
            assertThat(body, is("12"));
        }
    }
}

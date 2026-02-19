/*
 * Copyright (c) 2021, 2026 Oracle and/or its affiliates.
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

package io.helidon.examples.todos.backend;

import java.util.Base64;
import java.util.Map;

import io.helidon.config.mp.MpConfigSources;
import io.helidon.http.HeaderNames;
import io.helidon.microprofile.security.SecurityCdiExtension;
import io.helidon.microprofile.testing.AddBean;
import io.helidon.microprofile.testing.AddConfigBlock;
import io.helidon.microprofile.testing.AddConfigSource;
import io.helidon.microprofile.testing.AddExtension;
import io.helidon.microprofile.testing.AddJaxRs;
import io.helidon.microprofile.testing.DisableDiscovery;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import io.helidon.microprofile.tracing.TracingCdiExtension;

import com.datastax.driver.core.Cluster;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@HelidonTest
@DisableDiscovery
@AddJaxRs
@AddExtension(TracingCdiExtension.class)
@AddExtension(SecurityCdiExtension.class)
@AddBean(DbService.class)
@AddBean(JaxRsBackendResource.class)
@AddConfigBlock("""
        tracing.enabled: false
        security.provider-policy.type=FIRST
        security.providers.0.google-login.enabled=false
        security.providers.2.http-signatures.enabled=false
        security.providers.3.http-basic-auth.realm=helidon
        security.providers.3.http-basic-auth.users.0.login=john
        security.providers.3.http-basic-auth.users.0.password=todo123
        """)
class BackendTests {

    @Container
    static final CassandraContainer CONTAINER = new CassandraContainer("cassandra:3.11.2");

    @AddConfigSource
    static ConfigSource config() {
        return MpConfigSources.create(Map.of("cassandra.port", String.valueOf(CONTAINER.getMappedPort(9042))));
    }

    @BeforeAll
    static void init() {
        try (var cluster = Cluster.builder()
                .withoutMetrics()
                .withPort(CONTAINER.getMappedPort(9042))
                .addContactPoint("localhost")
                .build();
                var session = cluster.newSession()) {
            session.execute("""
                    CREATE KEYSPACE backend WITH REPLICATION = {
                        'class' : 'SimpleStrategy',
                        'replication_factor' : 1
                    };
                    """);
            session.execute("""
                    CREATE TABLE backend.backend (
                        id ascii,
                        user ascii,
                        message ascii,
                        completed Boolean,
                        created timestamp,
                        PRIMARY KEY (id)
                    );
                    """);
        }
    }

    @Test
    void testTodoScenario(WebTarget webTarget) {
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("john:todo123".getBytes());
        JsonObject todo = Json.createObjectBuilder()
                .add("title", "todo title")
                .build();

        // Add a new todo
        JsonObject returnedTodo = webTarget
                .path("/api/backend")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderNames.AUTHORIZATION.defaultCase(), basicAuth)
                .post(Entity.json(todo), JsonObject.class);

        assertThat(returnedTodo.getString("user"), is("john"));
        assertThat(returnedTodo.getString("title"), is(todo.getString("title")));

        // Get the todo created earlier
        JsonObject fromServer = webTarget.path("/api/backend/" + returnedTodo.getString("id"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderNames.AUTHORIZATION.defaultCase(), basicAuth)
                .get(JsonObject.class);

        assertThat(fromServer, is(returnedTodo));

        // Update the todo created earlier
        JsonObject updatedTodo = Json.createObjectBuilder()
                .add("title", "updated title")
                .add("completed", false)
                .build();

        fromServer = webTarget.path("/api/backend/" + returnedTodo.getString("id"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderNames.AUTHORIZATION.defaultCase(), basicAuth)
                .put(Entity.json(updatedTodo), JsonObject.class);

        assertThat(fromServer.getString("title"), is(updatedTodo.getString("title")));

        // Delete the todo created earlier
        fromServer = webTarget.path("/api/backend/" + returnedTodo.getString("id"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderNames.AUTHORIZATION.defaultCase(), basicAuth)
                .delete(JsonObject.class);

        assertThat(fromServer.getString("id"), is(returnedTodo.getString("id")));

        // Get list of todos
        JsonArray jsonValues = webTarget.path("/api/backend")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderNames.AUTHORIZATION.defaultCase(), basicAuth)
                .get(JsonArray.class);

        assertThat("There should be no todos on server", jsonValues.size(), is(0));
    }
}

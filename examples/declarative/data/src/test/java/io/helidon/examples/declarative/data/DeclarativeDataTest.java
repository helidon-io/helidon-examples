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

package io.helidon.examples.declarative.data;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.Status;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webserver.testing.junit5.ServerTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Disabled("Missing a way to use the test container created port. Helidon team is working on this.")
@Testcontainers(disabledWithoutDocker = true)
@ServerTest
public class DeclarativeDataTest {
    private static final boolean IS_ARM = System.getProperty("os.arch", "amd64").equals("aarch64");
    private static final DockerImageName X86_IMAGE =
            DockerImageName.parse("container-registry.oracle.com/mysql/community-server:9.4.0")
                    .asCompatibleSubstituteFor("mysql");

    private static final DockerImageName ARM_IMAGE =
            DockerImageName.parse("container-registry.oracle.com/mysql/community-server:9.4.0-aarch64")
                    .asCompatibleSubstituteFor("mysql");

    @SuppressWarnings("resource")
    static final MySQLContainer<?> CONTAINER = new MySQLContainer<>(IS_ARM ? ARM_IMAGE : X86_IMAGE)
            .withUsername("user")
            .withPassword("changeIt")
            .withNetworkAliases("mysql")
            .withDatabaseName("db1");

    static {
        CONTAINER.start();
        // override configuration value to use the container's port
        System.setProperty("data.url", CONTAINER.getJdbcUrl());
    }

    private final Http1Client client;

    public DeclarativeDataTest(Http1Client client) {
        this.client = client;
    }

    @AfterAll
    static void stopContainer() {
        CONTAINER.stop();
    }

    @Test
    void testHelloWorld() {
        var response = client.get("/hello")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("Hello World"));
    }

    @Test
    void testHelloNamed() {
        var response = client.get("/hello/Test")
                .accept(MediaTypes.TEXT_PLAIN)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        String entity = response.entity();
        assertThat(entity, is("Hello Test"));
    }

    @Test
    void testUpdateGreeting() {
        var response = client.post("/hello")
                .contentType(MediaTypes.TEXT_PLAIN)
                .submit("Hola");

        assertThat(response.status(), is(Status.NO_CONTENT_204));
        response.close();

        try {
            var entity = client.get("/hello")
                    .accept(MediaTypes.TEXT_PLAIN)
                    .requestEntity(String.class);
            assertThat(entity, is("Hola World"));
        } finally {
            client.post("/hello")
                    .contentType(MediaTypes.TEXT_PLAIN)
                    .submit("Hello")
                    .close();
        }
    }
}

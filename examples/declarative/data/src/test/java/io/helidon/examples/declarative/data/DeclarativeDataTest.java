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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@Testcontainers(disabledWithoutDocker = true)
@ExtendWith(DeclarativeDataTest.ContainerJdbcConfig.class)
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
    @Container
    static final MySQLContainer<?> CONTAINER = new MySQLContainer<>(IS_ARM ? ARM_IMAGE : X86_IMAGE)
            .withUsername("user")
            .withPassword("changeit")
            .withNetworkAliases("mysql")
            .withDatabaseName("db1");

    private final Http1Client client;

    public DeclarativeDataTest(Http1Client client) {
        this.client = client;
    }

    @Test
    void testListPokemon() {
        var response = client.get("/pokemon/all")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.entity(), containsString("\"name\":\"Pikachu\""));
    }

    @Test
    void testFindPokemon() {
        var response = client.get("/pokemon/get/Pikachu")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.entity(), containsString("\"type\":\"Electric\""));
    }

    @Test
    void testListPokemonByType() {
        var response = client.get("/pokemon/type/Electric")
                .accept(MediaTypes.APPLICATION_JSON)
                .request(String.class);

        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.entity(), containsString("\"name\":\"Raichu\""));
    }

    static final class ContainerJdbcConfig implements BeforeAllCallback, AfterAllCallback {
        @Override
        public void beforeAll(ExtensionContext context) {
            var jdbcUrl = CONTAINER.getJdbcUrl();
            System.setProperty("data.url", jdbcUrl);
            System.setProperty("data.sources.sql.0.provider.hikari.url", jdbcUrl);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            System.clearProperty("data.url");
            System.clearProperty("data.sources.sql.0.provider.hikari.url");
        }
    }
}

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

package io.helidon.examples.integrations.cdi.datasource.hikaricp.postgres;

import java.nio.file.Path;
import java.util.Map;

import io.helidon.config.mp.MpConfigSources;
import io.helidon.microprofile.testing.AddConfigSource;
import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.ws.rs.client.WebTarget;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@Testcontainers(disabledWithoutDocker = true)
@HelidonTest
class TablesPostgresTest {

    private static final ImageFromDockerfile IMAGE = new ImageFromDockerfile("pgsql", false)
            .withFileFromPath(".", Path.of("etc/docker"));

    @Container
    static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer(IMAGE)
            .withUsername("user")
            .withPassword("pgsql123")
            .withDatabaseName("db1");

    @AddConfigSource
    static ConfigSource config() {
        return MpConfigSources.create(Map.of("javax.sql.DataSource.ds1.dataSource.url", CONTAINER.getJdbcUrl()));
    }

    @Test
    void testGet(WebTarget target) {
        try (var rsp = target.path("/tables").request().get()) {
            assertThat(rsp.getStatus(), is(200));
            String entity = rsp.readEntity(String.class);
            assertThat(entity.lines().toList(), hasItems("tables", "columns", "views"));
        }
    }
}

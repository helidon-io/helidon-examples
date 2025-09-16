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

package io.helidon.examples.integrations.cdi.datasource.hikaricp.mysql;

import java.util.Map;

import io.helidon.config.mp.MpConfigSources;
import io.helidon.microprofile.testing.AddConfigSource;
import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.ws.rs.client.WebTarget;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@Testcontainers(disabledWithoutDocker = true)
@HelidonTest
class TablesMySQLTest {

    private static final boolean IS_ARM = System.getProperty("os.arch", "amd64").equals("aarch64");
    private static final DockerImageName X86_IMAGE =
            DockerImageName.parse("container-registry.oracle.com/mysql/community-server:9.4.0")
                    .asCompatibleSubstituteFor("mysql");

    private static final DockerImageName ARM_IMAGE =
            DockerImageName.parse("container-registry.oracle.com/mysql/community-server:9.4.0-aarch64")
                    .asCompatibleSubstituteFor("mysql");

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> CONTAINER = new MySQLContainer<>(IS_ARM ? ARM_IMAGE : X86_IMAGE)
            .withUsername("user")
            .withPassword("mysql123")
            .withNetworkAliases("mysql")
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
            assertThat(entity.lines().toList(), hasItems("TABLES", "COLUMNS", "VIEWS"));
        }
    }
}

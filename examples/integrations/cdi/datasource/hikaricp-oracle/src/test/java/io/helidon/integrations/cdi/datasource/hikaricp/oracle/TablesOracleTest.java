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

package io.helidon.integrations.cdi.datasource.hikaricp.oracle;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import io.helidon.config.mp.MpConfigSources;
import io.helidon.microprofile.testing.AddConfigSource;
import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.ws.rs.client.WebTarget;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

@Testcontainers(disabledWithoutDocker = true)
@HelidonTest
class TablesOracleTest {

    private static final DockerImageName IMAGE = DockerImageName.parse("container-registry.oracle.com/database/free:latest-lite");

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> CONTAINER = new GenericContainer<>(IMAGE)
            .withEnv("ORACLE_PWD", "oracle123")
            .withExposedPorts(1521)
            .withStartupAttempts(3)
            .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE.*", 1)
                    .withStartupTimeout(Duration.ofMinutes(5)));

    static String jdbcUrl() {
        return "jdbc:oracle:thin:@localhost:%s/FREE".formatted(CONTAINER.getMappedPort(1521));
    }

    @AddConfigSource
    static ConfigSource config() {
        return MpConfigSources.create(Map.of("javax.sql.DataSource.ds1.dataSource.url", jdbcUrl()));
    }

    @Test
    void testGet(WebTarget target) {
        try (var rsp = target.path("/tables").request().get()) {
            assertThat(rsp.getStatus(), is(200));
            String entity = rsp.readEntity(String.class);
            assertThat(entity.lines().toList(), hasItems("USER$", "VIEW$"));
        }
    }
}

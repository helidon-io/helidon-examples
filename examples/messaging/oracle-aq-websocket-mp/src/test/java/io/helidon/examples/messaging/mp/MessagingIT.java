/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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

package io.helidon.examples.messaging.mp;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.helidon.config.mp.MpConfigSources;
import io.helidon.logging.common.LogConfig;
import io.helidon.microprofile.testing.junit5.Configuration;
import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@HelidonTest
@Configuration(useExisting = true)
class MessagingIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingIT.class);

    static {
        LogConfig.configureRuntime();
    }

    @Container
    static final GenericContainer<?> oracle =
            new GenericContainer<>(
                    new ImageFromDockerfile()
                            .withFileFromFile("init.sql", new File("../docker/oracle-aq/init.sql"))
                            .withDockerfileFromBuilder(builder -> builder
                                    .from("container-registry.oracle.com/database/express:latest")
                                    .copy("init.sql", "/opt/oracle/scripts/startup/")
                                    .expose(1521)))
                    .withExposedPorts(1521)
                    .waitingFor(Wait.forLogMessage(".*DONE: Executing user defined scripts.*", 1))
                    .withLogConsumer(new Slf4jLogConsumer(LOGGER));

    @Inject
    MsgProcessingBean msgProcessingBean;

    @BeforeAll
    static void init() {
        ConfigProviderResolver cr = ConfigProviderResolver.instance();
        String url = "jdbc:oracle:thin:@localhost:" + oracle.getMappedPort(1521) + ":XE";
        Config c = cr.getBuilder()
                .addDefaultSources()
                .addDiscoveredSources()
                .addDiscoveredConverters()
                .withSources(MpConfigSources.create(Map.of(
                        "config_ordinal", "205",
                        "server.port", "0",
                        "mp.initializer.allow", "true",
                        "javax.sql.DataSource.aq-test-ds.URL", url
                )))
                .build();
        cr.registerConfig(c, Thread.currentThread().getContextClassLoader());
    }

    @Test
    void testMessage() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> future = new CompletableFuture<>();
        msgProcessingBean.subscribeMulti().forEach(future::complete);
        msgProcessingBean.process("Test message");
        assertThat(future.get(3, TimeUnit.MINUTES), is("Test message"));
    }
}

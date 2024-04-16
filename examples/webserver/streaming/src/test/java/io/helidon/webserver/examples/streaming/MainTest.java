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

package io.helidon.webserver.examples.streaming;

import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.helidon.common.http.Http.Status;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainTest {

    private static WebServer webServer;
    private static WebClient webClient;
    private static int publicPort;

    private final String TEST_DATA_1 = "Test Data 1";
    private final String TEST_DATA_2 = "Test Data 2";

    @BeforeAll
    public static void startTheServer() {
        webServer = WebServer.builder(Main.createRouting()).build().start().await();
        webClient = WebClient.builder().build();
        publicPort = webServer.port();
    }

    @Test
    @Order(0)
    void testBadRequest() throws Exception {
        webClient.get()
                .uri("http://localhost:" + publicPort)
                .path("/download")
                .request()
                .thenAccept(response -> assertThat(response.status(), is(Status.BAD_REQUEST_400)))
                .toCompletableFuture()
                .get();
    }

    @Test
    @Order(1)
    void testUpload1() throws Exception {
        WebClientResponse response = webClient.post()
                .uri("http://localhost:" + publicPort)
                .path("/upload")
                .submit(TEST_DATA_1)
                .toCompletableFuture()
                .get();
        assertThat(response.status(), is(Status.OK_200));
    }

    @Test
    @Order(2)
    void testDownload1() throws Exception {
        WebClientResponse response = webClient.get()
                .uri("http://localhost:" + publicPort)
                .path("/download")
                .request()
                .toCompletableFuture()
                .get();
         assertThat(response.status(), is(Status.OK_200));
         assertThat(response.content().as(String.class).get(), is (TEST_DATA_1));
    }

    @Test
    @Order(3)
    void testUpload2() throws Exception {
        WebClientResponse response = webClient.post()
                .uri("http://localhost:" + publicPort)
                .path("/upload")
                .submit(TEST_DATA_2)
                .toCompletableFuture()
                .get();
        assertThat(response.status(), is(Status.OK_200));
    }

    @Test
    @Order(4)
    void testDownload2() throws Exception {
        WebClientResponse response = webClient.get()
                .uri("http://localhost:" + publicPort)
                .path("/download")
                .request()
                .toCompletableFuture()
                .get();
        assertThat(response.status(), is(Status.OK_200));
        assertThat(response.content().as(String.class).get(), is (TEST_DATA_2));
    }
}
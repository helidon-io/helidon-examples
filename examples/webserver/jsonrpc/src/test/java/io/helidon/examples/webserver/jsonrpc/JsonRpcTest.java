/*
 * Copyright (c) 2025, 2026 Oracle and/or its affiliates.
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
package io.helidon.examples.webserver.jsonrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.helidon.http.Status;
import io.helidon.jsonrpc.core.JsonRpcError;
import io.helidon.webclient.jsonrpc.JsonRpcClient;
import io.helidon.webclient.jsonrpc.JsonRpcClientBatchRequest;
import io.helidon.webclient.jsonrpc.JsonRpcClientResponse;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.jsonrpc.JsonRpcRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import org.junit.jupiter.api.Test;

import static io.helidon.examples.webserver.jsonrpc.JsonRpcMain.StartStopResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ServerTest
class JsonRpcTest {

    private final JsonRpcClient client;

    JsonRpcTest(JsonRpcClient client) {
        this.client = client;
    }

    @SetUpRoute
    static void routing(HttpRouting.Builder builder) {
        JsonRpcRouting jsonRpcRouting = JsonRpcRouting.builder()
                .service(new JsonRpcMain.MachineService())
                .build();
        builder.register("/rpc", jsonRpcRouting);
    }

    @Test
    void testStart() {
        try (var res = client.rpcMethod("start")
                .rpcId(1)
                .param("when", "NOW")
                .param("duration", "PT0S")
                .path("/rpc/machine")
                .submit()) {
            assertThat(res.status(), is(Status.OK_200));
            assertThat(rpcId(res), is(Optional.of(1)));
            assertThat(res.result().isPresent(), is(true));
            StartStopResult result = res.result().get().as(StartStopResult.class);
            assertThat(result.status(), is("RUNNING"));
        }
    }

    @Test
    void testStop() {
        try (var res = client.rpcMethod("stop")
                .rpcId(2)
                .param("when", "NOW")
                .path("/rpc/machine")
                .submit()) {
            assertThat(res.status(), is(Status.OK_200));
            assertThat(rpcId(res), is(Optional.of(2)));
            assertThat(res.result().isPresent(), is(true));
            StartStopResult result = res.result().get().as(StartStopResult.class);
            assertThat(result.status(), is("STOPPED"));
        }
    }

    @Test
    void testInvalidParams() {
        try (var res = client.rpcMethod("start")
                .rpcId(3)
                .param("when", "LATER")
                .param("duration", "PT0S")
                .path("/rpc/machine")
                .submit()) {
            assertThat(res.status(), is(Status.OK_200));
            assertThat(rpcId(res), is(Optional.of(3)));
            assertThat(res.result().isEmpty(), is(true));
            assertThat(res.error().isPresent(), is(true));
            JsonRpcError error = res.error().get();
            assertThat(error.code(), is(JsonRpcError.INVALID_PARAMS));
            assertThat(error.message(), is("Bad param"));
        }
    }

    @Test
    void testSimpleBatch() {
        JsonRpcClientBatchRequest batch = client.batch("/rpc/machine");

        batch.rpcMethod("start")
                .rpcId(1)
                .param("when", "NOW")
                .param("duration", "PT0S")
                .addToBatch()
                .rpcMethod("stop")
                .rpcId(2)
                .param("when", "NOW")
                .addToBatch();

        try (var res = batch.submit()) {
            assertThat(res.status(), is(Status.OK_200));
            assertThat(res.size(), is(2));
            Map<Integer, String> results = new HashMap<>();
            for (JsonRpcClientResponse response : res) {
                assertThat(response.result().isPresent(), is(true));
                results.put(rpcId(response).orElseThrow(), response.result().get().as(StartStopResult.class).status());
            }
            assertThat(results, is(Map.of(1, "RUNNING", 2, "STOPPED")));
        }
    }

    private static Optional<Integer> rpcId(JsonRpcClientResponse response) {
        return response.rpcId().map(value -> value.asNumber().bigDecimalValue().intValueExact());
    }
}

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
package io.helidon.examples.webserver.jsonrpc;

import java.time.Duration;

import io.helidon.jsonrpc.core.JsonRpcError;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.jsonrpc.JsonRpcHandlers;
import io.helidon.webserver.jsonrpc.JsonRpcRequest;
import io.helidon.webserver.jsonrpc.JsonRpcResponse;
import io.helidon.webserver.jsonrpc.JsonRpcRouting;
import io.helidon.webserver.jsonrpc.JsonRpcRules;
import io.helidon.webserver.jsonrpc.JsonRpcService;

/**
 * The JSON-RPC example main class.
 */
public class JsonRpcMain {

    private JsonRpcMain() {
    }

    /**
     * Entry point to application.
     *
     * @param args CLI args
     */
    public static void main(String[] args) {
        LogConfig.configureRuntime();

        // create JSON-RPC routing
        JsonRpcRouting jsonRpcRouting = JsonRpcRouting.builder()
                .service(new MachineService())
                .build();

        // set up HTTP routing using JSON-RPC routing
        WebServer.builder()
                .port(8080)
                .host("127.0.0.1")
                .routing(r -> r.register("/rpc", jsonRpcRouting))
                .build()
                .start();
    }

    static class MachineService implements JsonRpcService {

        @Override
        public void routing(JsonRpcRules rules) {
            rules.register("/machine",
                           JsonRpcHandlers.builder()
                                   .method("start", this::start)
                                   .method("stop", this::stop)
                                   .build());
        }

        void start(JsonRpcRequest req, JsonRpcResponse res) {
            StartStopParams params = req.params().as(StartStopParams.class);
            if (params.when().equals("NOW")) {
                res.result(new StartStopResult("RUNNING"));
            } else {
                res.error(JsonRpcError.INVALID_PARAMS, "Bad param");
            }
            res.send();
        }

        void stop(JsonRpcRequest req, JsonRpcResponse res) {
            StartStopParams params = req.params().as(StartStopParams.class);
            if (params.when().equals("NOW")) {
                res.result(new StartStopResult("STOPPED"));
            } else {
                res.error(JsonRpcError.INVALID_PARAMS, "Bad param");
            }
            res.send();
        }
    }

    /**
     * A record representing the start/stop params.
     *
     * @param when time to start machine
     * @param duration for how long
     */
    public record StartStopParams(String when, Duration duration) {
    }

    /**
     * A record representing the start/stop result.
     *
     * @param status status of operation
     */
    public record StartStopResult(String status) {
    }
}

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

import io.helidon.http.Status;
import io.helidon.logging.common.LogConfig;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.jsonrpc.JsonRpcHandlers;
import io.helidon.webserver.jsonrpc.JsonRpcRouting;
import io.helidon.webserver.jsonrpc.JsonRpcRules;
import io.helidon.webserver.jsonrpc.JsonRpcService;
import io.helidon.webserver.jsonrpc.JsonRpcRequest;
import io.helidon.webserver.jsonrpc.JsonRpcResponse;
import io.helidon.webserver.jsonrpc.JsonRpcError;

public class JsonRpcMain {

    private JsonRpcMain() {
    }

    public static void main(String[] args) {
        LogConfig.configureRuntime();

        // create JSON-RPC routing
        JsonRpcRouting routing = JsonRpcRouting.builder()
                .service(new JsonRpcService1())
                .build();

        // set up HTTP routing from JSON-RPC routing
        WebServer.builder()
                .port(8080)
                .host("127.0.0.1")
                .addRouting(routing.toHttpRouting())
                .build()
                .start();
    }

    static class JsonRpcService1 implements JsonRpcService {

        @Override
        public void routing(JsonRpcRules rules) {
            // register a handler for each method on same path
            rules.register("/jsonrpc",
                           JsonRpcHandlers.builder()
                                   .method("start", this::start)
                                   .method("stop", this::stop)
                                   .build());
        }

        void start(JsonRpcRequest req, JsonRpcResponse res) throws Exception {
            StartStopParams params = req.params().as(StartStopParams.class);
            if (params.when().equals("NOW")) {
                res.id(req.id().orElseThrow());
                res.result(new StartStopResult("RUNNING"));
                res.status(Status.OK_200).send();
            } else {
                res.error(JsonRpcError.builder()
                                  .code(-32600)
                                  .data(new ErrorData("Bad param"))
                                  .build());
                res.status(Status.OK_200).send();
            }
        }

        void stop(JsonRpcRequest req, JsonRpcResponse res) throws Exception {
            StartStopParams params = req.params().as(StartStopParams.class);
            if (params.when().equals("NOW")) {
                res.id(req.id().orElseThrow());
                res.result(new StartStopResult("STOPPED"));
                res.status(Status.OK_200).send();
            } else {
                res.error(JsonRpcError.builder()
                                  .code(-32600)
                                  .data(new ErrorData("Bad param"))
                                  .build());
                res.status(Status.OK_200).send();
            }
        }

        public record StartStopParams(String when, Duration duration) {
        }

        public record StartStopResult(String status) {
        }

        public record ErrorData(String reason) {
        }
    }
}

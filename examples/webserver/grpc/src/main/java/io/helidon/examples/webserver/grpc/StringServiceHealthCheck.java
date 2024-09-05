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
package io.helidon.examples.webserver.grpc;

import io.helidon.common.configurable.Resource;
import io.helidon.common.tls.Tls;
import io.helidon.config.Config;
import io.helidon.health.HealthCheck;
import io.helidon.health.HealthCheckResponse;
import io.helidon.health.HealthCheckType;
import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.grpc.GrpcClient;

class StringServiceHealthCheck implements HealthCheck {

    private final WebClient webClient;

    StringServiceHealthCheck(Config config) {
        Tls clientTls = Tls.builder()
                .trust(trust -> trust
                        .keystore(store -> store
                                .passphrase("password")
                                .trustStore(true)
                                .keystore(Resource.create("client.p12"))))
                .build();
        int serverPort = config.get("port").asInt().get();
        this.webClient = WebClient.builder()
                .tls(clientTls)
                .baseUri("https://localhost:" + serverPort)
                .build();
    }

    @Override
    public String name() {
        return StringService.class.getSimpleName();
    }

    @Override
    public HealthCheckType type() {
        return HealthCheckType.READINESS;
    }

    /**
     * Self-invocation to verify gRPC endpoint is available and ready.
     *
     * @return health check response
     */
    @Override
    public HealthCheckResponse call() {
        try {
            GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
            StringServiceGrpc.StringServiceBlockingStub service =
                    StringServiceGrpc.newBlockingStub(grpcClient.channel());
            Strings.StringMessage res = service.upper(
                    Strings.StringMessage.newBuilder().setText("hello").build());
            return HealthCheckResponse.builder()
                    .status(res.getText().equals("HELLO"))
                    .get();
        } catch (Exception e) {
            return HealthCheckResponse.builder()
                    .status(false)
                    .get();
        }
    }
}

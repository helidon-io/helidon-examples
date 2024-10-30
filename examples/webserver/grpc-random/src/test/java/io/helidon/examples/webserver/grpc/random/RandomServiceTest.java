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

package io.helidon.examples.webserver.grpc.random;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.helidon.common.configurable.Resource;
import io.helidon.common.tls.Tls;
import io.helidon.config.Config;
import io.helidon.examples.webserver.grpc.random.Random.ParamMessage;
import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.grpc.GrpcClient;
import io.helidon.webclient.grpc.GrpcClientProtocolConfig;
import io.helidon.webserver.Router;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.grpc.GrpcRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;
import io.helidon.examples.webserver.grpc.random.Random.ParamMessage;
import io.helidon.examples.webserver.grpc.random.Random.RandomMessage;
import io.helidon.examples.webserver.grpc.random.RandomServiceGrpc;
import io.helidon.examples.webserver.grpc.random.RandomServiceGrpc.RandomServiceBlockingStub;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

/**
 * Tests gRPC Strings service using {@link io.helidon.webclient.api.WebClient}.
 */
@ServerTest
class RandomServiceTest {
    private static final long TIMEOUT_SECONDS = 10;
    private static final int BOUND = 100;
    private static final int COUNT = 10;

    private final WebClient webClient;

    private RandomServiceTest(WebServer server) {
        Tls clientTls = Tls.builder()
                .trust(trust -> trust
                        .keystore(store -> store
                                .passphrase("password")
                                .trustStore(true)
                                .keystore(Resource.create("client.p12"))))
                .build();
        Config config = Config.create();
        GrpcClientProtocolConfig protocolConfig = GrpcClientProtocolConfig.builder()
                .config(config.get("grpc-protocol-config"))
                .build();
        this.webClient = WebClient.builder()
                .tls(clientTls)
                .baseUri("https://localhost:" + server.port())
                .addProtocolConfig(protocolConfig)
                .build();
    }

    @SetUpRoute
    static void routing(Router.RouterBuilder<?> router) {
        router.addRouting(GrpcRouting.builder().service(new RandomService()));
    }

    @Test
    void testRandomSingle() {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        RandomServiceBlockingStub service = RandomServiceGrpc.newBlockingStub(grpcClient.channel());
        RandomMessage res = service.randomSingle(newParamMessage(BOUND));
        assertThat(res.getNumber(), is(lessThan(BOUND)));
    }

    @Test
    void testRandomMany() throws InterruptedException, TimeoutException, ExecutionException {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        RandomServiceGrpc.RandomServiceStub service = RandomServiceGrpc.newStub(grpcClient.channel());
        CompletableFuture<Iterator<RandomMessage>> future = new CompletableFuture<>();
        StreamObserver<ParamMessage> req = service.randomMany(multiStreamObserver(future));
        req.onNext(newParamMessage(BOUND));
        req.onNext(newParamMessage(COUNT));
        req.onCompleted();
        Iterator<RandomMessage> res = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        int n = 0;
        for (; res.hasNext(); n++) {
            assertThat(res.next().getNumber(), is(lessThan(BOUND)));
        }
        assertThat(n, is(COUNT));
    }

    static ParamMessage newParamMessage(int n) {
        return ParamMessage.newBuilder().setNumber(n).build();
    }

    static <ResT> StreamObserver<ResT> multiStreamObserver(CompletableFuture<Iterator<ResT>> future) {
        return new StreamObserver<>() {
            private final List<ResT> value = new ArrayList<>();

            @Override
            public void onNext(ResT value) {
                this.value.add(value);
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(value.iterator());
            }
        };
    }
}

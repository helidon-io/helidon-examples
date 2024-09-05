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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.helidon.common.configurable.Resource;
import io.helidon.common.tls.Tls;
import io.helidon.http.Status;
import io.helidon.webclient.api.HttpClientResponse;
import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.grpc.GrpcClient;
import io.helidon.webserver.Router;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.grpc.GrpcRouting;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.webserver.testing.junit5.SetUpRoute;

import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests gRPC Strings service using {@link io.helidon.webclient.api.WebClient}.
 */
@ServerTest
class StringServiceTest {
    private static final long TIMEOUT_SECONDS = 10;

    private final WebClient webClient;

    private StringServiceTest(WebServer server) {
        Tls clientTls = Tls.builder()
                .trust(trust -> trust
                        .keystore(store -> store
                                .passphrase("password")
                                .trustStore(true)
                                .keystore(Resource.create("client.p12"))))
                .build();
        this.webClient = WebClient.builder()
                .tls(clientTls)
                .baseUri("https://localhost:" + server.port())
                .build();
    }

    @SetUpRoute
    static void routing(Router.RouterBuilder<?> router) {
        router.addRouting(GrpcRouting.builder().service(new StringService()));
    }

    @Test
    void testUnaryUpper() {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        StringServiceGrpc.StringServiceBlockingStub service = StringServiceGrpc.newBlockingStub(grpcClient.channel());
        Strings.StringMessage res = service.upper(newStringMessage("hello"));
        assertThat(res.getText(), is("HELLO"));
    }

    @Test
    void testUnaryLower() {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        StringServiceGrpc.StringServiceBlockingStub service = StringServiceGrpc.newBlockingStub(grpcClient.channel());
        Strings.StringMessage res = service.lower(newStringMessage("HELLO"));
        assertThat(res.getText(), is("hello"));
    }

    @Test
    void testServerStreamingSplit() {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        StringServiceGrpc.StringServiceBlockingStub service = StringServiceGrpc.newBlockingStub(grpcClient.channel());
        Iterator<Strings.StringMessage> res = service.split(newStringMessage("hello world"));
        assertThat(res.next().getText(), is("hello"));
        assertThat(res.next().getText(), is("world"));
        assertThat(res.hasNext(), is(false));
    }

    @Test
    void testClientStreamingJoin() throws ExecutionException, InterruptedException, TimeoutException {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        StringServiceGrpc.StringServiceStub service = StringServiceGrpc.newStub(grpcClient.channel());
        CompletableFuture<Strings.StringMessage> future = new CompletableFuture<>();
        StreamObserver<Strings.StringMessage> req = service.join(singleStreamObserver(future));
        req.onNext(newStringMessage("hello"));
        req.onNext(newStringMessage("world"));
        req.onCompleted();
        Strings.StringMessage res = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(res.getText(), is("hello world"));
    }

    @Test
    void testBidirectionalEcho() throws ExecutionException, InterruptedException, TimeoutException {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        StringServiceGrpc.StringServiceStub service = StringServiceGrpc.newStub(grpcClient.channel());
        CompletableFuture<Iterator<Strings.StringMessage>> future = new CompletableFuture<>();
        StreamObserver<Strings.StringMessage> req = service.echo(multiStreamObserver(future));
        req.onNext(newStringMessage("hello"));
        req.onNext(newStringMessage("world"));
        req.onCompleted();
        Iterator<Strings.StringMessage> res = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(res.next().getText(), is("hello"));
        assertThat(res.next().getText(), is("world"));
        assertThat(res.hasNext(), is(false));
    }

    @Test
    void testUnaryUpperInterceptor() {
        GrpcClient grpcClient = webClient.client(GrpcClient.PROTOCOL);
        Channel channel = grpcClient.channel(new StringServiceInterceptor());
        StringServiceGrpc.StringServiceBlockingStub service = StringServiceGrpc.newBlockingStub(channel);
        Strings.StringMessage res = service.upper(newStringMessage("hello"));
        assertThat(res.getText(), is("[[HELLO]]"));
    }

    /**
     * Tests server health using HTTP, not gRPC.
     */
    @Test
    void testHealthHttp() {
        try (HttpClientResponse res = webClient.get("/observe/health").request()) {
            assertThat(res.status(), is(Status.OK_200));
            String value = res.as(String.class);
            assertThat(value, containsString("UP"));
            assertThat(value, not(containsString("DOWN")));
        }
    }

    static Strings.StringMessage newStringMessage(String data) {
        return Strings.StringMessage.newBuilder().setText(data).build();
    }

    static <ReqT> StreamObserver<ReqT> singleStreamObserver(CompletableFuture<ReqT> future) {
        return new StreamObserver<>() {
            private ReqT value;

            @Override
            public void onNext(ReqT value) {
                this.value = value;
            }

            @Override
            public void onError(Throwable t) {
                future.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                future.complete(value);
            }
        };
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

/*
 * Copyright (c) 2024, 2025 Oracle and/or its affiliates.
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
package io.helidon.examples.microprofile.grpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import io.helidon.grpc.api.Grpc;
import io.helidon.microprofile.grpc.client.GrpcConfigurablePort;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import io.helidon.examples.microprofile.grpc.Strings.StringMessage;

import io.grpc.stub.StreamObserver;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@HelidonTest
class StringServiceTest {

    @Inject
    private WebTarget webTarget;

    @Inject
    @Grpc.GrpcProxy
    private StringServiceClient client;

    @BeforeEach
    void updatePort() {
        if (client instanceof GrpcConfigurablePort c) {
            c.channelPort(webTarget.getUri().getPort());
        }
    }

    @Test
    void testUnaryUpper() {
        StringMessage res = client.upper(newStringMessage("hello"));
        assertThat(res.getText(), is("HELLO"));
    }

    @Test
    void testUnaryLower() {
        StringMessage res = client.lower(newStringMessage("HELLO"));
        assertThat(res.getText(), is("hello"));
    }

    @Test
    void testServerStreamingSplit() {
        Stream<StringMessage> stream = client.split(newStringMessage("hello world"));
        List<StringMessage> value = stream.toList();
        assertThat(value, hasSize(2));
        assertThat(value, contains(newStringMessage("hello"), newStringMessage("world")));
    }

    @Test
    void testClientStreamingJoin() throws InterruptedException {
        ListObserver<StringMessage> response = new ListObserver<>();
        StreamObserver<StringMessage> request = client.join(response);
        request.onNext(newStringMessage("hello"));
        request.onNext(newStringMessage("world"));
        request.onCompleted();
        List<StringMessage> value = response.value();
        assertThat(value.getFirst(), is(newStringMessage("hello world")));
    }

    /**
     * Helper method to create a string message from a string.
     *
     * @param data the string
     * @return the string message
     */
    StringMessage newStringMessage(String data) {
        return StringMessage.newBuilder().setText(data).build();
    }

    /**
     * Helper class to collect a list of observed values.
     *
     * @param <T> the type of values
     */
    static class ListObserver<T> implements StreamObserver<T> {
        private static final long TIMEOUT_SECONDS = 10;

        private List<T> value = new ArrayList<>();
        private final CountDownLatch latch = new CountDownLatch(1);

        public List<T> value() throws InterruptedException {
            boolean b = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assert b;
            return value;
        }

        @Override
        public void onNext(T value) {
            this.value.add(value);
        }

        @Override
        public void onError(Throwable t) {
            value = null;
        }

        @Override
        public void onCompleted() {
            latch.countDown();
        }
    }
}


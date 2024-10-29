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

import java.util.Random;

import io.helidon.examples.webserver.grpc.random.Random.ParamMessage;
import io.helidon.examples.webserver.grpc.random.Random.RandomMessage;
import io.helidon.webserver.grpc.GrpcService;

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;

class RandomService implements GrpcService {

    private final Random random = new Random();

    @Override
    public Descriptors.FileDescriptor proto() {
        return io.helidon.examples.webserver.grpc.random.Random.getDescriptor();
    }

    @Override
    public void update(Routing router) {
        router.unary("RandomSingle", this::randomSingle)
                .bidi("RandomMany", this::randomMany);
    }

    private void randomSingle(ParamMessage request, StreamObserver<RandomMessage> response) {
        int bound = request.getNumber();
        response.onNext(newRandomMessage(random.nextInt(bound)));
        response.onCompleted();
    }

    private StreamObserver<ParamMessage> randomMany(StreamObserver<RandomMessage> response) {
        return new StreamObserver<>() {

            private int bound = Integer.MIN_VALUE;
            private int count = Integer.MIN_VALUE;

            @Override
            public void onNext(ParamMessage paramMessage) {
                // collect bound and count, in that order
                if (bound == Integer.MIN_VALUE) {
                    bound = paramMessage.getNumber();
                } else if (count == Integer.MIN_VALUE) {
                    count = paramMessage.getNumber();
                } else {
                    onError(new IllegalStateException("Received extra input params"));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                response.onError(throwable);
            }

            @Override
            public void onCompleted() {
                // verify input params received
                if (bound == Integer.MIN_VALUE || count == Integer.MIN_VALUE) {
                    onError(new IllegalStateException("Did not receive all input params"));
                }

                // send stream of random numbers
                for (int i = 0; i < count; i++) {
                    response.onNext(newRandomMessage(random.nextInt(bound)));
                }
                response.onCompleted();
            }
        };
    }

    private static RandomMessage newRandomMessage(int random) {
        return RandomMessage.newBuilder().setNumber(random).build();
    }
}

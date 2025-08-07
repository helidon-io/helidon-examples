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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.helidon.examples.microprofile.grpc.Strings.StringMessage;
import io.helidon.grpc.api.Grpc;
import io.helidon.grpc.core.CollectingObserver;

import com.google.protobuf.Descriptors;
import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * An implementation of a string service.
 */
@Grpc.GrpcService
@ApplicationScoped
public class StringService {

    /**
     * Make the proto available to Helidon for gRPC reflection.
     *
     * @return the proto file descriptor
     */
    @Grpc.Proto
    public Descriptors.FileDescriptor proto() {
        return Strings.getDescriptor();
    }

    /**
     * Uppercase a string.
     *
     * @param request string message
     * @return string message
     */
    @Grpc.Unary("Upper")
    public StringMessage upper(StringMessage request) {
        return newStringMessage(request.getText().toUpperCase());
    }

    /**
     * Lowercase a string.
     *
     * @param request string message
     * @return string message
     */
    @Grpc.Unary("Lower")
    public StringMessage lower(StringMessage request) {
        return newStringMessage(request.getText().toLowerCase());
    }

    /**
     * Split a string using space delimiters.
     *
     * @param request string message
     * @return stream of string messages
     */
    @Grpc.ServerStreaming("Split")
    public Stream<StringMessage> split(StringMessage request) {
        String[] parts = request.getText().split(" ");
        return Stream.of(parts).map(this::newStringMessage);
    }

    /**
     * Join a stream of messages using spaces.
     *
     * @param observer stream of messages
     * @return single message as a stream
     */
    @Grpc.ClientStreaming("Join")
    public StreamObserver<StringMessage> join(StreamObserver<StringMessage> observer) {
        return CollectingObserver.create(
                Collectors.joining(" "),
                observer,
                StringMessage::getText,
                this::newStringMessage);
    }

    private StringMessage newStringMessage(String text) {
        return StringMessage.newBuilder().setText(text).build();
    }
}


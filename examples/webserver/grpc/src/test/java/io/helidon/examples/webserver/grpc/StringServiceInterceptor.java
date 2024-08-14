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

import javax.annotation.Nullable;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

/**
 * Intercepts a client call and wraps outgoing messages in double square brackets.
 */
class StringServiceInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, ResT> ClientCall<ReqT, ResT> interceptCall(MethodDescriptor<ReqT, ResT> method,
                                                             CallOptions callOptions,
                                                             Channel next) {
        ClientCall<ReqT, ResT> delegate = next.newCall(method, callOptions);
        return new ClientCall<>() {
            @Override
            public void start(Listener<ResT> responseListener, Metadata headers) {
                delegate.start(responseListener, headers);
            }

            @Override
            public void request(int numMessages) {
                delegate.request(numMessages);
            }

            @Override
            public void cancel(@Nullable String message, @Nullable Throwable cause) {
                delegate.cancel(message, cause);
            }

            @Override
            public void halfClose() {
                delegate.halfClose();
            }

            @Override
            @SuppressWarnings("unchecked")
            public void sendMessage(ReqT message) {
                Strings.StringMessage msg = (Strings.StringMessage) message;
                delegate.sendMessage((ReqT) Strings.StringMessage.newBuilder()
                        .setText("[[" + msg.getText() + "]]")
                        .build());
            }
        };
    }
}
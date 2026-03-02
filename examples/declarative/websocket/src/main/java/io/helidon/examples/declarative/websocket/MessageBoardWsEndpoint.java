/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
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

package io.helidon.examples.declarative.websocket;

import java.util.Locale;

import io.helidon.http.Http;
import io.helidon.service.registry.Service;
import io.helidon.webserver.websocket.WebSocketServer;
import io.helidon.websocket.WebSocket;
import io.helidon.websocket.WsSession;

@WebSocketServer.Endpoint
@Http.Path("/websocket")
@Service.Singleton
class MessageBoardWsEndpoint {
    private static final System.Logger LOGGER = System.getLogger(MessageBoardWsEndpoint.class.getName());

    private final MessageQueue queue;

    @Service.Inject
    MessageBoardWsEndpoint(MessageQueue queue) {
        this.queue = queue;
    }

    @WebSocket.OnOpen
    void onOpen() {
        LOGGER.log(System.Logger.Level.INFO, "OnOpen called");
    }

    @WebSocket.OnMessage
    void onMessage(WsSession session, String message) {
        LOGGER.log(System.Logger.Level.INFO, "OnMessage called '" + message + "'");

        // Send all messages in the queue
        if (message.equals("SEND")) {
            while (!queue.isEmpty()) {
                String toSend = queue.pop().toUpperCase(Locale.ROOT);
                LOGGER.log(System.Logger.Level.INFO, "Sending '" + toSend + "'");
                session.send(toSend, true);
            }
        }
    }

    @WebSocket.OnClose
    void onClose() {
        LOGGER.log(System.Logger.Level.INFO, "OnClose called");
    }

    @WebSocket.OnError
    void onError(Throwable t) {
        LOGGER.log(System.Logger.Level.INFO, "OnError called", t);
    }
}

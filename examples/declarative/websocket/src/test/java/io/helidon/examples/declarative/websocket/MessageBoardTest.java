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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.helidon.http.Status;
import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.http1.Http1Client;
import io.helidon.webclient.websocket.WsClient;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.testing.junit5.ServerTest;
import io.helidon.websocket.WsCloseCodes;
import io.helidon.websocket.WsListener;
import io.helidon.websocket.WsSession;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Class MessageBoardTest.
 */
@ServerTest
class MessageBoardTest {
    private static final String[] MESSAGES = {"Whisky", "Tango", "Foxtrot"};

    private final Http1Client client;
    private final WsClient wsClient;

    MessageBoardTest(WebClient client, WebServer server) {
        this.client = client.client(Http1Client.PROTOCOL);
        this.wsClient = client.client(WsClient.PROTOCOL);
    }

    @Test
    public void testBoard() throws InterruptedException {
        // Post messages using REST resource
        for (String message : MESSAGES) {
            try (var res = client.post("/rest")
                    .submit(message)) {
                assertThat(res.status(), is(Status.NO_CONTENT_204));
            }
        }

        // Now connect to message board using WS and them back

        CountDownLatch messageLatch = new CountDownLatch(MESSAGES.length);

        wsClient.connect("/websocket", new WsListener() {
            @Override
            public void onOpen(WsSession session) {
                // Send an initial message to start receiving
                session.send("SEND", true);
            }

            @Override
            public void onMessage(WsSession session, String text, boolean last) {
                messageLatch.countDown();
                if (messageLatch.getCount() == 0) {
                    session.close(WsCloseCodes.NORMAL_CLOSE, "finished");
                }
            }
        });

        // Wait until all messages are received
        assertThat("Message latch should have counted down to 0",
                   messageLatch.await(1000, TimeUnit.SECONDS),
                   is(true));
    }
}

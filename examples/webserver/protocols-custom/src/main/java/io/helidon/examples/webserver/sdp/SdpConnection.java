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

package io.helidon.examples.webserver.sdp;

import java.time.Duration;
import java.util.Optional;

import io.helidon.common.buffers.BufferData;
import io.helidon.common.buffers.DataReader;
import io.helidon.common.buffers.DataWriter;
import io.helidon.common.concurrency.limits.Limit;
import io.helidon.common.concurrency.limits.LimitAlgorithm;
import io.helidon.http.DirectHandler;
import io.helidon.http.RequestException;
import io.helidon.http.Status;
import io.helidon.webserver.ConnectionContext;
import io.helidon.webserver.spi.ServerConnection;

/**
 * A ServerConnection that implements the Simple Demo Protocol (SDP) protocol.
 */
public class SdpConnection implements ServerConnection {

    private final ConnectionContext ctx;
    private final DataWriter writer;
    private final DataReader reader;

    private volatile Thread myThread;
    private volatile boolean isRunning = true;
    private long lastReadTimeMs = 0;

    /**
     * Create a connection class that handles the SDP protocol.
     *
     * @param connectionContext  Information about the connection
     */
    public SdpConnection(ConnectionContext connectionContext) {
        ctx = connectionContext;
        reader = ctx.dataReader();
        writer = ctx.dataWriter();
    }

    @Override
    public Duration idleTime() {
        // How long our connection has been idle
        return Duration.ofMillis(System.currentTimeMillis() - lastReadTimeMs);
    }

    @Override
    public void close(boolean interrupt) {
        isRunning = false;
        if (interrupt) {
            if (myThread != null) {
                myThread.interrupt();
            }
        }
    }

    @Override
    public void handle(Limit limit) throws InterruptedException {

        // Limit is used to limit concurrency.
        Optional<LimitAlgorithm.Token> token = limit.tryAcquire();
        if (token.isEmpty()) {
            throw RequestException.builder()
                    .setKeepAlive(false)
                    .status(Status.SERVICE_UNAVAILABLE_503)
                    .type(DirectHandler.EventType.OTHER)
                    .message("Too Many Concurrent Requests")
                    .build();
        }

        LimitAlgorithm.Token permit = token.get();
        myThread = Thread.currentThread();
        lastReadTimeMs = System.currentTimeMillis();

        while (isRunning) {
            String cmd = reader.readLine();
            lastReadTimeMs = System.currentTimeMillis();
            switch (cmd) {
                case "sdp":
                case "":
                    break;
                case "help":
                    writer.writeNow(BufferData.create(
                            """
                            gc               Run GC
                            getProperties    Display all system properties
                            .                Exit
                            """));
                    break;
                case "gc":
                    System.gc();
                    writer.writeNow(BufferData.create("I ran GC for you.\n"));
                    break;
                case "getProperties":
                    System.getProperties().forEach(
                            (k, v) -> writer.writeNow(BufferData.create(formatKeyValue(k.toString(), v.toString())))
                    );
                    break;
                case ".":
                    isRunning = false;
                    break;
                default:
                    writer.writeNow(BufferData.create("I do not understand command: '" + cmd + "'  Type 'help' for help\n"));
            }
            if (isRunning) {
                writer.writeNow(BufferData.create("> "));
            }
        }

        // Release concurrency limit permit
        permit.success();
    }

    private String formatKeyValue(String key, String value) {
        return key + ": " + value + "\n";
    }
}

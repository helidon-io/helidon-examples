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

import java.lang.System.Logger.Level;
import java.util.Set;

import io.helidon.common.buffers.BufferData;
import io.helidon.common.buffers.Bytes;
import io.helidon.webserver.ConnectionContext;
import io.helidon.webserver.spi.ServerConnection;
import io.helidon.webserver.spi.ServerConnectionSelector;

/**
 * Sniffs the incoming bytes to see if it is the SDP protocol and allocates a connection.
 */
public class SdpConnectionSelector implements ServerConnectionSelector {

    private static final System.Logger LOGGER = System.getLogger(SdpConnectionSelector.class.getName());

    @Override
    public int bytesToIdentifyConnection() {
        return 4;
    }

    @Override
    public Support supports(BufferData bufferData) {
        if (LOGGER.isLoggable(Level.DEBUG)) {
            LOGGER.log(Level.DEBUG, "SDP: supports() called. bufferData=\n" + bufferData.debugDataHex());
        }
        /* Must start with "sdp\n" */
        if (bufferData.read() == 's'
                && bufferData.read() == 'd'
                && bufferData.read() == 'p'
                && eol(bufferData.read())) {
            return Support.SUPPORTED;
        } else {
            return Support.UNSUPPORTED;
        }
    }

    private boolean eol(int c) {
        return (c == Bytes.CR_BYTE || c == Bytes.LF_BYTE);
    }

    @Override
    public Set<String> supportedApplicationProtocols() {
        return Set.of("sdp");
    }

    @Override
    public ServerConnection connection(ConnectionContext connectionContext) {
        LOGGER.log(Level.DEBUG, "SDP: creating new connection");
        return new SdpConnection(connectionContext);
    }
}

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
        LOGGER.log(Level.DEBUG, "SDP: supports called. bufferData=\n" + bufferData.debugDataHex());
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

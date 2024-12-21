package tech.smartboot.feat.client.impl;

import org.smartboot.socket.transport.AioSession;

public class WebSocketRequestImpl extends AbstractRequest {
    public WebSocketRequestImpl(AioSession session) {
        init(new WebSocketOutputStream(this, session));
    }
}

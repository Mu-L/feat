package tech.smartboot.feat.core.client;

import java.io.IOException;

public interface BodyStreaming {
    public static final BodyStreaming SKIP_BODY_STREAMING = (response, bytes, end) -> {

    };

    void stream(HttpResponse response, byte[] bytes, boolean end) throws IOException;

}

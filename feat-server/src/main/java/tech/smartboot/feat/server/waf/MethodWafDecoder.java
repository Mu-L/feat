package tech.smartboot.feat.server.waf;

import tech.smartboot.feat.common.enums.HttpStatus;
import tech.smartboot.feat.server.HttpServerConfiguration;
import tech.smartboot.feat.server.decode.Decoder;
import tech.smartboot.feat.server.impl.Request;

import java.nio.ByteBuffer;

public class MethodWafDecoder extends AbstractWafDecoder {


    public MethodWafDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
        WafConfiguration wafConfiguration = getConfiguration().getWafConfiguration();
        if (!wafConfiguration.getAllowMethods().isEmpty() && !wafConfiguration.getAllowMethods().contains(request.getMethod())) {
            throw new WafException(HttpStatus.METHOD_NOT_ALLOWED, WafConfiguration.DESC);
        }
        if (!wafConfiguration.getDenyMethods().isEmpty() && wafConfiguration.getDenyMethods().contains(request.getMethod())) {
            throw new WafException(HttpStatus.METHOD_NOT_ALLOWED, WafConfiguration.DESC);
        }
        return null;
    }
}

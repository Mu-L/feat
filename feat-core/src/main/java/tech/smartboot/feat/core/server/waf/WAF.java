/*
 *  Copyright (C) [2024] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.feat.core.server.waf;

import tech.smartboot.feat.core.common.enums.HttpStatus;
import tech.smartboot.feat.core.common.utils.CollectionUtils;
import tech.smartboot.feat.core.server.ServerOptions;
import tech.smartboot.feat.core.server.impl.HttpEndpoint;

public class WAF {
    public static void methodCheck(ServerOptions options, HttpEndpoint request) {
        WafOptions wafOptions = options.getWafOptions();
        if (!wafOptions.isEnable()) {
            return;
        }
        if (!wafOptions.getAllowMethods().isEmpty() && !wafOptions.getAllowMethods().contains(request.getMethod())) {
            throw new WafException(HttpStatus.METHOD_NOT_ALLOWED, WafOptions.DESC);
        }
        if (!wafOptions.getDenyMethods().isEmpty() && wafOptions.getDenyMethods().contains(request.getMethod())) {
            throw new WafException(HttpStatus.METHOD_NOT_ALLOWED, WafOptions.DESC);
        }
    }

    public static void checkUri(ServerOptions options, HttpEndpoint request) {
        WafOptions wafOptions = options.getWafOptions();
        if (!wafOptions.isEnable()) {
            return;
        }
        if (request.getUri().equals("/") || CollectionUtils.isEmpty(wafOptions.getAllowUriPrefixes()) && CollectionUtils.isEmpty(wafOptions.getAllowUriSuffixes())) {
            return;
        }
        for (String prefix : wafOptions.getAllowUriPrefixes()) {
            if (request.getUri().startsWith(prefix)) {
                return;
            }
        }
        for (String suffix : wafOptions.getAllowUriSuffixes()) {
            if (request.getUri().endsWith(suffix)) {
                return;
            }
        }
        throw new WafException(HttpStatus.BAD_REQUEST, WafOptions.DESC);
    }
}

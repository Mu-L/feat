/*
 *  Copyright (C) [2024] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.feat.core.common;

/**
 * @author 三刀 zhengjunweimail@163.com
 * @version v1.0.0
 */
public class HttpProtocol {
    public static final HttpProtocol HTTP_11 = new HttpProtocol("HTTP/1.1");
    public static final HttpProtocol HTTP_10 = new HttpProtocol("HTTP/1.0");
    public static final HttpProtocol HTTP_2 = new HttpProtocol("HTTP/2.0");

    private final String protocol;

    HttpProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }
}

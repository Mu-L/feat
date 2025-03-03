/*
 *  Copyright (C) [2024] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.feat.core.server;

import tech.smartboot.feat.core.server.impl.HttpEndpoint;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface HttpHandler {
    default void onHeaderComplete(HttpEndpoint request) throws IOException {
    }

    default void handle(HttpRequest request, CompletableFuture<Object> completableFuture) throws Throwable {
        try {
            handle(request);
        } finally {
            completableFuture.complete(null);
        }
    }

    void handle(HttpRequest request) throws Throwable;

    /**
     * 断开 TCP 连接
     */
    default void onClose(HttpEndpoint request) {
    }
}

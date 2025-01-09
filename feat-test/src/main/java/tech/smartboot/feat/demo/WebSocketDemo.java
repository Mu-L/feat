/*******************************************************************************
 * Copyright (c) 2024, tech.smartboot. All rights reserved.
 * project name: feat
 * file name: WebSocketDemo.java
 * Date: 2021-06-20
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package tech.smartboot.feat.demo;

import tech.smartboot.feat.core.common.codec.websocket.CloseReason;
import tech.smartboot.feat.core.server.HttpRequest;
import tech.smartboot.feat.core.server.HttpResponse;
import tech.smartboot.feat.core.server.HttpServer;
import tech.smartboot.feat.core.server.HttpServerHandler;
import tech.smartboot.feat.core.server.WebSocketRequest;
import tech.smartboot.feat.core.server.WebSocketResponse;
import tech.smartboot.feat.core.server.handler.HttpRouteHandler;
import tech.smartboot.feat.core.server.upgrade.WebSocketUpgradeHandler;

/**
 * @author 三刀
 * @version V1.0 , 2020/4/1
 */
public class WebSocketDemo {
    public static void main(String[] args) {
        //1. 实例化路由Handle
        HttpRouteHandler routeHandle = new HttpRouteHandler();

        //2. 指定路由规则以及请求的处理实现
        routeHandle.route("/", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws Throwable {
                request.upgrade(new WebSocketUpgradeHandler() {
                    @Override
                    public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
                        response.ping("hello".getBytes());
                        response.sendTextMessage("接受到客户端消息：" + data);
                    }

                    @Override
                    public void onClose(WebSocketRequest request, WebSocketResponse response, CloseReason closeReason) {
                        System.out.println("客户端关闭连接，状态码：" + closeReason.getCode());
                        System.out.println("客户端关闭连接，原因：" + closeReason.getReason());
                        super.onClose(request, response, closeReason);
                    }
                });
            }
        });

        // 3. 启动服务
        HttpServer bootstrap = new HttpServer();
        bootstrap.configuration().setWsIdleTimeout(5000).debug(true);
        bootstrap.httpHandler(routeHandle);
        bootstrap.start();
    }
}

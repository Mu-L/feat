/*******************************************************************************
 * Copyright (c) 2024, tech.smartboot. All rights reserved.
 * project name: feat
 * file name: HttpRouteDemo.java
 * Date: 2021-06-20
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package tech.smartboot.feat.demo;

import tech.smartboot.feat.core.server.HttpServer;
import tech.smartboot.feat.router.Chain;
import tech.smartboot.feat.router.Context;
import tech.smartboot.feat.router.Interceptor;
import tech.smartboot.feat.router.Router;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 请求路由示例
 *
 * @author 三刀
 * @version V1.0 , 2020/4/1
 */
public class HttpRouteDemo {
    public static void main(String[] args) {
        //1. 实例化路由Handle
        Router routeHandle = new Router();

        //2. 指定路由规则以及请求的处理实现
        routeHandle.route("/", ctx -> ctx.Response.write("feat".getBytes()))
//                .route("/h2", new BaseHttpHandler() {
//                    @Override
//                    public void handle(HttpRequest request) throws IOException {
//                        request.upgrade(new Http2UpgradeHandler() {
//                            @Override
//                            public void handle(HttpRequest request, HttpResponse response) throws Throwable {
//                                response.write("feat h2".getBytes());
//                            }
//                        });
//                    }
//                })
//                .route("/test1", new BaseHttpHandler() {
//                    @Override
//                    public void handle(HttpRequest request) throws IOException {
//                        request.getResponse().write(("test1").getBytes());
//                    }
//                })
//                .route("/test2", new BaseHttpHandler() {
//                    @Override
//                    public void handle(HttpRequest request) throws IOException {
//                        request.getResponse().write(("test2").getBytes());
//                    }
//                })
//                .route("/ws", new BaseHttpHandler() {
//                    @Override
//                    public void handle(HttpRequest request) throws IOException {
//                        request.upgrade(new WebSocketUpgradeHandler() {
//
//                        });
//                    }
//                })
//                .route("/a/test1", new BaseHttpHandler() {
//                    @Override
//                    public void handle(HttpRequest request) throws IOException {
//                        request.getResponse().write(("test1").getBytes());
//                    }
//                })
                .route("/b/c/test1", ctx -> ctx.Response.write(("/b/c/test1").getBytes()))
                .route("/b/c/test2", ctx -> ctx.Response.write(("/b/c/test2").getBytes()));
        routeHandle.addInterceptor(new Interceptor() {
            @Override
            public List<String> pathPatterns() {
                return Arrays.asList("/b/*");
            }

            @Override
            public void intercept(Context context, CompletableFuture<Object> completableFuture, Chain chain) throws Throwable {
                System.out.println("intercept:" + context.Request.getRequestURI());
                chain.proceed(context, completableFuture);
            }
        });

        // 3. 启动服务
        HttpServer bootstrap = new HttpServer();
        bootstrap.options().debug(true);
        bootstrap.httpHandler(routeHandle);
        bootstrap.listen();
    }
}

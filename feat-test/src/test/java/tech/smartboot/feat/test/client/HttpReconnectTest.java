/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRestTest.java
 * Date: 2021-06-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package tech.smartboot.feat.test.client;

import com.alibaba.fastjson.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tech.smartboot.feat.client.HttpClient;
import tech.smartboot.feat.common.enums.HeaderNameEnum;
import tech.smartboot.feat.common.enums.HeaderValueEnum;
import tech.smartboot.feat.server.HttpBootstrap;
import tech.smartboot.feat.server.HttpRequest;
import tech.smartboot.feat.server.HttpResponse;
import tech.smartboot.feat.server.HttpServerHandler;
import tech.smartboot.feat.server.handler.HttpRouteHandler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/7
 */
public class HttpReconnectTest {

    private HttpBootstrap httpBootstrap;


    @Before
    public void init() {
        httpBootstrap = new HttpBootstrap();
        HttpRouteHandler routeHandler = new HttpRouteHandler();

        routeHandler.route("/post", new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                response.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.keepalive.getName());
                JSONObject jsonObject = new JSONObject();
                for (String key : request.getParameters().keySet()) {
                    jsonObject.put(key, request.getParameter(key));
                }
                response.write(jsonObject.toString().getBytes());
                response.close();
            }
        });
        httpBootstrap.configuration().debug(true);
        httpBootstrap.httpHandler(routeHandler).setPort(8080).start();
    }

    @Test
    public void testPost() throws ExecutionException, InterruptedException {
        HttpClient httpClient = new HttpClient("localhost", 8080);
//        httpClient.configuration().debug(true);
        int i = 1000;
        while (i-- > 0) {
            Future<tech.smartboot.feat.client.HttpResponse> future = httpClient.post("/post")
                    .header().keepalive(true).done()
                    .onSuccess(response -> {
                        System.out.println(response.body());
                        httpClient.close();
                    })
                    .onFailure(throwable -> {
                        httpClient.close();
                    })
                    .done();
            if (i % 3 == 0) {
                Thread.sleep(10);
            }
        }
    }

    @After
    public void destroy() {
        httpBootstrap.shutdown();
    }

}

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

import java.util.Set;

public interface PushBuilder {

    PushBuilder method(String method);

    PushBuilder queryString(String queryString);

    PushBuilder setHeader(String name, String value);


    PushBuilder addHeader(String name, String value);


    PushBuilder removeHeader(String name);


    PushBuilder path(String path);


    void push();


    String getMethod();


    String getQueryString();


    Set<String> getHeaderNames();


    String getHeader(String name);

    String getPath();
}

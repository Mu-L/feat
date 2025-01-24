/*******************************************************************************
 * Copyright (c) 2024, tech.smartboot. All rights reserved.
 * project name: feat
 * file name: HttpServerConfiguration.java
 * Date: 2021-02-22
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package tech.smartboot.feat.core.server;

import org.smartboot.socket.extension.plugins.Plugin;
import org.smartboot.socket.extension.plugins.ProxyProtocolPlugin;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;
import tech.smartboot.feat.core.common.enums.HeaderNameEnum;
import tech.smartboot.feat.core.common.utils.ByteTree;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.feat.core.server.handler.BaseHttpHandler;
import tech.smartboot.feat.core.server.impl.HttpEndpoint;
import tech.smartboot.feat.core.server.waf.WafConfiguration;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/22
 */
public class ServerOptions {
    public static final String VERSION = "v0.5";

    /**
     * 缓存
     */
    private final ByteTree<Object> byteCache = new ByteTree<>(16 * 1024);
    /**
     * URI缓存
     */
    private final ByteTree<BaseHttpHandler> uriByteTree = new ByteTree<>(16 * 1024);

    private final ByteTree<HeaderNameEnum> headerNameByteTree = new ByteTree<>(16 * 1024);

    /**
     * smart-socket 插件
     */
    private final List<Plugin<HttpEndpoint>> plugins = new ArrayList<>();

    /**
     * 是否启用控制台banner
     */
    private boolean bannerEnabled = true;
    /**
     * read缓冲区大小
     */
    private int readBufferSize = 1024;
    /**
     * write缓冲区大小
     */
    private int writeBufferSize = 1024;
    /**
     * 服务线程数
     */
    private int threadNum = Math.max(Runtime.getRuntime().availableProcessors(), 2);
    /**
     * 解析的header数量上限
     */
    private int headerLimiter = 100;

    /**
     * 闲置超时时间，默认：1分钟
     */
    private long httpIdleTimeout = 60000;

    /**
     * 闲置超时时间，默认：1分钟
     */
    private long wsIdleTimeout = 120000;
    /**
     * 服务器名称
     */
    private String serverName = "feat";

    /**
     * 是否加密通信
     */
    private boolean secure;
    /**
     * 最大请求报文
     */
    private long maxRequestSize = Integer.MAX_VALUE;

    private boolean lowMemory = false;
    private AsynchronousChannelGroup group;

    private final WafConfiguration wafConfiguration = new WafConfiguration();

    private Runnable shutdownHook;


    int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * 设置read缓冲区大小，读缓冲区的大小至少得能容纳 url 或者一个Header value的长度，否则将触发异常
     *
     * @param readBufferSize
     * @return
     */
    public ServerOptions readBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        return this;
    }

    int getThreadNum() {
        return threadNum;
    }

    public ServerOptions threadNum(int threadNum) {
        this.threadNum = threadNum;
        return this;
    }

    int getWriteBufferSize() {
        return writeBufferSize;
    }

    public ServerOptions writeBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
        return this;
    }

    boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public ServerOptions bannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
        return this;
    }

    public int getHeaderLimiter() {
        return headerLimiter;
    }

    /**
     * 支持解析的Header上限,若客户端提交Header数超过该值，超过部分将被忽略
     *
     * @param headerLimiter
     */
    public ServerOptions headerLimiter(int headerLimiter) {
        this.headerLimiter = headerLimiter;
        return this;
    }

    public ServerOptions proxyProtocolSupport() {
        plugins.add(0, new ProxyProtocolPlugin<>());
        return this;
    }

    /**
     * 启用 debug 模式后会打印码流
     */
    public ServerOptions debug(boolean debug) {
        plugins.removeIf(plugin -> plugin instanceof StreamMonitorPlugin);
        if (debug) {
            addPlugin(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM,
                    StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));
        }
        return this;
    }

    public String serverName() {
        return serverName;
    }

    public ServerOptions serverName(String server) {
        if (server == null) {
            this.serverName = null;
        } else {
            this.serverName = StringUtils.trim(server).replaceAll("\r", "").replaceAll("\n", "");
        }
        return this;
    }

    public ByteTree<BaseHttpHandler> getUriByteTree() {
        return uriByteTree;
    }

    /**
     * 将字符串缓存至 ByteTree 中，在Http报文解析过程中将获得更好的性能表现。
     * 适用范围包括： URL、HeaderName、HeaderValue
     */
    public ByteTree<Object> getByteCache() {
        return byteCache;
    }

    public ByteTree<HeaderNameEnum> getHeaderNameByteTree() {
        return headerNameByteTree;
    }

    public ServerOptions addPlugin(Plugin<HttpEndpoint> plugin) {
        plugins.add(plugin);
        if (plugin instanceof SslPlugin) {
            secure = true;
        }
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public ServerOptions addPlugin(List<Plugin<HttpEndpoint>> plugins) {
        this.plugins.addAll(plugins);
        return this;
    }

    public List<Plugin<HttpEndpoint>> getPlugins() {
        return plugins;
    }

    public AsynchronousChannelGroup group() {
        return group;
    }

    public ServerOptions group(AsynchronousChannelGroup group) {
        this.group = group;
        return this;
    }

    public WafConfiguration getWafConfiguration() {
        return wafConfiguration;
    }

    public long getHttpIdleTimeout() {
        return httpIdleTimeout;
    }

    public ServerOptions setHttpIdleTimeout(long httpIdleTimeout) {
        this.httpIdleTimeout = httpIdleTimeout;
        return this;
    }

    public long getWsIdleTimeout() {
        return wsIdleTimeout;
    }

    public ServerOptions setWsIdleTimeout(long wsIdleTimeout) {
        this.wsIdleTimeout = wsIdleTimeout;
        return this;
    }

    boolean isLowMemory() {
        return lowMemory;
    }

    public ServerOptions setLowMemory(boolean lowMemory) {
        this.lowMemory = lowMemory;
        return this;
    }

    public Runnable shutdownHook() {
        return shutdownHook;
    }

    public ServerOptions shutdownHook(Runnable shutdownHook) {
        this.shutdownHook = shutdownHook;
        return this;
    }
}

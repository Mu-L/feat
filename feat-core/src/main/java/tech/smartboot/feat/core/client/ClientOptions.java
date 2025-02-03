package tech.smartboot.feat.core.client;

import org.smartboot.socket.extension.plugins.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/2/13
 */
class ClientOptions<T> {


    /**
     * smart-socket 插件
     */
    private final List<Plugin<T>> plugins = new ArrayList<>();

    /**
     * 连接超时时间
     */
    private int connectTimeout;

    /**
     * 远程地址
     */
    private final String host;
    /**
     * 远程端口
     */
    private final int port;

    private ProxyConfig proxy;

    /**
     * read缓冲区大小
     */
    private int readBufferSize = 1024;

    /**
     * read缓冲区大小
     */
    private int writeBufferSize = 1024;


    private boolean https = false;

    private boolean debug = false;

    public ClientOptions(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置建立连接的超时时间
     */
    protected ClientOptions connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


    /**
     * 设置 Http 代理服务器
     *
     * @param host     代理服务器地址
     * @param port     代理服务器端口
     * @param username 授权账户
     * @param password 授权密码
     */
    protected ClientOptions<T> proxy(String host, int port, String username, String password) {
        this.proxy = new ProxyConfig(host, port, username, password);
        return this;
    }

    /**
     * 连接代理服务器
     *
     * @param host 代理服务器地址
     * @param port 代理服务器端口
     */
    public ClientOptions proxy(String host, int port) {
        return this.proxy(host, port, null, null);
    }

    ProxyConfig getProxy() {
        return proxy;
    }

    public int readBufferSize() {
        return readBufferSize;
    }

    protected ClientOptions<T> readBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        return this;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public ClientOptions<T> setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
        return this;
    }

    /**
     * 启用 debug 模式后会打印码流
     */
    protected ClientOptions<T> debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    boolean isDebug() {
        return debug;
    }

    protected ClientOptions<T> addPlugin(Plugin<T> plugin) {
        plugins.add(plugin);
        return this;
    }

    public List<Plugin<T>> getPlugins() {
        return plugins;
    }

    public boolean isHttps() {
        return https;
    }

    public ClientOptions setHttps(boolean https) {
        this.https = https;
        return this;
    }
}

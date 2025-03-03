/*
 *  Copyright (C) [2024] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.feat.fileserver;

import tech.smartboot.feat.core.server.ServerOptions;

/**
 * @author 三刀(zhengjunweimail@163.com)
 * @version v1.0.0
 */
public class FileServerOptions extends ServerOptions {
    private String baseDir = "./";
    /**
     * 若设置为true，则允许以目录列表的形式展现
     */
    private boolean autoIndex = false;

    public String baseDir() {
        return baseDir;
    }

    public FileServerOptions baseDir(String baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    public boolean autoIndex() {
        return autoIndex;
    }

    public FileServerOptions autoIndex(boolean autoIndex) {
        this.autoIndex = autoIndex;
        return this;
    }
}

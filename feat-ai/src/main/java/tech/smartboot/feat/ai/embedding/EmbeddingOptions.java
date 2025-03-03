/*
 *  Copyright (C) [2024] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.feat.ai.embedding;

public class EmbeddingOptions {
    public static final String AI_VENDOR_GITEE = "https://ai.gitee.com/v1/";
    private String baseUrl = System.getenv("FEATAI_BASE_URL") != null ? System.getenv("FEAT_AI_BASE_URL") : AI_VENDOR_GITEE;
    private boolean debug;
    private String model;
    private String apiKey = System.getenv("FEATAI_API_KEY");

    public boolean isDebug() {
        return debug;
    }

    public EmbeddingOptions debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public EmbeddingOptions baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getModel() {
        return model;
    }

    public EmbeddingOptions model(String model) {
        this.model = model;
        return this;
    }

    public EmbeddingOptions model(ModelVendor model) {
        return baseUrl(model.getVendor()).model(model.getModel());
    }

    public String getApiKey() {
        return apiKey;
    }

    public EmbeddingOptions apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
}

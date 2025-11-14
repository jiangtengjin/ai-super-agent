package com.xhh.aiagent.constant;

/**
 * 网络搜索工具相关常量
 */
public interface WebSearchConstant {

    /**
     * 搜索源
     */
    String URL = "https://www.searchapi.io/api/v1/search";

    /**
     * 搜索引擎（key）
     */
    String PARAM_ENGINE = "engine";

    /**
     * 搜索引擎（value）
     */
    String PARAM_ENGINE_VALUE = "baidu";

    /**
     * 搜索关键字
     */
    String PARAM_Q = "q";

    /**
     * API Key
     */
    String PARAM_API_KEY = "api_key";

    /**
     * 响应字段
     * organic_results
     */
    String ORGANIC_RESULTS = "organic_results";

}

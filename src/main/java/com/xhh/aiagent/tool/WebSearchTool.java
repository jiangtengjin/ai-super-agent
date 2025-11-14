package com.xhh.aiagent.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xhh.aiagent.constant.WebSearchConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网络搜索工具
 */
public class WebSearchTool {

    private final String apiKey;

    public WebSearchTool(String apiKey){
        this.apiKey = apiKey;
    }

    @Tool(description = "Search content from web")
    public String searchContentFromWeb(@ToolParam(description = "The content needs to search") String keyWord) {
        // 准备请求参数
        Map<String, Object> param = new HashMap<>();
        param.put(WebSearchConstant.PARAM_ENGINE, WebSearchConstant.PARAM_ENGINE_VALUE);
        param.put(WebSearchConstant.PARAM_Q, keyWord);
        param.put(WebSearchConstant.PARAM_API_KEY, apiKey);
        try {
            // 发送请求
            String response = HttpUtil.get(WebSearchConstant.URL, param);
            // 解析响应结果
            JSONObject jsonObject = JSONUtil.parseObj(response);
            // 提取 organic_results
            JSONArray organicResults = jsonObject.getJSONArray(WebSearchConstant.ORGANIC_RESULTS);
            List<Object> objects = organicResults.subList(0, 3);
            // 拼接搜索结果
            String result = objects.stream().map(obj -> {
                JSONObject tmpJSONObj = (JSONObject) obj;
                return tmpJSONObj.toString();
            }).collect(Collectors.joining(","));
            return result;
        } catch (Exception e) {
            return "Fail to search content：" + e.getMessage();
        }
    }

}

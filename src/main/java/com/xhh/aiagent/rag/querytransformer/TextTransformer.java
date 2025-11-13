package com.xhh.aiagent.rag.querytransformer;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class TextTransformer {

    /**
     * 缓存 accessToken
     * 写入后 30天过期，和百度的 accessToken 保持一致
     * 访问后 10 分钟过期
     */
    private final Cache<String, String> accessTokenCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofSeconds(2592000))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) ->
                    log.info("accessToken 被移除: key={}, 原因={}", key, cause))
            .build();

    // 缓存 key
    private final String CACHE_KEY = "access_token";

    /**
     * 翻译成目标语言
     *
     * @param from      源语言
     * @param to        目标语言
     * @param q         翻译内容
     * @param termIds   翻译术语ID
     * @return
     */
    public String textTrans(String from, String to, String q, String termIds) {
        // 获取 accessToken
        String cacheValue = accessTokenCache.get(this.accessKey, key -> this.getAccessToken());

        // 构建请求URL
        String url = "https://aip.baidubce.com/rpc/2.0/mt/texttrans/v1?access_token=" + cacheValue;

        // 构建请求体JSON
        JSONObject requestBody = new JSONObject();
        requestBody.set("q", q);
        requestBody.set("from", from);
        requestBody.set("to", to);
        requestBody.set("termIds", termIds);

        // 发送POST请求
        HttpResponse response = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(5000) // 设置超时时间5秒
                .execute();

        // 检查请求是否成功
        if (response.isOk()) {
            log.info("翻译接口请求成功，result：{}",response.body());
            // 解析结果
            JSONObject json = JSONUtil.parseObj(response.body());
            return json.getByPath("result.trans_result[0].dst", String.class);
        } else {
            log.error("翻译请求失败，HTTP状态码: {}", response.getStatus());
        }
        return "";
    }

    @Value("${baidu.access_key}")
    private String accessKey;

    @Value("${baidu.secret_key}")
    private String secretKey;

    /**
     * 生成 access token
     * 翻译接口需要携带这个参数
     *
     * @return accessToken
     */
    private String getAccessToken(){
        String url = String.format(
                "https://aip.baidubce.com/oauth/2.0/token?client_id=%s&client_secret=%s&grant_type=client_credentials",
                accessKey,
                secretKey);

        try {
            // HttpUtil.post 会自动处理空请求体
            String result = HttpUtil.post(url, "");
            log.info("获取 access token 成功，result：{}", result);

            // 解析结果
            JSONObject json = JSONUtil.parseObj(result);
            String accessToken = json.getStr("access_token", "");
            return accessToken;

        } catch (Exception e) {
            log.error("获取 access token 失败：{}", e.getMessage(), e);
            return "";
        }
    }
}

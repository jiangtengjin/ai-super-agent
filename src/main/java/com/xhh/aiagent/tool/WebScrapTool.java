package com.xhh.aiagent.tool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具
 */
public class WebScrapTool {

    @Tool(description = "抓取网页内容")
    public String doScrap(@ToolParam(description = "需要抓取网页的 url") String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return document.html();
        } catch (Exception e) {
            return "网页抓取失败：" + e.getMessage();
        }
    }

}

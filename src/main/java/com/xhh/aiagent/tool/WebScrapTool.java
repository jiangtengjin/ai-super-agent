package com.xhh.aiagent.tool;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具
 */
public class WebScrapTool {

    @Tool(description = "Scraping content from a web page")
    public String doScrap(@ToolParam(description = "URL of the web page to scrap") String url) {
        try {
            Document document = Jsoup.connect(url)
                    .timeout(10000)
                    .userAgent("Mozilla/5.0")
                    .get();
            return document.text();
        } catch (Exception e) {
            return "Fail to scrap content：" + e.getMessage();
        }
    }

}

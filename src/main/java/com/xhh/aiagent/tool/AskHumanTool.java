package com.xhh.aiagent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Scanner;

/**
 * 向人类寻求帮助
 */
public class AskHumanTool {

    @Tool(description = "Use this tool to ask human for help.")
    public String doAsk(@ToolParam(description = "The question you want to ask human") String question) {
        return new Scanner(System.in).nextLine().trim();
    }

}

package com.xhh.aiagent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Scanner;

/**
 * 向人类寻求帮助
 * 暂定（Spring boot 项目中的控制台是只读的，不能通过 Scanner 的方式进行交互）
 */
public class AskHumanTool {

    @Tool(description = "Use this tool to ask human for help.")
    public String doAsk(@ToolParam(description = "The question you want to ask human") String question) {
        System.out.println("\n=== 机器人需要帮助 ===");
        System.out.println("Bot: " + question);
        System.out.print("You: ");

        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim();

        System.out.println("=== 继续执行 ===");
        return response;
    }

}

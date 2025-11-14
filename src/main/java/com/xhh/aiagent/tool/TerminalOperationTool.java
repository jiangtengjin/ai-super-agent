package com.xhh.aiagent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TerminalOperationTool {

    @Tool(description = "在终端上执行命令")
    public String executeTerminalCommand(@ToolParam(description = "需要在终端上执行的命令") String command) {
        StringBuilder output = new StringBuilder();
        try {
//            Process process = Runtime.getRuntime().exec(command);
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("命令执行失败，错误码: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("执行失败的命令: ").append(e.getMessage());
        }
        return output.toString();
    }
}

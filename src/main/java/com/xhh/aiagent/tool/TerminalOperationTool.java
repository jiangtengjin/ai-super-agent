package com.xhh.aiagent.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TerminalOperationTool {

    @Tool(description = "Execute command on terminal")
    public String executeTerminalCommand(@ToolParam(description = "The command needs to execute") String command) {
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
                output.append("Fail to execute command, exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Fail to execute command: ").append(e.getMessage());
        }
        return output.toString();
    }
}

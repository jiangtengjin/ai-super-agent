package com.xhh.aiagent.tool;

import com.xhh.aiagent.model.enums.DaskStatus;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 终止工具
 */
public class TerminateTool {

    @Tool(description = "Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.\n" +
            "When you have finished all the tasks, call this tool to end the work.")
    public String terminate(@ToolParam(description = "The finish status of the interaction.") DaskStatus status) {
        return String.format("The interaction has been completed with status: {%s}", status);
    }

}

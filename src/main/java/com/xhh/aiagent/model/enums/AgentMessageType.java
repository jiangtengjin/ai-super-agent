package com.xhh.aiagent.model.enums;

// 消息类型枚举
public enum AgentMessageType {
    THOUGHTS, // AI思考过程
    TOOL_SELECTION, // 工具选择
    TOOL_CALL_INFO, // 工具调用详情
    TOOL_RESULT, // 工具调用结果
    STEP_INFO, // 步骤信息
    ERROR, // 错误信息
    SYSTEM // 系统信息
}
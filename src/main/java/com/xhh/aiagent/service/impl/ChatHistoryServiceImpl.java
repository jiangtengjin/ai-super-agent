package com.xhh.aiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhh.aiagent.model.entity.ChatHistory;
import com.xhh.aiagent.service.ChatHistoryService;
import com.xhh.aiagent.mapper.ChatHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author 机hui难得
* @description 针对表【chat_history(对话历史)】的数据库操作Service实现
* @createDate 2025-11-08 17:51:13
*/
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{

}





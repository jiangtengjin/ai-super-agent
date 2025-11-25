package com.xhh.aiagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhh.aiagent.exception.ErrorCode;
import com.xhh.aiagent.exception.ThrowUtils;
import com.xhh.aiagent.mapper.ConversationMapper;
import com.xhh.aiagent.model.entity.Conversation;
import com.xhh.aiagent.model.request.ConversationQueryRequest;
import com.xhh.aiagent.model.vo.ConversationVO;
import com.xhh.aiagent.service.ConversationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 机hui难得
 * @description 针对表【conversation(对话历史)】的数据库操作Service实现
 * @createDate 2025-11-22 17:33:17
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    @Override
    public Long addConversation(String userMessage, String conversationId, Long userId) {
        // 准备参数
        Conversation conversation = new Conversation();
        conversation.setConversationId(conversationId);
        conversation.setUserId(userId);
        String name = userMessage;
        if (userMessage.length() > 10) {
            name = userMessage.substring(0, 10) + "...";
        }
        conversation.setName(name);

        // 添加到数据库
        this.save(conversation);
        return conversation.getId();
    }

    @Override
    public Page<ConversationVO> getConversationByPage(int pageSize, LocalDateTime lastCreateTime, long userId) {
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        // 构造查询条件
        ConversationQueryRequest queryRequest = new ConversationQueryRequest();
        queryRequest.setUserId(userId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper<Conversation> queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询
        Page<Conversation> page = this.page(Page.of(1, pageSize), queryWrapper);

        // 处理查询结果
        List<ConversationVO> list = page.getRecords().stream()
                .map(this::objToVO)
                .collect(Collectors.toList());

        Page<ConversationVO> conversationVOPage = new Page<>();
        conversationVOPage.setTotal(page.getTotal());
        conversationVOPage.setRecords(list);
        return conversationVOPage;
    }

    @Override
    public Conversation getConversationByCId(String conversationId) {
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getConversationId, conversationId);
        Conversation conversation = this.getOne(queryWrapper);
        return conversation;
    }

    @Override
    public QueryWrapper<Conversation> getQueryWrapper(ConversationQueryRequest conversationQueryRequest) {
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        if (conversationQueryRequest == null) {
            return queryWrapper;
        }
        Long id = conversationQueryRequest.getId();
        String name = conversationQueryRequest.getName();
        String conversationId = conversationQueryRequest.getConversationId();
        Long userId = conversationQueryRequest.getUserId();
        LocalDateTime lastCreateTime = conversationQueryRequest.getLastCreateTime();
        String sortField = conversationQueryRequest.getSortField();

        // 拼接查询条件
        queryWrapper.eq(id != null, "id", id)
                .like(StrUtil.isNotBlank(name),"name", name)
                .eq(StrUtil.isNotBlank(name),"conversationId", conversationId)
                .eq(userId != null && userId > 1, "userId", userId);
        // 游标查询逻辑
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderByAsc(sortField);
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderByDesc("createTime");
        }
        return queryWrapper;
    }

    /**
     * 实体转 VO
     *
     * @param conversation
     * @return
     */
    public ConversationVO objToVO(Conversation conversation) {
        if (conversation == null) {
            return new ConversationVO();
        }
        return BeanUtil.copyProperties(conversation, ConversationVO.class);
    }
}





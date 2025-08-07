package com.zake.aicode.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zake.aicode.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.zake.aicode.model.entity.ChatHistory;
import com.zake.aicode.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a>程序员zake</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     *  删除对话消息
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);



    /**
     * 获取查询条件
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);


    /**
     * 分页获取对话消息
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    /**
     * 数据库中加载对话历史到记忆中
     * @param appId
     * @param chatMemory
     * @param maxCount
     * @return
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}

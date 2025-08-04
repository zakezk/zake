package com.zake.aicode.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zake.aicode.model.entity.ChatHistory;
import com.zake.aicode.mapper.ChatHistoryMapper;
import com.zake.aicode.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author <a>程序员zake</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}

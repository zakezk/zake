package com.zake.aicode.ai;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * ai服务创建  工厂设计模式
 */
@Configuration
public class AiCodeGeneratorServiceFactory {

    //格式化输出
    @Resource
    private ChatModel chatModel;

    //流式输出
    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建AI代码生成器服务
     *
     * @return
     */

//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();

    }

}
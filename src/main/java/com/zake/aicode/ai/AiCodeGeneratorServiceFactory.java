package com.zake.aicode.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zake.aicode.ai.tools.*;
import com.zake.aicode.exception.BusinessException;
import com.zake.aicode.exception.ErrorCode;
import com.zake.aicode.model.enums.CodeGenTypeEnum;
import com.zake.aicode.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


/**
 * ai服务创建  工厂设计模式
 */

@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    //格式化输出
    @Resource
    private ChatModel chatModel;

    //流式输出 - 使用默认的OpenAI流式模型（由Spring Boot自动配置创建）
    @Resource
    private StreamingChatModel openAiStreamingChatModel;

    //聊天记录存储 实现会话隔离
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    //聊天记录服务
    @Resource
    private ChatHistoryService chatHistoryService;

    //推理流式模型（用于 Vue 项目生成，带工具调用）
    @Resource
    private StreamingChatModel reasoningStreamingChatModel;


    @Resource
    private ToolManager toolManager;

    /**
     * 默认提供一个 Bean
     * 满足之前测试用例
     */
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return getAiCodeGeneratorService(0L);
//    }

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();
//
//    /**
//     * AI 服务实例缓存  caffeine
//     * 缓存策略：
//     * - 最大缓存 1000 个实例
//     * - 写入后 30 分钟过期
//     * - 访问后 10 分钟过期
//     */
//    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
//            .maximumSize(1000)
//            .expireAfterWrite(Duration.ofMinutes(30))
//            .expireAfterAccess(Duration.ofMinutes(10))
//            .removalListener((key, value, cause) -> {
//                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
//            })
//            .build();


//    /**
//     * 根据 appId 获取服务（带缓存）这个方法是为了兼容历史逻辑
//     */
//    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
//        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
//    }

    /**
     * 根据 appId 和代码生成类型获取服务（带缓存）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType.getValue();
    }

    /**
     * 创建新的 AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        // 根据代码生成类型选择不同的模型配置
        return switch (codeGenType) {
            // Vue 项目生成使用推理模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    // 添加工具
                    .tools(
//                            new FileWriteTool(),
//                            new FileReadTool(),
//                            new FileModifyTool(),
//                            new FileDirReadTool(),
//                            new FileDeleteTool()
                            toolManager.getAllTools()
                    )

                    // ai调用没有的工具 处理
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, "Error: there is no tool called "
                                    + toolExecutionRequest.name()
                    ))
                    .build();
            // HTML 和多文件生成使用默认模型
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(chatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "不支持的代码生成类型: " + codeGenType.getValue());
        };
    }

//
//    /**
//     * 创建新的 AI 服务实例(添加本地缓存)
//     */
//    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
//        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
//        // 根据 appId 构建独立的对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
//                .builder()
//                .id(appId)
//                .chatMemoryStore(redisChatMemoryStore)
//                .maxMessages(20)
//                .build();
//        // 从数据库加载历史对话到记忆中
//        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(openAiStreamingChatModel)
//                .chatMemory(chatMemory)
//                .build();
//    }


    /**
     * 内置 隔离
     * 会话隔离 方案一
     * @return
     */
//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                // 根据 id 构建独立的对话记忆
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory
//                        .builder()
//                        .id(memoryId)
//                        .chatMemoryStore(redisChatMemoryStore)
//                        .maxMessages(20)
//                        .build())
//                .build();
//    }
    /**
     * 根据 appId 获取服务
     * 方案二 AIService
     */
//    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
//        // 根据 appId 构建独立的对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
//                .builder()
//                .id(appId)
//                .chatMemoryStore(redisChatMemoryStore)
//                .maxMessages(20)
//                .build();
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .chatMemory(chatMemory)
//                .build();
//    }

//    /**
//     * 创建新的 AI 服务实例
//     */
//    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
//        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
//        // 根据 appId 构建独立的对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
//                .builder()
//                .id(appId)
//                .chatMemoryStore(redisChatMemoryStore)
//                .maxMessages(20)
//                .build();
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .chatMemory(chatMemory)
//                .build();
//    }


    /**
     * 创建AI代码生成器服务
     *
     * @return
     */

//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
//    }


    /**
     * 流式生成
     */

//    @Bean
//    public AiCodeGeneratorService aiCodeGeneratorService() {
//        return AiServices.builder(AiCodeGeneratorService.class)
//                .chatModel(chatModel)
//                .streamingChatModel(streamingChatModel)
//                .build();
//
//    }

}
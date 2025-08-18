package com.zake.aicode.langgraph4j.ai;

import com.zake.aicode.langgraph4j.tool.ImageSearchTool;
import com.zake.aicode.langgraph4j.tool.LogoGeneratorTool;
import com.zake.aicode.langgraph4j.tool.MermaidDiagramTool;
import com.zake.aicode.langgraph4j.tool.UndrawIllustrationTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片收集服务工厂
 */
@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    //图片收集服务
    @Resource
    private ImageSearchTool imageSearchTool;

    //插画图片收集服务
    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    //架构图图片收集服务
    @Resource
    private MermaidDiagramTool mermaidDiagramTool;

    //logo图片收集服务
    @Resource
    private LogoGeneratorTool logoGeneratorTool;

    /**
     * 创建图片收集 AI 服务
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .chatModel(chatModel)
                .tools(
                        imageSearchTool,
                        undrawIllustrationTool,
                        mermaidDiagramTool,
                        logoGeneratorTool
                )
                .build();
    }
}
package com.zake.aicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;


/**
 * 结构化输出
 * htmlCode: html代码结果
 */
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    @Description("生成的 HTML 代码")
    private String htmlCode;
    @Description("生成的 代码描述 描述")
    private String description;
}


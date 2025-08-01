package com.zake.aicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * MultiFileCodeResult
 * 结构化 输出
 */

@Description("生成多文件代码文件的结果")
@Data
public class MultiFileCodeResult {

    @Description("生成的 HTML 描述")
    private String htmlCode;

    @Description("生成的 CSS 描述")
    private String cssCode;

    @Description("生成的 JS 描述")
    private String jsCode;

    @Description("生成的 代码的 描述")
    private String description;
}
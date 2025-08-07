package com.zake.aicode.ai;

import com.zake.aicode.ai.model.HtmlCodeResult;
import com.zake.aicode.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHTMLCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode("请做个程序员zake的工作记录小工具，控制代码在50行内");
        //断言：结果不为空
        Assertions.assertNotNull(result);
    }

    @Test
    void testGenerateCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("做个程序员zake的留言板，不超过50行");
        //断言：结果不为空
        Assertions.assertNotNull(multiFileCode);
    }

    @Test
    void testChatMemory() {
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(1, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
//        Assertions.assertNotNull(result);
//        result = aiCodeGeneratorService.generateHtmlCode(1, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(result);
//        result = aiCodeGeneratorService.generateHtmlCode(2, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
//        Assertions.assertNotNull(result);
//        result = aiCodeGeneratorService.generateHtmlCode(2, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(result);
    }
}
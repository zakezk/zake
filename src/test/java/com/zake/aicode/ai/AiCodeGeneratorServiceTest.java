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
}
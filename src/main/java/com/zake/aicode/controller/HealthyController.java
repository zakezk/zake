package com.zake.aicode.controller;

import com.zake.aicode.common.BaseResponse;
import com.zake.aicode.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/healthy")
public class HealthyController {


    @GetMapping("/")
    public BaseResponse<String> healthy() {
        return ResultUtils.success("healthy");
    }

}

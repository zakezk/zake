package com.zake.aicode.model.dto.app;

import com.zake.aicode.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 应用查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;
    /**
     * 应用封面
     */
    private String cover;
    /**
     * 代码生成类型（枚举）
     */
    private String codeGenType;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;


    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 优先级
     */
    private Integer priority;

    private static final long serialVersionUID = 1L;
} 
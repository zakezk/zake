package com.zake.aicode.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 *  用户注册请求
 */
@Data
public class UserRegisterRequest implements Serializable {


    //JDK 14+ 新增的序列化元数据注解
    @Serial
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
package com.zake.aicode.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 *  dto 数据传输对象
 * DTO 属于一种数据结构，其主要功能是在不同的应用层或者进程之间传输数据。
 * 它一般只包含数据字段以及对应的 getter/setter 方法，不涉及业务逻辑。
 * DTO 存在的意义在于降低系统间数据交换的复杂度。
 */

/**
 * 用户添加请求
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
package com.zake.aicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zake.aicode.model.dto.user.UserQueryRequest;
import com.zake.aicode.model.entity.User;
import com.zake.aicode.model.vo.LoginUserVO;
import com.zake.aicode.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a>程序员zake</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密
     * @return 用户
     */
    public String getEncryptPassword(String userPassword);

    /**
     * 用户登陆
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */

    LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     *
      * @param request HttpServletRequest
      * @return User
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户注销
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 用户转换为VO脱敏
     * @param user
     * @return
     */
    public UserVO getUserVO(User user);


    /**
     * 用湖托名
     * @param userList
     * @return
     */
    public List<UserVO> getUserVOList(List<User> userList) ;

    /**
     *  查询 QueryWrapper
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}

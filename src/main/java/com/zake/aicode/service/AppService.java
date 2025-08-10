package com.zake.aicode.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zake.aicode.model.dto.app.AppAddRequest;
import com.zake.aicode.model.dto.app.AppAdminUpdateRequest;
import com.zake.aicode.model.dto.app.AppQueryRequest;
import com.zake.aicode.model.dto.app.AppUpdateRequest;
import com.zake.aicode.model.entity.App;
import com.zake.aicode.model.entity.User;
import com.zake.aicode.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a>程序员zake</a>
 */
public interface AppService extends IService<App> {

    /**
     * 部署应用
     *
     */
    String deployApp(Long appId, User loginUser);

    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @return 生成结果流
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 创建应用
     *
     * @param appAddRequest 应用添加请求
     * @param request       HttpServletRequest
     * @return 新应用 id
     */
    long createApp(AppAddRequest appAddRequest, HttpServletRequest request);

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HttpServletRequest
     * @return 是否成功
     */
    boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request);

    /**
     * 根据 id 删除自己的应用
     *
     * @param id      应用id
     * @param request HttpServletRequest
     * @return 是否成功
     */
    boolean deleteMyApp(Long id, HttpServletRequest request);

    /**
     * 根据 id 查看应用详情
     *
     * @param app 应用app
     * @return 应用详情
     */
    AppVO getAppVOById(App app);

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         HttpServletRequest
     * @return 应用列表
     */
    Page<AppVO> listMyAppByPage(AppQueryRequest appQueryRequest, HttpServletRequest request);

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    Page<AppVO> listFeaturedAppByPage(AppQueryRequest appQueryRequest);

    /**
     * 根据 id 删除任意应用（管理员）
     *
     * @param id 应用id
     * @return 是否成功
     */
    boolean deleteApp(Long id);

    /**
     * 根据 id 更新任意应用（管理员，支持更新应用名称、应用封面、优先级）
     *
     * @param appUpdateRequest 应用更新请求
     * @return 是否成功
     */
    boolean updateApp(AppAdminUpdateRequest appUpdateRequest);

    /**
     * 分页查询应用列表（管理员，支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    Page<AppVO> listAppByPage(AppQueryRequest appQueryRequest);

    /**
     * 应用转换为VO脱敏
     *
     * @param app 应用
     * @return AppVO
     */
    AppVO getAppVO(App app);

    /**
     * 应用列表转换为VO脱敏
     *
     * @param appList 应用列表
     * @return AppVO列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 查询 QueryWrapper
     *
     * @param appQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}

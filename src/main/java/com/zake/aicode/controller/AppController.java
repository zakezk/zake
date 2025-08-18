package com.zake.aicode.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.zake.aicode.annotation.AuthCheck;
import com.zake.aicode.common.BaseResponse;
import com.zake.aicode.common.DeleteRequest;
import com.zake.aicode.common.ResultUtils;
import com.zake.aicode.constant.AppConstant;
import com.zake.aicode.constant.UserConstant;
import com.zake.aicode.exception.BusinessException;
import com.zake.aicode.exception.ErrorCode;
import com.zake.aicode.exception.ThrowUtils;
import com.zake.aicode.model.dto.app.*;
import com.zake.aicode.model.entity.App;
import com.zake.aicode.model.entity.User;
import com.zake.aicode.model.vo.AppVO;
import com.zake.aicode.ratelimter.annotation.RateLimit;
import com.zake.aicode.ratelimter.enums.RateLimitType;
import com.zake.aicode.service.AppService;
import com.zake.aicode.service.ProjectzDownloadService;
import com.zake.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;

/**
 * 应用 控制层。
 *
 * @author <a>程序员zake</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    @Resource
    UserService userService;

    @Resource
    private ProjectzDownloadService projectDownloadService;

    /**
     * 应用聊天生成代码（流式 SSE）
     * 必须写：在流式接口中显式声明
     *  MediaType.TEXT_EVENT_STREAM_VALUE 流式响应
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER,rate = 5, rateInterval = 60,message = "请求过于频繁，请稍后再试")//同一个用户在60s内只能请求5次
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        // 转换为 ServerSentEvent 格式 解决空格丢失 和 前端不知道 什么时候结束的 问题
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);//创建不可变 map
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    // 创建 ServerSentEvent （sse）对象
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)//设置数据
                            .build();
                })
                // 2. 流终止处理：追加结束信号
                .concatWith(Mono.just(
                        // 发送结束事件
                        ServerSentEvent.<String>builder()
                                .event("done")
                                .data("")
                                .build()
                ));
    }
    /**
     * 创建应用
     *
     * @param appAddRequest 应用添加请求
     * @param request       HttpServletRequest
     * @return 新应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> createApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        long result = appService.createApp(appAddRequest, request);
        return ResultUtils.success(result);
    }




    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }
    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest
            , HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 根据 id 修改自己的应用（目前只支持修改应用名称）
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HttpServletRequest
     * @return 是否成功
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = appService.updateMyApp(appUpdateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 删除自己的应用
     *
     * @param request HttpServletRequest
     * @return 是否成功
     */
    @PostMapping("/delete/my")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody   DeleteRequest deleteRequest, HttpServletRequest request) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        boolean result = appService.deleteMyApp(id, request);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 查看应用详情
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(@RequestParam Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        //查app信息
        App app = appService.getById(id);
        //service 查user
        AppVO appVO = appService.getAppVOById(app);
        return ResultUtils.success(appVO);
    }

    /**
     * 分页查询自己的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @param request         HttpServletRequest
     * @return 应用列表
     */
    @PostMapping("/list/my/page")
    public BaseResponse<Page<AppVO>> listMyAppByPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> result = appService.listMyAppByPage(appQueryRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询精选的应用列表（支持根据名称查询，每页最多 20 个）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @Cacheable(
            value = "good_app_page",
            key = "T(com.zake.aicode.utils.CacheKeyUtils).generateKey(#appQueryRequest)",//生成缓存的key
            condition = "#appQueryRequest.pageNum <= 10"
    )
    @PostMapping("/list/featured/page")
    public BaseResponse<Page<AppVO>> listFeaturedAppByPage(@RequestBody AppQueryRequest appQueryRequest) {

        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> result = appService.listFeaturedAppByPage(appQueryRequest);
        return ResultUtils.success(result);
    }

//    /**
//     * 分页获取精选应用列表
//     *
//     * @param appQueryRequest 查询请求
//     * @return 精选应用列表
//     */
//    @PostMapping("/good/list/page/vo")
//    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
//        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
//        // 限制每页最多 20 个
//        long pageSize = appQueryRequest.getPageSize();
//        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
//        long pageNum = appQueryRequest.getPageNum();
//        // 只查询精选的应用
//        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
//        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
//        // 分页查询
//        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
//        // 数据封装
//        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
//        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
//        appVOPage.setRecords(appVOList);
//        return ResultUtils.success(appVOPage);
//    }

    /**
     * 根据 id 删除任意应用（管理员）
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.deleteApp(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 更新任意应用（管理员，支持更新应用名称、应用封面、优先级）
     *
     * @param appUpdateRequest 应用更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateApp(@RequestBody AppAdminUpdateRequest appUpdateRequest) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = appService.updateApp(appUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询应用列表（管理员，支持根据除时间外的任何字段查询，每页数量不限）
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<AppVO> result = appService.listAppByPage(appQueryRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取应用（管理员）
     *
     * @param id 应用id
     * @return 应用详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppById(@RequestParam Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(app);
    }
}

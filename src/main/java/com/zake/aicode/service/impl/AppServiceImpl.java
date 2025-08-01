package com.zake.aicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zake.aicode.constant.AppConstant;
import com.zake.aicode.core.AiCodeGeneratorFacade;
import com.zake.aicode.exception.BusinessException;
import com.zake.aicode.exception.ErrorCode;
import com.zake.aicode.exception.ThrowUtils;
import com.zake.aicode.mapper.AppMapper;
import com.zake.aicode.model.dto.app.AppAddRequest;
import com.zake.aicode.model.dto.app.AppQueryRequest;
import com.zake.aicode.model.dto.app.AppUpdateRequest;
import com.zake.aicode.model.entity.App;
import com.zake.aicode.model.entity.User;
import com.zake.aicode.model.enums.CodeGenTypeEnum;
import com.zake.aicode.model.vo.AppVO;
import com.zake.aicode.model.vo.UserVO;
import com.zake.aicode.service.AppService;
import com.zake.aicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用 服务层实现。
 *
 * @author <a>程序员zake</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Autowired
    private UserService userService;


    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;



    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }
    @Override
    public long createApp(AppAddRequest appAddRequest, HttpServletRequest request) {
        // 1. 校验
        if (appAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String initPrompt = appAddRequest.getInitPrompt();
        if (StringUtils.isBlank(initPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "初始化提示不能为空");
        }


        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 3. 创建应用
        App app = new App();
        BeanUtils.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 暂时设置为多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 设置为默认
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY);

        //插入数据库
        boolean save = this.save(app);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建应用失败");
        }

        return app.getId();
    }

    @Override
    public boolean updateMyApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        // 1. 校验
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 3. 检查应用是否存在且属于当前用户
        App oldApp = this.getById(appUpdateRequest.getId());
        if (oldApp == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限修改此应用");
        }

        // 4. 更新应用（目前只支持修改应用名称）
        App app = new App();
        app.setId(appUpdateRequest.getId());
        app.setAppName(appUpdateRequest.getAppName());
        app.setUpdateTime(LocalDateTime.now());

        return this.updateById(app);
    }

    @Override
    public boolean deleteMyApp(Long id, HttpServletRequest request) {
        // 1. 校验
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }

        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 3. 检查应用是否存在且属于当前用户
        App app = this.getById(id);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能删除自己的应用");
        }

        // 4. 删除应用
        return this.removeById(id);
    }

    @Override
    public AppVO getAppVOById(App app) {

        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);

        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return getAppVO(app);
    }

    @Override
    public Page<AppVO> listMyAppByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        // 1. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 2. 设置查询条件
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }
        appQueryRequest.setUserId(loginUser.getId());

        // 3. 限制每页最多20个
        if (appQueryRequest.getPageSize() > 20) {
            appQueryRequest.setPageSize(20);
        }

        // 4. 查询
        Page<App> appPage = this.page
                (Page.of(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()),
                getQueryWrapper(appQueryRequest));

        // 5. 转换为VO
        Page<AppVO> appVOPage = new Page<>(appQueryRequest.getPageNum(), appQueryRequest.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public Page<AppVO> listFeaturedAppByPage(AppQueryRequest appQueryRequest) {
        // 1. 设置查询条件
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }

        // 2. 限制每页最多20个
        if (appQueryRequest.getPageSize() > 20) {
            appQueryRequest.setPageSize(20);
        }

        // 3. 查询精选应用（优先级不为空的应用）
        QueryWrapper queryWrapper = getQueryWrapper(appQueryRequest);
        queryWrapper.eq( App::getPriority,AppConstant.GOOD_APP_PRIORITY);

        Page<App> appPage = this.page(Page.of(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()),
                queryWrapper);

        // 4. 转换为VO
        Page<AppVO> appVOPage = new Page<>(appQueryRequest.getPageNum(), appQueryRequest.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public boolean deleteApp(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用ID无效");
        }
        return this.removeById(id);
    }

    @Override
    public boolean updateApp(AppUpdateRequest appUpdateRequest) {
        if (appUpdateRequest == null || appUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        App app = new App();
        BeanUtils.copyProperties(appUpdateRequest, app);
        app.setUpdateTime(LocalDateTime.now());

        return this.updateById(app);
    }

    @Override
    public Page<AppVO> listAppByPage(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            appQueryRequest = new AppQueryRequest();
        }

        Page<App> appPage = this.page(Page.of(appQueryRequest.getPageNum(), appQueryRequest.getPageSize()),
                getQueryWrapper(appQueryRequest));

        Page<AppVO> appVOPage = new Page<>(appQueryRequest.getPageNum(), appQueryRequest.getPageSize(), appPage.getTotalRow());
        List<AppVO> appVOList = getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);

        return appVOPage;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        return appList.stream().map(this::getAppVO).toList();
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

}

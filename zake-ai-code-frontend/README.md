# Zake AI 应用生成平台

一个基于 AI 对话的应用生成平台，用户可以通过自然语言描述来创建网站和应用。

## 功能特性

### 用户功能
- **AI 对话生成应用**：通过自然语言描述创建网站和应用
- **实时预览**：生成过程中实时查看应用效果
- **应用部署**：一键部署应用到可访问的 URL
- **应用管理**：查看、编辑、删除自己的应用
- **分页浏览**：支持分页查看我的应用和精选应用
- **搜索功能**：根据应用名称搜索应用

### 管理员功能
- **应用管理**：管理所有用户创建的应用
- **应用编辑**：编辑任意应用的信息（名称、封面、优先级）
- **应用删除**：删除任意应用
- **精选设置**：设置应用为精选（优先级设为99）
- **用户管理**：管理平台用户

## 页面说明

### 1. 主页 (HomeView)
- 网站标题和副标题
- 用户提示词输入框
- 建议分类按钮
- 我的应用分页列表（仅登录用户可见）
- 精选应用分页列表

### 2. 应用生成对话页 (AppChatPage)
- 顶部栏：应用名称和部署按钮
- 左侧对话区域：与 AI 的对话历史
- 右侧网页展示区域：实时预览生成的应用
- 支持 SSE 流式对话

### 3. 应用管理页 (AppManagePage)
- 管理员专用页面
- 应用列表表格展示
- 搜索和筛选功能
- 编辑、删除、精选操作

### 4. 应用信息修改页 (AppEditPage)
- 用户和管理员都可以访问
- 普通用户只能编辑自己的应用
- 管理员可以编辑任意应用
- 支持修改应用名称和封面

## 技术栈

- **前端框架**：Vue 3 + TypeScript
- **路由管理**：Vue Router
- **状态管理**：Pinia
- **UI 组件**：Ant Design Vue
- **HTTP 请求**：Axios
- **构建工具**：Vite

## 项目结构

```
src/
├── api/                    # API 接口
│   ├── appController.ts    # 应用相关接口
│   ├── userController.ts   # 用户相关接口
│   └── typings.d.ts       # TypeScript 类型定义
├── components/             # 公共组件
│   ├── GlobalHeader.vue   # 全局头部
│   └── GlobalFooter.vue   # 全局底部
├── layouts/               # 布局组件
│   └── BasicLayout.vue    # 基础布局
├── pages/                 # 页面组件
│   ├── HomeView.vue       # 主页
│   ├── admin/             # 管理员页面
│   │   ├── UserManagePage.vue
│   │   └── AppManagePage.vue
│   ├── app/               # 应用相关页面
│   │   ├── AppChatPage.vue
│   │   └── AppEditPage.vue
│   └── user/              # 用户页面
│       ├── UserLoginPage.vue
│       └── UserRegisterPage.vue
├── router/                # 路由配置
│   └── index.ts
├── stores/                # 状态管理
│   ├── counter.ts
│   └── loginUser.ts
└── main.ts               # 应用入口
```

## 路由配置

- `/` - 主页
- `/user/login` - 用户登录
- `/user/register` - 用户注册
- `/admin/user` - 用户管理（管理员）
- `/admin/app` - 应用管理（管理员）
- `/app/chat/:appId` - 应用对话页
- `/app/edit/:appId` - 应用编辑页

## 权限控制

- **普通用户**：可以创建、编辑、删除自己的应用
- **管理员**：可以管理所有应用和用户

## 开发说明

1. 安装依赖：
```bash
npm install
```

2. 启动开发服务器：
```bash
npm run dev
```

3. 构建生产版本：
```bash
npm run build
```

## 环境变量

- `VITE_API_BASE_URL`：API 基础 URL（可选）

## 注意事项

1. 应用生成需要后端支持 SSE（Server-Sent Events）
2. 应用预览需要后端提供静态资源服务
3. 部署功能需要后端支持应用部署
4. 用户权限验证通过路由守卫实现

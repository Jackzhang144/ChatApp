# ChatApp 技术学习文档

本文聚焦于 ChatApp 的前后端实现细节，帮助你快速理解整体架构、关键代码路径以及本地运行与扩展方式。项目位于 `ChatApp-java`（Spring Boot 服务端）与 `ChatApp-front`（Electron + Vue 桌面端）两个模块。

---

## 1. 总体架构

ChatApp 采用「服务端 + 桌面端」的典型即时通讯方案：

| 组件 | 说明 |
| --- | --- |
| Spring Boot Web 应用 (`ChatApp-java`) | 暴露 REST API、WebSocket 接入、文件/资源管理，并通过 Redis 做会话/Token/限流辅助。 |
| Netty WebSocket 服务 (`NettyWebSocketStarter`) | 负责长连接、消息推送与心跳检测，伴随 Spring Boot 进程启动。 |
| Electron 主进程 (`ChatApp-front/src/main`) | 管理窗口、托盘、IPC 通道、本地缓存(SQLite)、WebSocket 客户端、文件下载/生成。 |
| Vue3 渲染进程 (`ChatApp-front/src/renderer`) | 负责 UI、业务交互、调用 REST 接口（通过 Axios）以及和主进程 IPC 联动。 |
| MySQL + Redis | MySQL 存储账号、群组、消息等结构化数据；Redis 缓存登录状态、系统参数与在线会话。 |

主要交互流程：

1. 用户通过桌面端注册/登录，服务端验证验证码后返回 `token`、账号资料、系统配置（`AccountController`）。
2. Electron 主进程持久化 `token` 并建立 WebSocket 连接 (`wsClient.js`)，将初始会话/消息落盘到本地 SQLite 以支撑离线浏览。
3. 用户发送文本/媒体消息时，渲染进程先调用 REST (`/chat/sendMessage`)，如伴随文件则通过主进程写入本地并回传服务端 (`file.js` + `/chat/uploadFile`)。
4. 服务端 `ChatMessageServiceImpl` 将消息写入 MySQL、更新会话状态、借助 `MessageHandler` 通过 Netty 推送至目标客户端；Redis 同时缓存必要的会话/联系人数据。
5. 客户端收到推送后更新本地数据库和 UI，必要时触发提醒、托盘提示或窗体闪烁。

---

## 2. 后端（ChatApp-java）

### 2.1 技术栈与项目结构

- **框架**：Spring Boot 3.2、MyBatis、Spring Data Redis、Lettuce 连接池、Lombok。
- **协议**：REST + JSON（HTTP 5050 端口）、Netty WebSocket（5051 端口）。
- **Persistence**：MySQL（`db/ChatApp.sql` 初始化脚本）、Redis（Token、系统设置、会话缓存）。
- 模块划分：
  - `controller`：负责 REST API，使用 `@GlobalInterceptor` 做登录/管理员校验。
  - `service`/`impl`：封装业务逻辑，组合 Mapper、Redis、WebSocket 推送。
  - `mappers`：MyBatis 映射接口，生成 CRUD。
  - `entity`：PO/DTO/VO/枚举等，集中在 `entity/po|vo|dto|enums`。
  - `redis`：`RedisConfig`、`RedisUtils`、`RedisComponent` 等基础设施。
  - `websocket`：Netty 启动器与消息分发器，向在线用户推送消息。

### 2.2 配置与启动

- `application.yml`（`ChatApp-java/src/main/resources`）定义 HTTP/WebSocket 端口、MySQL、Redis、上传大小限制、`project.folder`（文件存储目录）、`admin.emails`（默认管理员邮箱）等。
- `AppConfig`（`entity/config/AppConfig.java`）在运行时注入这些配置，提供 `getProjectFolder()` 等工具方法。
- `InitRun`（`InitRun.java`）实现 `ApplicationRunner`：启动前探测 MySQL/Redis 连通性，并在独立线程启动 Netty WebSocket 服务。
- 启动命令：
  ```bash
  mvn -pl ChatApp-java -am spring-boot:run
  # or 打包后运行
  mvn -pl ChatApp-java -am clean package
  java -jar ChatApp-java/target/ChatApp-java-1.0.jar
  ```

### 2.3 认证与全局拦截

- 所有需要鉴权的接口标注 `@GlobalInterceptor`（`annotation/GlobalInterceptor.java`），默认校验登录。
- `GlobalOperationAspect` Before 通知读取请求头 token，经 `RedisUtils` 校验 `Constants.REDIS_KEY_WS_TOKEN + token`，可额外校验管理员权限。
- `ABaseController` 提供 `getTokenUserInfo()` / `resetTokenUserInfo()` 便于 Controller 层复用 token 信息、刷新过期时间。
- Token 与用户 ID 映射也会写入 Redis（`Constants.REDIS_KEY_WS_TOKEN_USERID`），用于互踢逻辑、离线通知等。

### 2.4 核心业务流程

#### 账号/验证码

- `AccountController` 提供验证码生成 (`/account/checkCode`)、注册、登录、系统配置查询。
- 验证码依赖 `easy-captcha`，在 JDK 21 环境通过 `nashorn-core` 兼容 ArithmeticCaptcha。

#### 联系人 & 群组

- `UserContactController`, `GroupController`, `AdminGroupController` 等处理好友申请、群聊 CRUD、群成员操作。
- 服务端使用 `UserContactTypeEnum` 区分单聊/群聊 ID 前缀，`StringTools` 负责拼接/截断。
- Redis 中缓存联系人列表（`RedisComponent.getUserContactList`）以加速 `ChatMessageServiceImpl.saveMessage` 的联系人权限校验。

#### 消息流转

1. `ChatController.sendMessage` 校验消息类型 (`MessageTypeEnum`)，创建 `ChatMessage`。
2. `ChatMessageServiceImpl.saveMessage` 核心步骤：
   - 依据联系人类型计算会话 ID (`StringTools.getChatSessionId4User/Group`)。
   - 写入 `chat_message`，更新/插入 `chat_session` 与 `chat_session_user`。
   - 构造 `MessageSendDto`，通过 `MessageHandler` 推送给目标用户的 WebSocket 连接。
   - 文件消息（`MEDIA_CHAT`）初始状态为 `SENDING`，待客户端上传完文件后再转为 `SENDED`。
3. `ChatController.uploadFile` 接受 Multipart 文件 + 缩略图，委托 `ChatMessageService.saveMessageFile` 将文件写入 `project.folder` 对应目录，并更新消息状态。
4. 下载接口 `downloadFile` 复用 `FileDownloadUtils` 将文件流写入响应，支持封面/原文件切换。

#### 系统/管理员能力

- `Admin*Controller` 覆盖用户、靓号、群组、系统设置、客户端版本五大后台模块。管理员账号通过配置邮箱判定。
- `AdminAppUpdateController` 结合前端 `Update` 视图实现桌面端更新策略（灰度/全量、外链/本地资源）。

### 2.5 数据模型

初始化脚本位于 `db/ChatApp.sql`，主要表：

- `user_info`：账号基本信息、邮箱、密码（MD5）、状态、登录/离线时间。
- `user_contact`：好友关系及申请状态。
- `group_info` / `group_member`：群组及成员。
- `chat_message` / `chat_session` / `chat_session_user`：消息与会话快照。
- `app_update`：客户端版本管理。
- `user_info_beauty`：靓号分配。

配合 Redis 实现的高频结构：

- `Constants.REDIS_KEY_WS_TOKEN{token}`：`TokenUserInfoDto` 序列化。
- `Constants.REDIS_KEY_WS_TOKEN_USERID{uid}`：token 反向索引，便于强制下线。
- `Constants.REDIS_KEY_SYS_SETTING`：`SysSettingDto` 缓存（水印、允许注册开关等）。
- `RedisComponent` 还缓存联系人 ID 列表、群成员、消息未读计数等。

### 2.6 扩展建议

- **消息类型扩展**：`MessageTypeEnum` 中新增类型 -> 更新前端 `wsClient`、`ChatMessageServiceImpl` 以及 UI 展示模板。
- **文件存储**：若迁移到对象存储，可改造 `ChatMessageService.saveMessageFile` 与 `FileDownloadUtils`，并更新前端下载逻辑。
- **监控/日志**：`logging.level.root=debug` 仅适用于本地；生产可调整为 `info` 并接入 ELK。

---

## 3. 桌面端（ChatApp-front）

### 3.1 技术栈与目录

- **主进程**：Electron 28、express（本地静态服务）、ws（WebSocket 客户端）、sqlite3（本地缓存）、electron-store（KV 持久化）、fluent-ffmpeg（缩略图/转码）。
- **渲染进程**：Vue 3.5、Vite 4、Pinia、Vue Router、Element Plus、Axios、DPlayer、v-viewer、moment。
- **目录结构**：
  - `src/main`：主进程入口 (`index.js`)、IPC 定义 (`ipc.js`)、本地数据库 (`db/*.js`)、文件工具 (`file.js`)、WebSocket 客户端 (`wsClient.js`)、窗口注册 (`windowProxy.js`)。
  - `src/renderer`：Vue 应用（`App.vue`、`router`、`stores`、`views/*`）、全局组件与样式。
  - `src/preload`：暴露 `ipcRenderer`/Electron API。
  - `assets/ffmpeg.exe|ffprobe.exe`：Windows 自带二进制，macOS/Linux 需自备。

### 3.2 主进程职责

#### 3.2.1 窗口与托盘（`src/main/index.js`）

- 启动时创建登录窗口（300×370），登陆成功后调整到聊天尺寸（850×800），并根据用户身份动态扩展托盘菜单（例如「管理后台」入口）。
- macOS 定制化：标准标题栏、traffic light 位置、Template 图标。
- 托盘图标点击时恢复窗口；关闭窗口默认隐藏到托盘（非真正退出）。

#### 3.2.2 IPC 通道（`src/main/ipc.js`）

- 统一在 `index.js` 中注册：登录/注册模式切换、会话加载、好友申请计数、消息本地写入、缩略图生成、系统设置、文件目录管理等。
- 每个 `ipcMain.on` 对应渲染层调用（通过 `window.ipcRenderer`），如 `loadChatMessage`、`setSessionSelect`、`saveClipBoardFile`、`downloadUpdate` 等。
- 登录成功后初始化：
  - Electron Store 中写入 `token`/用户配置 (`store.js`)。
  - 本地 SQLite 表结构（`db/Tables.js`）通过 `ADB.createTable` 保证存在。
  - WebSocket 长连接 (`wsClient.js`)，首次回调写入会话、消息缓存。

#### 3.2.3 本地数据库与缓存

- SQLite 初始化路径：`~/.chatapp/` 或 `~/.chatappdev/`（`db/ADB.js`），并维护 `add_tables`/`add_indexes`/`alter_tables` 的 schema migration。
- `ChatMessageModel`、`ChatSessionUserModel`、`UserSetting` 封装查询/更新，确保渲染层可异步读取历史消息、会话列表、未读计数。
- Electron Store (`store.js`) 负责保存与用户绑定的 KV：`{userId}{key}` 命名，典型数据有 token、当前选中会话、服务器域名、本地文件夹路径等。

#### 3.2.4 WebSocket 客户端（`src/main/wsClient.js`）

- 使用 `ws` 包直连服务端（`devWsDomain` / `prodWsDomain` 存于本地配置）。
- 连接成功后发送心跳、缓存剩余重连次数（默认 5 次，5s 重试）。
- `onmessage` 根据 `messageType` 分发：初始化数据（0）、好友申请（4）、文件上传完成（6）、群事件（9/11/12）、被踢下线（7）、常规聊天消息（2/5）。
- 收到消息后：更新 session 快照 (`saveOrUpdate4Message`)、写入 SQLite、推送给渲染层（`sender.send("reciveMessage", message)`），若窗口未聚焦则 `flashFrame(true)`。

#### 3.2.5 文件处理与本地服务（`src/main/file.js`）

- 负责将选取的文件复制到本地、生成缩略图（ffmpeg/ffprobe）、保存聊天附件、头像裁切等。
- 提供 Express 本地服务以供渲染层访问缓存文件；同时支持更换/打开文件夹、保存剪切板文件等操作。
- 文件上传流转：渲染层 -> `ipcRenderer.send("addLocalMessage")` -> 主进程复制/压缩/生成封面 -> `axios` POST `/api/chat/uploadFile` -> 更新消息状态。

### 3.3 渲染进程职责

#### 3.3.1 应用初始化 (`renderer/src/main.js`)

- 注册 Pinia、Router、Element Plus、全局组件（WinOp、Avatar、Dialog 等）与工具（Verify、Utils、Request、Message、Confirm、Api）。
- 自定义样式：`cust-elementplus.css`、`base.css`、图标字体。

#### 3.3.2 路由与视图 (`renderer/src/router/index.js`)

- 顶层路由包括：
  - `/login`：登录/注册切换页。
  - `/main`：主布局，嵌套 `chat`、`contact`、`setting` 子路由。
  - `/admin`：管理员后台（用户、靓号、群组、系统设置、版本管理）。
  - `/showMedia`：媒体预览弹窗。
- 每个子模块进一步细分，如联系人内的搜索/好友申请/群详情等，配合 Pinia Store 控制状态。

#### 3.3.3 状态管理 (`src/renderer/src/stores`)

- 典型 Store：
  - `GlobalInfoStore`：登录态、窗口信息、未读数等。
  - `UserInfoStore`：当前用户资料。
  - `ContactStateStore`：联系人/群聊选中状态。
  - `MessageCountStore`：会话未读统计。
  - `SysSettingStore`：水印、最大群成员、是否开放注册等系统参数。
  - `AvatarUpdateStore`：换头像过程中的临时数据、裁剪结果等。

#### 3.3.4 网络层 (`utils/Request.js` + `utils/Api.js`)

- 统一封装 Axios：POST + FormData 为主，支持 `dataType=json`、`responseType` 切换、多国语 loading。
- 请求拦截器负责展示 Loading、注入 `token`，响应拦截器统一处理错误码（901 触发 `ipcRenderer` 的 `reLogin`）。
- `Api.js` 统一管理后端路径，配合 `Request` 模块在视图中调用。

#### 3.3.5 IPC / WebSocket 结合

- 渲染层通过 `window.ipcRenderer` 调用主进程能力，例如：
  - `ipcRenderer.send("loadChatMessage", payload)` -> 主进程读 SQLite -> `loadChatMessage` 回调。
  - `ipcRenderer.invoke("createCover", filePath)` -> ffmpeg 导出封面 -> 回写流。
  - `ipcRenderer.send("downloadUpdate", { id, fileName })` -> Electron 自动更新。
- WebSocket 事件 `reciveMessage` 由主进程广播到渲染层，UI 根据 `messageType` 刷新聊天、联系人、提示窗。

#### 3.3.6 UI 组件与交互

- 聊天模块（`views/chat/Chat.vue`）组合消息列表、输入框、会话侧边栏，支持右键菜单、拖拽上传、粘贴截图。
- 联系人模块包含搜索（邮箱 ID）、好友申请、群管理（创建、编辑、成员列表）。
- 设置模块支持个人资料修改、文件目录管理（调用主进程更换存储位置）、关于页展示版本信息。
- 管理员后台重用 Element Plus 表格、分页，直接调用管理端 REST 接口。

### 3.4 构建与调试

```bash
cd ChatApp-front
npm install
npm run dev        # Electron + Vite 热加载
npm run build      # 打包 renderer 产物
npm run build:mac  # electron-builder 生成对应安装包，win/linux 对应 build:win/build:linux
```

- `electron-vite` 在开发模式会启动两个进程（主进程 + Vite Renderer），`npm run dev` 前请保证后端 API 可达。
- 构建阶段 `electron-builder` 会将 `assets/**` 作为额外资源打包，确保 Windows 附带 ffmpeg/ffprobe。
- 如果需要调试主进程，可使用 `--inspect=5858`（在 `package.json` 的 `dev` 脚本中已配置）。

### 3.5 常见问题与排查

1. **WebSocket 无法连接**：检查 `store` 中的 `devWsDomain/prodWsDomain` 是否正确，确保 5051 端口可访问。
2. **文件缩略图失败**：macOS/Linux 必须安装系统级 `ffmpeg`/`ffprobe`，并保证 PATH 可找到。
3. **SQLite 表缺失**：首次运行若 `.chatapp/` 权限不足会导致创建失败，可在 `db/ADB.js` 中查看日志并手动清理。
4. **登录超时频繁**：后端 Redis TTL 由 `Constants.REDIS_KEY_TOKEN_EXPIRES` 控制，注意客户端是否在后台运行导致 token 过期。

---

## 4. 运行与学习建议

1. **准备环境**：MySQL 8 + Redis 6/7 + JDK 21 + Node.js 18；执行 `db/ChatApp.sql` 初始化数据库。
2. **配置**：修改 `ChatApp-java/src/main/resources/application.yml` 的数据库/Redis/`project.folder`；前端在登录界面点击设置填写后端域名与 WebSocket 地址（会保存到 Electron Store）。
3. **联调顺序**：先启动后端，再运行前端；首次登录可在 `db/ChatApp.sql` 中查看示例账号或自行注册（若允许注册）。
4. **阅读代码路径**：建议从 Controller -> Service -> Mapper 顺序阅读后端；前端则先理解主进程 `ipc.js`、`wsClient.js`，再查看渲染层 Router/Store/Views。
5. **扩展实践**：尝试新增消息类型（如语音），或将文件存储切换至对象存储，或编写脚本批量导入靓号，巩固对现有架构的理解。

---

通过本指南，你可以从「整体架构 → 后端链路 → 前端链路 → 运行/排错」的视角逐步掌握 ChatApp 的实现原理。更多细节可参考对应源码文件并结合 README.md 中的运行说明。祝学习顺利！

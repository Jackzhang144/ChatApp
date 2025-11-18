# ChatApp

ChatApp 是一个包含桌面端与服务端的即时通讯项目：后端提供账号、联系人、群聊、文件与通知等能力，Electron 客户端负责在 Windows、macOS 与 Linux 桌面环境中提供类原生体验。

## 项目概览

- **后端服务（ChatApp-java）**：Spring Boot + MyBatis + Redis + MySQL，暴露 REST + WebSocket 接口，处理账号鉴权、双端同步、消息路由和上传文件的持久化。
- **桌面客户端（ChatApp-front）**：Electron + Vue 3 + Vite，集成 Element Plus 组件库，提供用户登录、联系人管理、单聊/群聊、设置以及客户端自更新能力。
- **支撑设施**：Redis 用于存储会话状态与缓存，MySQL 存储核心业务数据，`folder/` 目录用来落地上传资源、日志和构建产物。

## 主要特性

- 账号注册/登录、联系人搜索与申请、群聊/群管理以及黑名单等常规 IM 功能。
- 消息通过 WebSocket 实时投递，离线状态借助 Redis 与数据库保证补发。
- 支持图片、文件等上传，服务端通过 `project.folder` 约定存储路径。
- 内置管理员视图，用于管理用户资料、群组与客户端版本。
- Electron 应用封装 Vite 渲染进程，可通过脚本快速打包成多个平台的安装包。

## 仓库结构

```text
.
├── pom.xml                     # 根 POM，聚合并暴露 ChatApp-java 模块
├── ChatApp-java/              # Spring Boot 服务端
│   ├── src/main/java/com/chatapp
│   └── src/main/resources/
├── ChatApp-front/             # Electron + Vue 客户端
│   ├── src/
│   ├── package.json
│   └── electron-vite 配置
├── db/ChatApp.sql                 # 初始化 MySQL 的建表与示例数据脚本
├── folder/                     # 运行期产生的文件、日志目录（已在 .gitignore 中忽略）
└── .gitignore
```

## 环境要求

- **JDK 21**
- Maven 3.9+
- Node.js 18+ 与 npm（Electron/Vite 构建依赖）
- MySQL 8.x
- Redis 6.x/7.x
- 可选：`pnpm`/`yarn`、`docker`、`ffmpeg`/`ffprobe`

## 数据库初始化

1. 创建数据库与基础数据：
   ```bash
   mysql -u <user> -p < db/ChatApp.sql
   ```
2. 如需修改库名或凭据，先调整 `db/ChatApp.sql` 与 `ChatApp-java/src/main/resources/application.yml` 中的 `spring.datasource.*`。

## 后端服务（ChatApp-java）

- 核心栈：**Spring Boot 3.2 + MyBatis + Redis (Lettuce) + MySQL**，配合 Lombok 简化实体对象维护。
- `application.yml` 中配置 web 端口（5050）、WebSocket 端口（5051）、MySQL、Redis 连接以及 `project.folder`（默认 `folder/`）。
- Multipart 上传默认 15 MB，可按需调整；验证码依赖 `easy-captcha` 与 `nashorn-core`。
- 启动方式：
  ```bash
  mvn -pl ChatApp-java -am spring-boot:run   # 热启动
  mvn -pl ChatApp-java -am clean package
  java -jar ChatApp-java/target/ChatApp-java-1.0.jar
  ```
- 常用命令：`mvn -pl ChatApp-java -q validate`、`mvn -pl ChatApp-java test`。

## 桌面客户端（ChatApp-front）

- 核心栈：**Electron 28 + Vue 3.5 + Vite 4 + Element Plus 2.11**，配合 Pinia、Vue Router 构建多视图应用。
- 项目内置 Windows 版 FFmpeg/FFprobe，可自备 macOS/Linux 版本确保视频转码与截图等能力可用。
- 安装依赖与调试：
  ```bash
  cd ChatApp-front
  npm install
  npm run dev      # 启动 Electron + Vite
  ```
- 构建脚本：
  ```bash
  npm run build
  npm run build:mac   # 其他平台见 package.json 中的 build:* 脚本
  ```
- `src/renderer/src/assets/cust-elementplus.css` 等文件覆盖 Element Plus 主题；`Update` 视图与 `electron-builder` 配置配合实现客户端自更新。

## 开发流程建议

1. 启动 MySQL 与 Redis，并导入 `db/ChatApp.sql`。
2. 配置 `ChatApp-java/src/main/resources/application.yml`，保证数据库、Redis 以及 `project.folder` 指向可写路径。
3. 在根目录运行 `mvn -pl ChatApp-java -am spring-boot:run` 启动服务端。
4. 进入 `ChatApp-front` 执行 `npm run dev`，打开桌面客户端进行调试。
5. 需要产物时，通过 `npm run build:<platform>` 与 `mvn package` 生成安装包与后端发布包。

## 其他说明

- `.gitignore` 已忽略常见的 IDE、Maven、Electron 构建产物以及运行期数据，若需要提交某些目录（例如 `folder/`）请在其中放置 `.gitkeep` 并更新忽略策略。
- `project.folder` 指向的目录会存储上传的头像、文件以及日志，如部署到服务器请调整到合适的挂载点，并确保具备可写权限。
- 默认管理员邮箱由 `application.yml` 的 `admin.emails` 控制，可在初始化脚本中一并修改。

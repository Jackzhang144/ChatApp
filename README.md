# EasyChat

EasyChat 是一个包含桌面端与服务端的即时通讯项目。后端基于 **Spring Boot + MyBatis + Redis + MySQL**，前端则采用 **Electron + Vue3 + Vite**，可以在桌面环境中运行，支持常见的账号注册、联系人、群聊等能力。

## 仓库结构

```text
.
├── pom.xml                     # 根 POM，聚合并暴露 easychat-java 模块
├── easychat-java/              # Spring Boot 服务端
│   ├── src/main/java/com/easychat
│   └── src/main/resources/
├── easychat-front/             # Electron + Vue 客户端
│   ├── src/
│   ├── package.json
│   └── electron-vite 配置
├── db/init.sql                 # 初始化 MySQL 的建表与示例数据脚本
├── folder/                     # 运行期产生的文件、日志目录（已在 .gitignore 中忽略）
└── .gitignore
```

## 环境要求

- JDK 1.8+
- Maven 3.6+
- Node.js 18+（Electron 依赖）及 npm
- MySQL 8.x
- Redis 5.x/6.x
- 可选：`pnpm`/`yarn`、`docker` 等工具

## 数据库初始化

1. 创建数据库与基础数据：
   ```bash
   mysql -u <user> -p < db/init.sql
   ```
2. 如需修改库名或凭据，先调整 `db/init.sql` 与 `easychat-java/src/main/resources/application.yml` 中的 `spring.datasource.*`。

## 后端（easychat-java）

- 配置：
  - `application.yml` 中包含 web 端口（5050）、WebSocket 端口（5051）、MySQL、Redis 连接以及 `project.folder`（用于存储上传文件与日志，默认指向仓库下的 `folder/`，首次运行请确保该路径存在且可写）。
- 启动：
  ```bash
  # 在 repo 根目录
  mvn -pl easychat-java -am spring-boot:run
  # 或者打包运行
  mvn -pl easychat-java -am clean package
  java -jar easychat-java/target/easychat-java-1.0.jar
  ```
- 常用命令：
  - `mvn -pl easychat-java -q validate`：快速校验依赖。
  - `mvn -pl easychat-java test`：执行服务端单元测试（如有）。

## 前端（easychat-front）

- 依赖安装：
  ```bash
  cd easychat-front
  npm install
  ```
- 开发调试（默认会启动 Electron + Vite dev server）：
  ```bash
  npm run dev
  ```
- 构建：
  ```bash
  npm run build          # 生成 electron-vite 产物
  npm run build:mac      # 其它平台见 package.json 中的 build:* 脚本
  ```
- 运行桌面端前，保证后端服务已启动并且能访问 MySQL、Redis。

## 开发流程建议

1. 启动 MySQL 与 Redis，并导入 `db/init.sql`。
2. 在 `easychat-java` 中更新 `application.yml`，确保数据库、Redis、文件存储路径与本地一致。
3. 使用 `mvn -pl easychat-java -am spring-boot:run` 启动服务端。
4. 另开终端进入 `easychat-front`，执行 `npm run dev` 或 `npm run start` 以启动 Electron 预览。
5. 如需产物，使用 `npm run build:<platform>` 和 `mvn package`。

## 其他说明

- `.gitignore` 已忽略常见的 IDE、Maven、Electron 构建产物以及运行期数据，若需要提交某些目录（例如 `folder/`）请在其中放置 `.gitkeep` 并更新忽略策略。
- `project.folder` 指向的目录会存储上传的头像、文件以及日志，如部署到服务器请调整到合适的挂载点，并确保具备可写权限。
- 默认管理员邮箱由 `application.yml` 的 `admin.emails` 控制，可在初始化脚本中一并修改。

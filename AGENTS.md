# AGENTS.md

plugin-uni-halo：Halo 2.26 插件（Java 21 + Gradle 9.4 wrapper），前端为独立 Gradle 子工程 `ui`（Vue 3.5 + Vite 8 + TypeScript + pnpm 10）。当前处于「架构升级」后的空脚手架状态：后端仅有 `UniHaloPlugin`（继承 `BasePlugin`，生命周期打印日志），UI 仅有示例页；业务（Scheme/Endpoint/业务视图）尚未迁入，详见 `.docs/` 下的迁移规划文档。

## 常用命令

- 完整构建（含 UI）：`./gradlew build` —— 根工程 `classes` 依赖 `processUiResources`，会自动触发 `:ui:assemble`（pnpm build）并把 `ui/build/dist` 拷入 `build/resources/main/ui`，因此**构建必须联网装 pnpm 依赖**（Node ^20.19 或 >=22.12，pnpm 10，CI 用 Node 24）。
- 启动内置 Halo 调试环境：`./gradlew haloServer`（来自 `run.halo.plugin.devtools` 插件）。
- 后端测试：`./gradlew test`（JUnit 5 + Mockito，见 `src/test/java`）。
- UI 单独操作：`cd ui && pnpm install && pnpm dev`。注意 `dev` 是 `vite build --watch --mode=development`（监听重建，不是开发服务器）。
- UI 校验：`pnpm type-check`（vue-tsc）、`pnpm test:unit`（vitest）、`./gradlew :ui:pnpmCheck`（check 任务自动跑）。`pnpm lint` = oxlint + eslint，两者均带 `--fix`，**会直接改写文件**；格式化用 `pnpm format`（prettier）。

## 关键约定与坑

- 后端 `compileOnly 'run.halo.app:api'`（BOM `run.halo.tools.platform:plugin:2.26.0`）：依赖 Halo API 编译即可，禁止打进插件。
- 根包 `cn.ialley.unihalo`；插件清单 `src/main/resources/plugin.yaml` 的 `metadata.name: uni-halo`（与工程名 `plugin-uni-halo` 不同），`spec.requires: ">=2.26.0"`。
- `generatePluginComponentsIdx` 与配置缓存不兼容（已在 build.gradle 声明 `notCompatibleWithConfigurationCache`），不要尝试启用 configuration cache。
- `.editorconfig`：Java/Gradle 缩进 4 空格、前端 2 空格，LF 行尾；Java 与前端 max_line_length 100。UTF-8。
- `.docs/` 是规划/历史文档，其中提到的 `Constants.java`、`endpoints/`、`ui/src/api/` 等**目标态文件当前不存在**；API 域名（`uni.uhalo.pro` vs `unihalo.ialley.cn`）尚未定案，写新 API 前先确认。
- `workplace/` 为空置目录，勿放代码。
- UI 入口 `ui/src/index.ts` 用 `@halo-dev/ui-shared` 的 `definePlugin` 注册路由/扩展点，`@` 别名指向 `./src`。

## 维护规则

当项目结构、构建/测试命令、架构边界、开发约定或本文档记录的其他事实发生变化时，须在同一改动中同步更新本文件。

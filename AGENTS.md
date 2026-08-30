# AGENTS.md

plugin-uni-halo：Halo 2.26 插件（Java 21 + Gradle 9.4 wrapper），前端为独立 Gradle 子工程 `ui`（Vue 3.5 + Vite 8 + TypeScript + pnpm 10）。已从 1.x 仓库迁入完整业务（应用管理 / 版本管理 / 二维码 / 配置接口），API 分组域名采用 `unihalo.ialley.cn`（`BASIC_DOMAIN_NAME` 为唯一源头，GVK 已切换，历史数据需按 `.docs/api-domain-change-plan-a.md` 第 5 节迁移）。

## 常用命令

- 完整构建（含 UI）：`./gradlew build` —— 根工程 `classes` 依赖 `processUiResources`，会自动触发 `:ui:assemble`（pnpm build）并把 `ui/build/dist` 拷入 `build/resources/main/ui`，因此**构建必须联网装 pnpm 依赖**（Node ^20.19 或 >=22.12，pnpm 10，CI 用 Node 24）。
- 启动内置 Halo 调试环境：`./gradlew haloServer`（来自 `run.halo.plugin.devtools` 插件）。
- 后端测试：`./gradlew test`（JUnit 5 + Mockito，见 `src/test/java`）。
- UI 单独操作：`cd ui && pnpm install && pnpm dev`。注意 `dev` 是 `vite build --watch --mode=development`（监听重建，不是开发服务器）。
- UI 校验：`pnpm type-check`（vue-tsc）、`pnpm test:unit`（vitest）、`./gradlew :ui:pnpmCheck`（check 任务自动跑）。`pnpm lint` = oxlint + eslint，两者均带 `--fix`，**会直接改写文件**；格式化用 `pnpm format`（prettier）。

## 关键约定与坑

- 后端 `compileOnly 'run.halo.app:api'`（BOM `run.halo.tools.platform:plugin:2.26.0`）：依赖 Halo API 编译即可，禁止打进插件。
- 根包 `cn.ialley.unihalo`；插件清单 `src/main/resources/plugin.yaml` 的 `metadata.name: uni-halo`（与工程名 `plugin-uni-halo` 不同），`spec.requires: ">=2.26.0"`，已追加 `settingName` / `configMapName` 业务字段。
- **API 分组**：公开 `api.unihalo.ialley.cn/v1alpha1`，控制台 `console.api.unihalo.ialley.cn/v1alpha1`。`Constants.BASIC_DOMAIN_NAME` 是唯一源头；新增公开 GET 接口必须同步 `extensions/role-anonymous.yaml` 的 `rules`，否则 403。
- **索引与查询 API（Halo 2.22+ 规范）**：Scheme 索引一律用 `IndexSpecs.single(name, keyType).indexFunc(...)`（keyType 实现 `Comparable`，如 `Boolean.class`），不要用已弃用的 `new IndexSpec()` / `IndexAttributeFactory`；字段查询用 `Queries`（`and/equal/contains/or/not`，`and(Condition, Condition...)` 首条件+可变参）配合 `ListOptions.builder().fieldQuery(...)`，不要用已弃用的 `QueryFactory` / `FieldSelector.of`。`ReactiveSettingFetcher` 用 `getSettingValues()` / `getSettingValue()`（Jackson 3：`tools.jackson.databind.JsonNode`，不是 `com.fasterxml.jackson`）。
- UI 业务架构参考 `plugin-vote`（`D:\HaloWorkspace\uni-halo\community\reference-projects\plugin-vote\ui`）：views 内直接使用 `@tanstack/vue-query`（`useQuery`/`useMutation`，**无 composables 目录**）、列表用 `VEntityContainer` + `VEntity` + `VEntityField`、表单用 VModal 弹窗 + SubmitButton（Ctrl/Cmd+Enter 提交）。借鉴 `plugin-links/console`（`D:\HaloWorkspace\uni-halo\community\reference-projects\plugin-links\console`）：**领域类型统一放 `ui/src/types/index.ts`**、纯函数封装在 `ui/src/utils/`、列表筛选用统一组件 `ui/src/components/FilterDropdown.vue`（VDropdown + VDropdownItem，点击已选项清除）。构建保持 Vite，未引入 Rsbuild。
- `generatePluginComponentsIdx` 与配置缓存不兼容（已在 build.gradle 声明 `notCompatibleWithConfigurationCache`），不要尝试启用 configuration cache。
- `.editorconfig`：Java/Gradle 缩进 4 空格、前端 2 空格，LF 行尾；Java 与前端 max_line_length 100。UTF-8。
- `.docs/` 是规划/历史文档：迁移、app 升级设计、API 域名变更方案均已落地实施，文档保留作为决策与执行记录；写新 API 前先确认当前域名常量（`unihalo.ialley.cn`）与 role 放行。
- `workplace/` 为空置目录，勿放代码。
- UI 入口 `ui/src/index.ts` 用 `@halo-dev/ui-shared` 的 `definePlugin` 注册路由/扩展点，`@` 别名指向 `./src`。多页面业务按子目录组织（如 `views/app-manage/`、`views/love-manage/`），菜单用「父路由 + children 原生二级菜单」模式（由 Halo 菜单系统渲染，父路由组件仅含 `<RouterView />`）。**页面/组件内部 import 优先使用 `@` 别名**（`@/api`、`@/types`、`@/components/...`），不用相对路径。

## 维护规则

当项目结构、构建/测试命令、架构边界、开发约定或本文档记录的其他事实发生变化时，须在同一改动中同步更新本文件。

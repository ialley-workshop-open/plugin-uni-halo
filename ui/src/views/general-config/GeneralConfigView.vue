<script setup lang="ts">
import {Toast, VButton, VCard, VPageHeader, VSpace, VStatusDot, VSwitch, VTabbar} from "@halo-dev/components";
import {useQuery, useQueryClient} from "@tanstack/vue-query";
import {cloneDeep} from "lodash-es";
import {computed, nextTick, onUnmounted, ref, watch} from "vue";
import {VueDraggable} from "vue-draggable-plus";
import RiDragMove2Line from "~icons/ri/drag-move-2-line";
import RiDeleteBinLine from "~icons/ri/delete-bin-6-line";
import SubmitButton from "@/components/button/SubmitButton.vue";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import AuditCandidatesModal from "@/components/audit-config/AuditCandidatesModal.vue";
import {generalConfigApi} from "@/api";
import type {
  AuditDataRef,
  GeneralConfig,
  GeneralConfigCategoryItem,
  GeneralConfigLove,
  GeneralConfigQuickNavigationItem,
  GeneralConfigSpec,
} from "@/types";

const queryClient = useQueryClient();

type BigGroup = "profile" | "preferences" | "pages" | "assets" | "love" | "linkInfo" | "maintenance";

/** 恋爱模块入口（模块入口分区渲染数据源；password 语义见 GeneralConfigLoveModule 类型） */
const LOVE_MODULES: Array<{key: "ourStory" | "lovePhoto" | "loveDaily"; label: string; desc: string}> = [
  {key: "ourStory", label: "恋爱故事", desc: "恋爱页是否展示「恋爱故事」入口"},
  {key: "lovePhoto", label: "恋爱相册", desc: "恋爱页是否展示「恋爱相册」入口"},
  {key: "loveDaily", label: "恋爱清单", desc: "恋爱页是否展示「恋爱清单」入口"},
];

/** 卡片样式选项（与客户端 hermes/preferences.md §3.2 组件 layout 值对齐，旧 lr_ 与 tb_ 值体系废弃） */
const CARD_STYLE_OPTIONS = [
  {label: "上图下文", value: "image_top"},
  {label: "左文右图", value: "image_right"},
  {label: "上文下图", value: "image_bottom"},
  {label: "左图右文", value: "image_left"},
];

/** 清除某模块入口密码（清空新密码输入并标记 passwordRemoved，保存时后端清除） */
function clearModulePassword(key: "ourStory" | "lovePhoto" | "loveDaily") {
  const module = formState.value.spec.love[key];
  if (module) {
    module.password = "";
    module.passwordRemoved = true;
  }
}

/** 新密码输入时自动取消「清除密码」标记（重设优先级高于清除） */
function cancelRemovalOnTyping(module: GeneralConfigLove["ourStory"]) {
  if (module?.passwordRemoved) {
    module.passwordRemoved = false;
  }
}

// ===== 首页分类栏选择（固定 3 个，复用审核配置候选弹窗 AuditCandidatesModal）=====

const categoryModalVisible = ref(false);

/** 首页分类栏已选引用（GeneralConfigCategoryItem：name + displayName） */
const homeCategories = computed(
  () => formState.value.spec.pages.homeConfig.categories || []
);

/** 回显给候选弹窗的 AuditDataRef 形态（title ← displayName） */
const categoryModalSelected = computed<AuditDataRef[]>(() =>
  homeCategories.value.map((item) => ({
    name: item.name || "",
    title: item.displayName,
  }))
);

/** 快捷导航项列表（写回 formState；VueDraggable 需要非 undefined 数组） */
const homeQuickNavigation = computed({
  get: () => formState.value.spec.pages.homeConfig.quickNavigation || [],
  set: (value: GeneralConfigQuickNavigationItem[]) => {
    formState.value.spec.pages.homeConfig.quickNavigation = value;
  },
});

/** 弹窗确认：映射回 {name, displayName} 并关闭（候选弹窗已限制最多 3 个） */
const handleCategoryConfirm = (selected: AuditDataRef[]) => {
  formState.value.spec.pages.homeConfig.categories = selected.map((item) => ({
    name: item.name,
    displayName: item.title || item.name,
  }));
  categoryModalVisible.value = false;
};

const removeCategory = (item: GeneralConfigCategoryItem) => {
  const list = formState.value.spec.pages.homeConfig.categories || [];
  formState.value.spec.pages.homeConfig.categories = list.filter(
    (it) => it.name !== item.name
  );
};

// ===== 快捷导航项（仅维护名称/背景色/显示，key 固定、不可增删，拖拽排序） =====

/** rgba(...) → #rrggbb（取 RGB 部分，供 FormKit color 回显）；hex 原样返回 */
function toHexInput(value?: string): string {
  if (!value) {
    return "#cccccc";
  }
  const match = value.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)/);
  if (match) {
    const toHex = (n: string) => Number(n).toString(16).padStart(2, "0");
    // 正则已保证存在 3 个捕获组（noUncheckedIndexedAccess 下用非空断言）
    return `#${toHex(match[1]!)}${toHex(match[2]!)}${toHex(match[3]!)}`;
  }
  return /^#[0-9a-fA-F]{6}$/.test(value) ? value : "#cccccc";
}

/** FormKit type="color" 选色（hex）写回 bgColor（参考 plugin-announcement editor；客户端 backgroundColor 接受 hex） */
function onNavBgColor(item: GeneralConfigQuickNavigationItem, value: unknown) {
  if (typeof value === "string") {
    item.bgColor = value;
  }
}

const GROUP_ITEMS: Array<{id: BigGroup; label: string; desc: string}> = [
  {id: "profile", label: "应用设置", desc: "应用信息 / 博主 / 社交 / 版权与声明"},
  {id: "preferences", label: "偏好设置", desc: "首页/列表/归档布局与卡片样式"},
  {id: "pages", label: "页面设置", desc: "首页 / 图库 / 关于页"},
  {id: "assets", label: "资源设置", desc: "加载占位"},
  {id: "love", label: "恋爱设置", desc: "恋爱总开关 / 页面图片与模块入口"},
  {id: "linkInfo", label: "链接配置", desc: "小程序信息 / 作者信息"},
  {id: "maintenance", label: "维护设置", desc: "维护页文案 / 排期与开关"},
];

const SUB_TABS: Record<BigGroup, Array<{id: string; label: string}>> = {
  profile: [
    {id: "appInfo", label: "应用信息"},
    {id: "blogger", label: "博主资料"},
    {id: "social", label: "社交信息"},
    {id: "copyright", label: "页脚版权"},
    {id: "disclaimers", label: "免责声明"},
    {id: "about", label: "关于与详情"},
  ],
  preferences: [
    {id: "home", label: "首页"},
    {id: "articles", label: "文章列表"},
    {id: "archives", label: "文章归档"},
  ],
  pages: [
    {id: "home", label: "首页"},
    {id: "gallery", label: "图库页"},
    {id: "categoryPage", label: "分类页"},
    {id: "momentPage", label: "瞬间页"},
    {id: "aboutPage", label: "关于页"},
  ],
  assets: [
    {id: "loading", label: "加载占位"},
  ],
  love: [
    {id: "basic", label: "基本设置"},
    {id: "modules", label: "模块入口"},
  ],
  linkInfo: [
    {id: "info", label: "小程序信息"},
    {id: "author", label: "作者信息"},
  ],
  maintenance: [],
};

const bigGroup = ref<BigGroup>("profile");
const subTab = ref<string>("appInfo");

watch(bigGroup, (group) => {
  const first = SUB_TABS[group][0];
  if (first) {
    subTab.value = first.id;
  }
});

const subTabItems = computed(() => SUB_TABS[bigGroup.value]);

const {data: config, isLoading} = useQuery({
  queryKey: ["uni-halo:general-config"],
  queryFn: () => generalConfigApi.get(),
});

const formState = ref<GeneralConfig>(defaultConfig());

function defaultConfig(): GeneralConfig {
  return {
    metadata: {name: "general-config"},
    spec: defaultSpec(),
  };
}

function defaultSpec(): GeneralConfigSpec {
  return {
    profile: {
      appInfo: {name: "uni-halo", logo: "/plugins/plugin-uni-halo/assets/logo.png"},
      blogger: {nickname: "uni-halo", avatar: "", email: "", description: ""},
      social: {
        enabled: true,
        qq: "",
        wechat: "",
        weibo: "",
        email: "",
        blog: "",
        bilibili: "",
        juejin: "",
        csdn: "",
        gitee: "",
        github: "",
      },
      copyrightConfig: {enabled: true, content: "「 2022 uni-halo 丨 开源项目@小莫唐尼 」"},
      disclaimers: {enabled: true, content: ""},
      showAboutSystem: true,
      postDetailConfig: {
        showComment: true,
        copyrightEnabled: true,
        copyrightAuthor: "uni-halo",
        copyrightDesc:
          "使用《非商业性使用-相同方式共享 4.0 国际 (CC BY-NC-SA 4.0)》协议授权，文章来源于网上收集或者原创，若未在文章内说明的均为原创文章",
        copyrightViolation:
          "若侵害到您的权利，请您及时联系我，在收到通知后第一时间处理，邮箱：xxxx@xx.com",
      },
    },
    pages: {
      homeConfig: {
        // 首页标题保留字段（客户端默认「首页」），控制台不再提供配置项；
        // 轮播渲染参数 2026-09-08 起下线（app 端默认开启）
        pageTitle: "首页",
        useQuickNavigation: true,
        // 快捷导航默认 5 项（对齐客户端 uh-home-quick-nav 默认 navList；bgColor 原 bgGlass、visible 原 show）
        quickNavigation: [
          {key: "archives", title: "文章归档", color: "#03A9F4", bgColor: "rgba(3, 169, 244, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-mask", path: "/pages-blog/archives/archives", visible: true},
          {key: "vote", title: "投票中心", color: "#00BCD4", bgColor: "rgba(0, 188, 212, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-confused", path: "/pages-blog/votes/votes", visible: true},
          {key: "disclaimers", title: "友情链接", color: "#009688", bgColor: "rgba(0, 150, 136, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-wink", path: "/pages-blog/friend-links/friend-links", visible: true},
          {key: "love", title: "恋爱日记", color: "#FF4C67", bgColor: "rgba(255, 76, 103, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-in-love", path: "/pages-blog/love/love", visible: true},
          {key: "contact-blogger", title: "联系博主", color: "#FF9800", bgColor: "rgba(255, 152, 0, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-cool", path: "/pages-blog/contact/contact", visible: true},
        ],
        useCategory: true,
        // 首页分类栏选中引用：默认空（由站长挑选，固定 3 个）
        categories: [],
      },
      galleryConfig: {pageTitle: "图库"},
      categoryConfig: {pageTitle: ""},
      momentConfig: {pageTitle: ""},
      aboutConfig: {
        pageTitle: "关于博主",
        bgImageUrl: "/plugins/plugin-uni-halo/assets/uni_halo_profile_bg.jpg",
        waveImageUrl: "/plugins/plugin-uni-halo/assets/uni_halo_about_wave.gif",
      },
    },
    assets: {
      // 资源默认：仅加载占位 gif 内置插件资源，error 图留空由客户端回退
      // （2026-09-08 起默认图片/空图片配置已下线）
      loadingGifUrl: "/plugins/plugin-uni-halo/assets/uni_halo_img_lazyload.gif",
      loadingErrUrl: "",
    },
    preferences: {
      // 2026-09-08 起按页面分组（与客户端 hermes/preferences.md §3.3 内置默认对齐）：
      // 首页/归档 single + image_bottom，文章列表 double + image_bottom
      homeListLayout: "single",
      homeCardType: "image_bottom",
      articlesListLayout: "double",
      articleCardType: "image_bottom",
      archivesListLayout: "single",
      archivesCardType: "image_bottom",
      avatarRadius: true,
    },
    love: {
      // 恋爱模块默认：总开关关闭；背景图留空（原外链默认图依赖已清除，
      // 由站长配置或客户端内置回退）；模块入口仅开关 + 密码（2026-09-08 起无图标）
      loveEnabled: false,
      pageImages: {
        bgImageUrl: "",
      },
      ourStory: {enabled: true, passwordEnabled: false, password: "", passwordRemoved: false},
      lovePhoto: {enabled: false, passwordEnabled: false, password: "", passwordRemoved: false},
      loveDaily: {enabled: false, passwordEnabled: false, password: "", passwordRemoved: false},
    },
    linkInfo: {
      // 链接配置默认全部留空（站长配置后经 getConfigs 覆盖 pluginConfig.linksSubmitPlugin）
      displayName: "",
      miniProgramCode: "",
      link: "",
      authorName: "",
      avatar: "",
      website: "",
      description: "",
      applyRemark: "",
    },
    maintenance: {
      // 维护模式默认：关闭 + 标题默认「站点维护中」（与后端 buildDefaultMaintenance 对齐）
      enabled: false,
      title: "站点维护中",
      notice: "",
      description: "",
    },
  };
}

/** 回显/加载期间抑制脏标记；加载完成后开启变更追踪 */
let suppressDirty = true;

watch(
  () => config.value,
  (value) => {
    if (!value) {
      return;
    }
    const loaded = cloneDeep(value);
    loaded.spec = deepMerge(defaultSpec(), loaded.spec || {});
    loaded.metadata = {name: "general-config", ...loaded.metadata};
    suppressDirty = true;
    formState.value = loaded;
    dirty.value = false;
    nextTick(() => {
      suppressDirty = false;
    });
  },
  {immediate: true}
);

const dirty = ref(false);

watch(
  formState,
  () => {
    if (!suppressDirty) {
      dirty.value = true;
    }
  },
  {deep: true}
);

function deepMerge<T>(base: T, overlay: object): T {
  const out = cloneDeep(base) as unknown as Record<string, unknown>;
  for (const [key, value] of Object.entries(overlay)) {
    if (value === null || value === undefined) {
      continue;
    }
    if (
      value &&
      typeof value === "object" &&
      !Array.isArray(value) &&
      out[key] &&
      typeof out[key] === "object"
    ) {
      out[key] = deepMerge(out[key], value);
    } else {
      out[key] = cloneDeep(value);
    }
  }
  return out as unknown as T;
}

// ===== 维护分区（2026-09-04 新增，设计见 .docs/maintenance-config-design.md）=====
// 前端展示态规则与后端 MaintenanceResolver 一致；状态判定权威在服务端输出端

type MaintenanceViewStatus = "none" | "scheduled" | "active" | "ended";

const nowMs = ref(Date.now());
const clock = window.setInterval(() => {
  nowMs.value = Date.now();
}, 1000);
onUnmounted(() => window.clearInterval(clock));

/** 计算当前维护状态（enabled=false / endTime 已到 → 不维护；startTime 未来 → 预告） */
function resolveMaintenanceStatus(): MaintenanceViewStatus {
  const m = formState.value.spec.maintenance;
  if (!m?.enabled) {
    return "none";
  }
  const now = nowMs.value;
  const start = m.startTime ? Date.parse(m.startTime) : Number.NaN;
  const end = m.endTime ? Date.parse(m.endTime) : Number.NaN;
  if (Number.isFinite(end) && now >= end) {
    return "ended";
  }
  if (Number.isFinite(start) && now < start) {
    return "scheduled";
  }
  return "active";
}

const STATUS_META: Record<
  MaintenanceViewStatus,
  {label: string; state: "default" | "success" | "warning" | "error"; hint: string}
> = {
  none: {
    label: "未维护",
    state: "default",
    hint: "当前未启用维护模式。可填写开始/结束时间安排维护，或直接开启开关立即进入维护。",
  },
  scheduled: {
    label: "维护预告中",
    state: "warning",
    hint: "已安排维护，到开始时间将自动进入维护中；可修改开始时间或关闭开关取消。",
  },
  active: {
    label: "维护中",
    state: "error",
    hint: "小程序端已展示维护页。已填预计恢复时间则到点自动结束；未填则持续到手动关闭。",
  },
  ended: {
    label: "已按计划结束",
    state: "success",
    hint: "本次维护已到点自动结束，小程序端已恢复正常。如需继续维护，请将结束时间顺延至未来后保存。",
  },
};

const maintenanceStatus = computed<MaintenanceViewStatus>(() => resolveMaintenanceStatus());

const statusMeta = computed(() => STATUS_META[maintenanceStatus.value]);

/** 倒计时（scheduled → 距开始 / active → 距恢复），无目标或已归零返回 null */
function maintenanceCountdown(): {prefix: string; text: string} | null {
  const m = formState.value.spec.maintenance;
  if (!m) {
    return null;
  }
  const status = maintenanceStatus.value;
  const target =
    status === "scheduled" ? m.startTime : status === "active" ? m.endTime : undefined;
  if (!target) {
    return null;
  }
  const remaining = Date.parse(target) - nowMs.value;
  if (!Number.isFinite(remaining) || remaining <= 0) {
    return null;
  }
  const totalSeconds = Math.floor(remaining / 1000);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const pad = (n: number) => String(n).padStart(2, "0");
  const text =
    days > 0
      ? `${days} 天 ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
      : `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  return {prefix: status === "scheduled" ? "距开始维护还有" : "预计恢复还有", text};
}

const maintenanceCountdownText = computed(() => maintenanceCountdown());

/** RFC3339 UTC → 本地 datetime-local 输入值（yyyy-MM-ddTHH:mm），空/非法返回空串 */
function toLocalInput(iso?: string): string {
  if (!iso) {
    return "";
  }
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const pad = (n: number) => String(n).padStart(2, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}

/** 本地 datetime-local 输入值 → RFC3339 UTC；空输入返回 undefined（清除字段） */
function fromLocalInput(value: string): string | undefined {
  if (!value) {
    return undefined;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString();
}

const startTimeLocal = computed({
  get: () => toLocalInput(formState.value.spec.maintenance?.startTime),
  set: (value: string) => {
    formState.value.spec.maintenance.startTime = fromLocalInput(value);
  },
});

const endTimeLocal = computed({
  get: () => toLocalInput(formState.value.spec.maintenance?.endTime),
  set: (value: string) => {
    formState.value.spec.maintenance.endTime = fromLocalInput(value);
  },
});

/** 保存前预检：startTime >= endTime 拒绝（后端 400 兜底） */
function validateMaintenanceWindow(): string | null {
  const m = formState.value.spec.maintenance;
  if (!m) {
    return null;
  }
  const start = m.startTime ? Date.parse(m.startTime) : Number.NaN;
  const end = m.endTime ? Date.parse(m.endTime) : Number.NaN;
  if (Number.isFinite(start) && Number.isFinite(end) && start >= end) {
    return "维护结束时间必须晚于开始时间";
  }
  return null;
}

const handleSave = async () => {
  const maintenanceError = validateMaintenanceWindow();
  if (maintenanceError) {
    Toast.error(maintenanceError);
    return;
  }
  try {
    await generalConfigApi.save(formState.value);
    Toast.success("保存成功");
    dirty.value = false;
    queryClient.invalidateQueries({queryKey: ["uni-halo:general-config"]});
  } catch (error) {
    Toast.error((error as Error).message);
  }
};

/** 提前结束维护（置 enabled=false 并保存） */
const endMaintenanceNow = async () => {
  const m = formState.value.spec.maintenance;
  if (!m) {
    return;
  }
  m.enabled = false;
  await handleSave();
};
</script>

<template>
  <!-- 首页分类栏选择（固定 3 个，复用审核配置候选弹窗） -->
  <AuditCandidatesModal
    v-if="categoryModalVisible"
    type="category"
    :selected="categoryModalSelected"
    :max="3"
    @update:visible="categoryModalVisible = false"
    @confirm="handleCategoryConfirm"
  />

  <VPageHeader title="UniHalo-通用配置">
    <template #actions>
      <div class=":uno: flex items-center">
        <VSpace>
          <SubmitButton type="secondary" :loading="isLoading" :disabled="!dirty" text="保存" @submit="handleSave" />
        </VSpace>
      </div>
    </template>
  </VPageHeader>

  <div class=":uno: flex flex-col gap-4 md:m-4 lg:flex-row">
    <!-- 左列表：切换大分区（VCard，参考公告管理左侧类型栏） -->
    <aside class=":uno: w-full flex-shrink-0 lg:w-64">
      <VCard :body-class="[':uno: !p-0']">
        <div class=":uno: flex items-center justify-between border-b border-gray-100 px-4 py-3">
          <span class=":uno: text-sm font-semibold text-gray-700">配置分类</span>
        </div>
        <div
          v-for="item in GROUP_ITEMS"
          :key="item.id"
          class=":uno: cursor-pointer px-4 py-3"
          :class="
            bigGroup === item.id
              ? ':uno: bg-gray-50 font-medium text-gray-900'
              : ':uno: text-gray-700 hover:bg-gray-50'
          "
          @click="bigGroup = item.id"
        >
          <div class=":uno: text-sm">{{ item.label }}</div>
          <div class=":uno: mt-0.5 text-xs text-gray-400">{{ item.desc }}</div>
        </div>
      </VCard>
    </aside>

    <!-- 右侧：VTabbar 子切换 + 表单内容 -->
    <div class=":uno: min-w-0 flex-1">
      <VCard :loading="isLoading">
        <template #header>
          <div v-if="subTabItems.length > 1" class=":uno: p-2 pb-0">
            <VTabbar v-model:active-id="subTab" :items="subTabItems"/>
          </div>
        </template>

        <!-- 应用资料 → 应用信息 -->
        <template v-if="bigGroup === 'profile' && subTab === 'appInfo'">
          <FormKit v-model="formState.spec.profile.appInfo.name" name="appinfo_name" label="应用名称" type="text" help="小程序应用展示名称，如关于页标题等处使用" />
          <FormKit v-model="formState.spec.profile.appInfo.logo" name="appinfo_logo" label="应用图标" type="attachment" :accepts="['image/*']" help="小程序应用图标" />
        </template>

        <!-- 应用资料 → 博主资料 -->
        <template v-if="bigGroup === 'profile' && subTab === 'blogger'">
          <FormKit v-model="formState.spec.profile.blogger.nickname" name="blogger_nickname" label="昵称" type="text" />
          <FormKit v-model="formState.spec.profile.blogger.email" name="blogger_email" label="邮箱" type="text" />
          <FormKit v-model="formState.spec.profile.blogger.avatar" name="blogger_avatar" label="头像" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.profile.blogger.description" name="blogger_description" label="简介" type="textarea" />
        </template>

        <!-- 应用资料 → 社交信息 -->
        <template v-if="bigGroup === 'profile' && subTab === 'social'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">启用社交信息</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">开启后在小程序「关于」等页面展示社交入口</div>
            </div>
            <VSwitch v-model="formState.spec.profile.social.enabled" />
          </div>
          <div class=":uno: mt-3">
            <FormKit v-model="formState.spec.profile.social.qq" name="social_qq" label="QQ号" type="text" />
              <FormKit v-model="formState.spec.profile.social.wechat" name="social_wechat" label="微信号" type="text" />
              <FormKit v-model="formState.spec.profile.social.weibo" name="social_weibo" label="微博地址" type="text" />
              <FormKit v-model="formState.spec.profile.social.email" name="social_email" label="邮箱" type="text" />
              <FormKit v-model="formState.spec.profile.social.blog" name="social_blog" label="博客" type="text" />
              <FormKit v-model="formState.spec.profile.social.bilibili" name="social_bilibili" label="B站" type="text" />
              <FormKit v-model="formState.spec.profile.social.juejin" name="social_juejin" label="掘金" type="text" />
              <FormKit v-model="formState.spec.profile.social.csdn" name="social_csdn" label="CSDN" type="text" />
              <FormKit v-model="formState.spec.profile.social.gitee" name="social_gitee" label="Gitee" type="text" />
              <FormKit v-model="formState.spec.profile.social.github" name="social_github" label="GitHub" type="text" />
            </div>
        </template>

        <!-- 应用资料 → 页脚版权 -->
        <template v-if="bigGroup === 'profile' && subTab === 'copyright'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">显示版权信息</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">小程序页脚展示的版权文案</div>
            </div>
            <VSwitch v-model="formState.spec.profile.copyrightConfig.enabled" />
          </div>
          <div class=":uno: mt-3">
            <FormKit v-model="formState.spec.profile.copyrightConfig.content" name="copyright_content" label="版权内容" type="textarea" />
          </div>
        </template>

        <!-- 应用资料 → 免责声明（Halo 富文本） -->
        <template v-if="bigGroup === 'profile' && subTab === 'disclaimers'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">启用免责声明</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">在小程序「关于」等页面展示免责声明</div>
            </div>
            <VSwitch v-model="formState.spec.profile.disclaimers.enabled" />
          </div>
          <div class=":uno: mt-3">
            <RichTextEditorField v-model="formState.spec.profile.disclaimers.content" placeholder="输入免责声明内容，支持图文混排……留空使用默认模板" />
          </div>
        </template>

        <!-- 应用资料 → 关于与详情 -->
        <template v-if="bigGroup === 'profile' && subTab === 'about'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">关于项目</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">「关于」页展示开源项目介绍入口</div>
            </div>
            <VSwitch v-model="formState.spec.profile.showAboutSystem" />
          </div>

          <div class=":uno: mt-4 text-sm font-medium text-gray-700">文章详情</div>
          <div class=":uno: mt-2 rounded-lg bg-gray-50 p-4">
            <div class=":uno: flex items-center justify-between gap-4">
              <div>
                <div class=":uno: text-sm text-gray-700">显示评论相关</div>
                <div class=":uno: mt-0.5 text-xs text-gray-400">文章详情页是否展示评论相关功能</div>
              </div>
              <VSwitch v-model="formState.spec.profile.postDetailConfig.showComment" />
            </div>
            <div class=":uno: mt-3 flex items-center justify-between gap-4 border-t border-gray-100 pt-3">
              <div>
                <div class=":uno: text-sm text-gray-700">文章版权</div>
                <div class=":uno: mt-0.5 text-xs text-gray-400">文章底部是否展示版权声明</div>
              </div>
              <VSwitch v-model="formState.spec.profile.postDetailConfig.copyrightEnabled" />
            </div>
            <div class=":uno: mt-3">
              <FormKit v-model="formState.spec.profile.postDetailConfig.copyrightAuthor" name="post_copyright_author" label="文章版权作者" type="text" />
                <FormKit v-model="formState.spec.profile.postDetailConfig.copyrightDesc" name="post_copyright_desc" label="文章版权描述" type="textarea" />
                <FormKit v-model="formState.spec.profile.postDetailConfig.copyrightViolation" name="post_copyright_violation" label="文章侵权说明" type="textarea" />
              </div>
          </div>
        </template>

        <!-- 页面与排版 → 首页（2026-09-08：去标题/轮播，增加快捷导航逐项配置与分类选择） -->
        <template v-if="bigGroup === 'pages' && subTab === 'home'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
            <div>
              <div class=":uno: text-sm text-gray-700">显示快捷导航</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">首页顶部快捷入口</div>
            </div>
            <VSwitch v-model="formState.spec.pages.homeConfig.useQuickNavigation" />
          </div>

          <!-- 快捷导航项配置（一行紧凑：标识 key 禁用 + 名称左右 label + 背景色颜色选择 + 显示开关，拖拽排序） -->
          <div
            v-if="formState.spec.pages.homeConfig.useQuickNavigation"
            class=":uno: mt-4 rounded-lg bg-gray-50 p-4"
          >
            <div class=":uno: mb-2 text-sm font-medium text-gray-700">快捷导航项</div>
            <p class=":uno: mb-3 text-xs text-gray-400">
              拖拽排序，顺序即首页展示顺序；每项仅需维护名称与背景色（标识 key 不可修改）。
            </p>
            <VueDraggable
              v-model="homeQuickNavigation"
              handle=".nav-drag-handle"
            >
              <div
                v-for="(item, index) in homeQuickNavigation"
                :key="index"
                class=":uno: mb-2"
              >
                <div class=":uno: flex flex-wrap items-center gap-x-3 gap-y-2 rounded-lg border border-gray-100 bg-white px-3 py-2">
                  <span class=":uno: nav-drag-handle cursor-move shrink-0 text-gray-400 hover:text-gray-600">
                    <RiDragMove2Line class=":uno: h-4 w-4" />
                  </span>
                  <!-- 标识 key（禁用；FormKit label 置空，label 用 CSS 自定义左侧布局） -->
                  <div class=":uno: flex w-32 shrink-0 items-center gap-2">
                    <span class=":uno: w-10 shrink-0 text-xs text-gray-700">标识</span>
                    <FormKit
                      v-model="item.key"
                      :name="`nav_key_${index}`"
                      type="text"
                      disabled
                      outer-class=":uno: min-w-0 flex-1 !pt-0"
                    />
                  </div>
                  <!-- 名称 -->
                  <div class=":uno: flex min-w-0 flex-1 items-center gap-2">
                    <span class=":uno: w-10 shrink-0 text-xs text-gray-700">名称</span>
                    <FormKit
                      v-model="item.title"
                      :name="`nav_title_${index}`"
                      type="text"
                      placeholder="导航名称"
                      outer-class=":uno: min-w-0 flex-1 !pt-0"
                    />
                  </div>
                  <div class=":uno: flex shrink-0 items-center gap-2 mr-12">
                    <span class=":uno: w-10 shrink-0 text-xs text-gray-700">背景色</span>
                    <FormKit
                      type="color"
                      :model-value="toHexInput(item.bgColor)"
                      @update:model-value="onNavBgColor(item, $event)"
                      outer-class=":uno: w-14 shrink-0 !pt-0"
                    />
                  </div>
                  <!-- 显示开关 -->
                  <div class=":uno: flex shrink-0 items-center gap-2 text-xs text-gray-500">
                    <span>显示</span>
                    <VSwitch v-model="item.visible" />
                  </div>
                </div>
              </div>
            </VueDraggable>
          </div>

          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
            <div>
              <div class=":uno: text-sm text-gray-700">显示分类</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">首页是否展示精品文章分类</div>
            </div>
            <VSwitch v-model="formState.spec.pages.homeConfig.useCategory" />
          </div>

          <!-- 首页分类栏选中（固定 3 个，复用审核配置候选弹窗 AuditCandidatesModal） -->
          <div
            v-if="formState.spec.pages.homeConfig.useCategory"
            class=":uno: mt-4 rounded-lg bg-gray-50 p-4"
          >
            <div class=":uno: mb-2 text-sm font-medium text-gray-700">分类栏展示（固定 3 个）</div>
            <p class=":uno: mb-3 text-xs text-gray-400">
              首页分类栏仅展示选中的分类（最多 3 个），数据在 Halo「分类」管理维护；未选择时客户端回退默认展示。
            </p>
            <div v-if="homeCategories.length" class=":uno: mb-3 flex flex-wrap gap-2">
              <span
                v-for="item in homeCategories"
                :key="item.name"
                class=":uno: flex items-center gap-1 rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-700"
              >
                {{ item.displayName || item.name }}
                <button
                  class=":uno: text-gray-400 hover:text-red-500"
                  title="移除该分类"
                  @click="removeCategory(item)"
                >
                  <RiDeleteBinLine class=":uno: h-3.5 w-3.5" />
                </button>
              </span>
            </div>
            <VButton size="sm" type="secondary" @click="categoryModalVisible = true">
              选择分类
            </VButton>
          </div>
        </template>

        <!-- 页面与排版 → 图库页（2026-09-08：瀑布流配置下线，app 端默认） -->
        <template v-if="bigGroup === 'pages' && subTab === 'gallery'">
          <FormKit v-model="formState.spec.pages.galleryConfig.pageTitle" name="gallery_page_title" label="页面标题" type="text" help="图库页展示标题，留空使用默认" />
        </template>

        <!-- 页面与排版 → 分类页（2026-09-08 新增） -->
        <template v-if="bigGroup === 'pages' && subTab === 'categoryPage'">
          <FormKit v-model="formState.spec.pages.categoryConfig!.pageTitle" name="category_page_title" label="页面标题" type="text" help="分类页展示标题，留空使用默认" />
        </template>

        <!-- 页面与排版 → 瞬间页（2026-09-08 新增） -->
        <template v-if="bigGroup === 'pages' && subTab === 'momentPage'">
          <FormKit v-model="formState.spec.pages.momentConfig!.pageTitle" name="moment_page_title" label="页面标题" type="text" help="瞬间页展示标题，留空使用默认" />
        </template>

        <!-- 页面与排版 → 关于页 -->
        <template v-if="bigGroup === 'pages' && subTab === 'aboutPage'">
          <FormKit v-model="formState.spec.pages.aboutConfig.pageTitle" name="about_page_title" label="页面标题" type="text" />
          <FormKit v-model="formState.spec.pages.aboutConfig.bgImageUrl" name="about_bg_image" label="资料卡背景图" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.pages.aboutConfig.waveImageUrl" name="about_wave_image" label="资料卡波浪图" type="attachment" :accepts="['image/*']" />
        </template>

        <!-- 资源与兜底 → 加载占位（2026-09-08 起仅加载/失败两图，默认图片配置已下线） -->
        <template v-if="bigGroup === 'assets' && subTab === 'loading'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            小程序端图片加载占位资源，留空由客户端内置回退；也可引用插件内置资源，如
            <code class=":uno: rounded bg-gray-100 px-1">/plugins/plugin-uni-halo/assets/uni_halo_img_lazyload.gif</code>
            （素材放插件 <code class=":uno: rounded bg-gray-100 px-1">src/main/resources/static/assets/</code>，同名替换即生效）。
          </p>
          <FormKit v-model="formState.spec.assets.loadingGifUrl" name="assets_loading_gif" label="加载中的图片" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.assets.loadingErrUrl" name="assets_loading_err" label="加载失败图片" type="attachment" :accepts="['image/*']" />
        </template>

        <!-- 偏好设置 → 首页（L0 站点默认，用户可在小程序端「我的-设置」中按个人偏好覆盖） -->
        <template v-if="bigGroup === 'preferences' && subTab === 'home'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            以下为首页默认展示偏好，与小程序端「偏好设置-布局」首页分组对齐；用户可在「我的-设置」中按个人偏好覆盖。
          </p>
          <FormKit
            v-model="formState.spec.preferences.homeListLayout"
            name="pref_home_layout"
            label="首页列表布局"
            type="select"
            :options="[
              {label: '单列', value: 'single'},
              {label: '双列', value: 'double'},
            ]"
            help="首页文章列表默认展示方式"
          />
          <FormKit
            v-model="formState.spec.preferences.homeCardType"
            name="pref_home_card_type"
            label="首页卡片样式"
            type="select"
            :options="CARD_STYLE_OPTIONS"
            help="首页文章卡片中封面图与文字的位置关系"
          />
        </template>

        <!-- 偏好设置 → 文章列表（L0 站点默认，用户可在小程序端偏好覆盖） -->
        <template v-if="bigGroup === 'preferences' && subTab === 'articles'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            以下为文章列表页默认展示偏好，与小程序端「偏好设置-布局」文章列表分组对齐；用户可在「我的-设置」中按个人偏好覆盖。
          </p>
          <FormKit
            v-model="formState.spec.preferences.articlesListLayout"
            name="pref_articles_layout"
            label="文章列表布局"
            type="select"
            :options="[
              {label: '单列', value: 'single'},
              {label: '双列', value: 'double'},
            ]"
            help="文章列表页默认展示方式"
          />
          <FormKit
            v-model="formState.spec.preferences.articleCardType"
            name="pref_articles_card_type"
            label="文章列表卡片样式"
            type="select"
            :options="CARD_STYLE_OPTIONS"
            help="文章列表卡片中封面图与文字的位置关系"
          />
        </template>

        <!-- 偏好设置 → 文章归档（L0 站点默认，用户可在小程序端偏好覆盖） -->
        <template v-if="bigGroup === 'preferences' && subTab === 'archives'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            以下为文章归档页默认展示偏好，与小程序端「偏好设置-布局」文章归档分组对齐；用户可在「我的-设置」中按个人偏好覆盖。
          </p>
          <FormKit
            v-model="formState.spec.preferences.archivesListLayout"
            name="pref_archives_layout"
            label="文章归档布局"
            type="select"
            :options="[
              {label: '单列', value: 'single'},
              {label: '双列', value: 'double'},
            ]"
            help="文章归档页默认展示方式"
          />
          <FormKit
            v-model="formState.spec.preferences.archivesCardType"
            name="pref_archives_card_type"
            label="文章归档卡片样式"
            type="select"
            :options="CARD_STYLE_OPTIONS"
            help="文章归档卡片中封面图与文字的位置关系"
          />
        </template>

        <!-- 恋爱 → 基本设置（总开关 + 恋爱页背景图；2026-09-08 起仅背景图，无分组样式） -->
        <template v-if="bigGroup === 'love' && subTab === 'basic'">
          <p class=":uno: mb-3 text-xs text-gray-400">恋爱数据在「恋爱管理」菜单维护，此处配置恋爱总开关、恋爱页背景图与各模块入口展示。</p>
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">启用恋爱日记</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">开启后小程序端「我的页面」导航出现恋爱入口</div>
            </div>
            <VSwitch v-model="formState.spec.love.loveEnabled" />
          </div>
          <div class=":uno: mt-3">
            <FormKit v-model="formState.spec.love.pageImages!.bgImageUrl" name="love_bg_image" label="背景图片" type="attachment" :accepts="['image/*']" />
          </div>
        </template>

        <!-- 恋爱 → 模块入口（开关 + 入口密码；2026-09-08 起无图标配置） -->
        <template v-if="bigGroup === 'love' && subTab === 'modules'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            以下为恋爱页各模块入口的展示开关与入口密码；模块数据分别在「恋爱管理-恋爱故事 / 恋爱相册 / 恋爱清单」维护。
            设置密码后，小程序端进入该模块前需先验证密码。
          </p>
          <div v-for="item in LOVE_MODULES" :key="item.key" class=":uno: mb-4 rounded-lg bg-gray-50 p-4">
            <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
              <div>
                <div class=":uno: text-sm text-gray-700">{{ item.label }}</div>
                <div class=":uno: mt-0.5 text-xs text-gray-400">{{ item.desc }}</div>
              </div>
              <VSwitch v-model="formState.spec.love[item.key]!.enabled" />
            </div>
            <div class=":uno: mt-3">
              <div class=":uno: mb-2 flex items-center text-sm text-gray-700">
                入口密码
                <span
                  v-if="formState.spec.love[item.key]!.passwordEnabled"
                  class=":uno: ml-2 text-xs font-normal text-emerald-600"
                >已设置</span>
                <span v-else class=":uno: ml-2 text-xs font-normal text-gray-400">未设置</span>
              </div>
              <FormKit
                v-if="!formState.spec.love[item.key]!.passwordRemoved"
                v-model="formState.spec.love[item.key]!.password"
                :name="`love_${item.key}_password`"
                label="新密码"
                type="password"
                :help="formState.spec.love[item.key]!.passwordEnabled
                  ? '留空表示保持原密码不变'
                  : '设置后进入该模块前需先输入密码'"
                placeholder="输入入口密码"
                @input="cancelRemovalOnTyping(formState.spec.love[item.key])"
              />
              <p v-else class=":uno: text-sm text-gray-500">保存后将清除该入口密码。</p>
              <VButton
                v-if="formState.spec.love[item.key]!.passwordEnabled && !formState.spec.love[item.key]!.passwordRemoved"
                size="sm"
                type="danger"
                plain
                class=":uno: mt-2"
                @click="clearModulePassword(item.key)"
              >
                清除密码
              </VButton>
              <VButton
                v-if="formState.spec.love[item.key]!.passwordRemoved"
                size="sm"
                type="secondary"
                plain
                class=":uno: mt-2"
                @click="formState.spec.love[item.key]!.passwordRemoved = false"
              >
                取消清除
              </VButton>
            </div>
          </div>
        </template>

        <!-- 链接配置 → 小程序信息（2026-09-08 新增：站长小程序展示信息，小程序端「申请信息」弹窗展示） -->
        <template v-if="bigGroup === 'linkInfo' && subTab === 'info'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            配置你自己的小程序展示信息，用于 app 小程序端「申请信息」弹窗展示（小程序名称 / 太阳码 / 跳转地址 / 描述 / 申请说明）；留空的项不在弹窗中展示。
          </p>
          <FormKit v-model="formState.spec.linkInfo.displayName" name="link_display_name" label="小程序名称" type="text" placeholder="如「UniHalo 博客」" />
          <FormKit v-model="formState.spec.linkInfo.miniProgramCode" name="link_mini_program_code" label="太阳码/小程序码" type="attachment" :accepts="['image/*']" placeholder="选择或粘贴小程序码图片地址" help="小程序码图片，弹窗中点击可预览" />
          <FormKit v-model="formState.spec.linkInfo.link" name="link_url" label="跳转地址" type="text" placeholder="如 #小程序://小莫唐尼/AGLiOpse2vi6QJC" help="说明：微信打开小程序，点击右上角三个点找到复制链接。" />
          <FormKit v-model="formState.spec.linkInfo.description" name="link_description" label="小程序描述" type="textarea" placeholder="一句话介绍你的小程序，如「记录生活与技术的个人博客」" />
          <FormKit v-model="formState.spec.linkInfo.applyRemark" name="link_apply_remark" label="申请说明" type="textarea" placeholder="如「欢迎友链互换，请附上你的网站信息」" help="弹窗中「申请说明」栏展示的文案" />
        </template>

        <!-- 链接配置 → 作者信息（2026-09-08 新增：作者昵称/头像/网站） -->
        <template v-if="bigGroup === 'linkInfo' && subTab === 'author'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            配置作者展示信息，用于 app 小程序端「申请信息」弹窗中作者信息区展示；留空的项不在弹窗中展示。
          </p>
          <FormKit v-model="formState.spec.linkInfo.authorName" name="link_author_name" label="作者昵称" type="text" placeholder="如「小莫唐尼」" />
          <FormKit v-model="formState.spec.linkInfo.avatar" name="link_avatar" label="作者头像" type="attachment" :accepts="['image/*']" placeholder="选择或粘贴作者头像图片地址" />
          <FormKit v-model="formState.spec.linkInfo.website" name="link_website" label="作者网站" type="text" placeholder="如 https://your-site.com" />
        </template>

        <!-- 维护 → 维护设置（单屏，无子 Tab） -->
        <template v-if="bigGroup === 'maintenance'">
          <!-- 状态摘要卡：四态 + 实时倒计时 + 快捷操作 -->
          <div class=":uno: mb-4 rounded-lg bg-gray-50 p-4">
            <div class=":uno: flex flex-wrap items-center gap-x-4 gap-y-2">
              <VStatusDot :state="statusMeta.state" :text="statusMeta.label" />
              <span v-if="maintenanceCountdownText" class=":uno: text-sm text-gray-500">
                {{ maintenanceCountdownText.prefix }} {{ maintenanceCountdownText.text }}
              </span>
              <VButton
                v-if="maintenanceStatus === 'active'"
                class=":uno: !ml-auto"
                type="danger"
                size="sm"
                @click="endMaintenanceNow"
              >
                提前结束维护
              </VButton>
            </div>
            <p class=":uno: mt-2 text-xs text-gray-400">{{ statusMeta.hint }}</p>
          </div>

          <!-- 开启维护开关（时间窗口紧随其后） -->
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">开启维护</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">开启后小程序端将展示维护页；不填开始时间 = 开启即立即进入维护</div>
            </div>
            <VSwitch v-model="formState.spec.maintenance.enabled" />
          </div>

          <!-- 维护时间窗口（置于开启维护下方） -->
          <div class=":uno: mt-4 rounded-lg bg-gray-50 p-4">
            <div class=":uno: mb-3 text-sm font-medium text-gray-700">维护时间窗口</div>
            <div class=":uno: mb-4">
              <label class=":uno: mb-1 block text-sm text-gray-700" for="maintenance_start_time">开始时间</label>
              <input id="maintenance_start_time" v-model="startTimeLocal" type="datetime-local" class=":uno: h-9 w-full max-w-xs rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition hover:border-gray-400" />
              <p class=":uno: mt-1 text-xs text-gray-400">留空 = 开启后立即维护；填写未来时间则先向用户展示维护预告与倒计时</p>
            </div>
            <div>
              <label class=":uno: mb-1 block text-sm text-gray-700" for="maintenance_end_time">预计恢复时间</label>
              <input id="maintenance_end_time" v-model="endTimeLocal" type="datetime-local" class=":uno: h-9 w-full max-w-xs rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition hover:border-gray-400" />
              <p class=":uno: mt-1 text-xs text-gray-400">留空 = 持续维护直到手动关闭；填写后到点自动结束（小程序端恢复）</p>
            </div>
          </div>

          <!-- 维护标题 -->
          <div class=":uno: mt-4">
            <FormKit v-model="formState.spec.maintenance.title" name="maintenance_title" label="维护标题" type="text" help="维护页展示的大标题，如「系统升级维护」" />
          </div>

          <!-- 维护说明（页面展示，纯文本） -->
          <div class=":uno: mt-4">
            <FormKit v-model="formState.spec.maintenance.notice" name="maintenance_notice" label="维护说明" type="textarea" help="展示在维护页标题下方；留空则展示默认提示文案" />
          </div>

          <!-- 维护详情（小程序端弹窗展示，富文本） -->
          <div class=":uno: mt-4">
            <div class=":uno: mb-2 text-sm text-gray-700">维护详情</div>
            <RichTextEditorField v-model="formState.spec.maintenance.description" placeholder="维护详情，支持图文混排……小程序端「维护详情」弹窗展示，留空则不展示详情入口" />
          </div>
        </template>
      </VCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import {Dialog, Toast, VButton, VEmpty, VSpace, VSwitch} from "@halo-dev/components";
import {computed, inject, ref} from "vue";
import {VueDraggable} from "vue-draggable-plus";
import RiDragMove2Line from "~icons/ri/drag-move-2-line";
import RiDeleteBinLine from "~icons/ri/delete-bin-6-line";
import RiImageLine from "~icons/ri/image-line";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import AuditCandidatesModal from "@/components/audit-config/AuditCandidatesModal.vue";
import FeatureEntryCandidatesModal from "@/components/general-config/FeatureEntryCandidatesModal.vue";
import {GeneralConfigFormKey} from "@/views/general-config/form-context";
import {
  featureEntriesByGroup,
  toQuickNavigationItem,
  type FeatureEntry,
} from "@/constant/feature-entries";
import type {
  AuditDataRef,
  GeneralConfigCategoryItem,
  GeneralConfigMyPage,
  GeneralConfigQuickNavigationItem,
} from "@/types";

/**
 * 页面设置分区（2026-09-10 组件化拆分）：
 * 首页（快捷导航逐项配置 + 分类栏固定 3 个）/ 图库页 / 分类页 / 瞬间页 / 关于页
 * （含页脚版权与功能入口两组）/ 文章详情页 / 免责声明页。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;

/**
 * 快捷导航默认 5 项：由功能入口注册表派生（group=home，对齐客户端 uh-home-quick-nav
 * 默认 navList；bgColor 原 bgGlass、visible 原 show）。「恢复默认」数据源与「添加」
 * 候选弹窗一致（设计见 .docs/feature-entry-unified-design.md D3/D7）。
 */
const DEFAULT_QUICK_NAVIGATION: GeneralConfigQuickNavigationItem[] =
  featureEntriesByGroup("home").map(toQuickNavigationItem);

/** 首页快捷导航「添加」候选弹窗（统一清单：展示全部注册表条目，已配置置灰禁选，确认后追加） */
const quickNavModalVisible = ref(false);

/** 快捷导航项列表（写回 formState；VueDraggable 需要非 undefined 数组） */
const homeQuickNavigation = computed({
  get: () => formState.value.spec.pages.homeConfig.quickNavigation || [],
  set: (value: GeneralConfigQuickNavigationItem[]) => {
    formState.value.spec.pages.homeConfig.quickNavigation = value;
  },
});

/** 候选弹窗确认：选中的注册表条目快照追加到快捷导航列表尾部 */
const handleQuickNavConfirm = (selected: FeatureEntry[]) => {
  const list = formState.value.spec.pages.homeConfig.quickNavigation || [];
  formState.value.spec.pages.homeConfig.quickNavigation = [
    ...list,
    ...selected.map(toQuickNavigationItem),
  ];
  quickNavModalVisible.value = false;
};

/** 删除某项快捷导航 */
const removeQuickNavItem = (item: GeneralConfigQuickNavigationItem) => {
  const list = formState.value.spec.pages.homeConfig.quickNavigation || [];
  formState.value.spec.pages.homeConfig.quickNavigation = list.filter(
    (it) => it.key !== item.key
  );
};

/** 「恢复默认」：确认后恢复为候选弹窗注册表 home 组的默认配置（全部快照字段、排序/visible 一并恢复） */
function restoreQuickNavigationDefaults() {
  Dialog.warning({
    title: "恢复默认",
    description: "将全部快捷导航项恢复为默认配置，确定继续吗？",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: () => {
      formState.value.spec.pages.homeConfig.quickNavigation = DEFAULT_QUICK_NAVIGATION.map(
        (item) => ({...item})
      );
      Toast.success("已恢复默认");
    },
  });
}

// ===== 首页分类栏选择（固定 3 个，复用审核配置候选弹窗 AuditCandidatesModal）=====

const categoryModalVisible = ref(false);

/** 首页分类栏已选引用（GeneralConfigCategoryItem：name + displayName + cover 快照） */
const homeCategories = computed(
  () => formState.value.spec.pages.homeConfig.categories || []
);

/** 回显给候选弹窗的 AuditDataRef 形态（title ← displayName、cover ← cover、priority/postCount ← 快照） */
const categoryModalSelected = computed<AuditDataRef[]>(() =>
  homeCategories.value.map((item) => ({
    name: item.name || "",
    title: item.displayName,
    cover: item.cover,
    priority: item.priority,
    postCount: item.postCount,
  }))
);

/** 分类栏已选快照列表（写回 formState；VueDraggable 拖拽排序，顺序 = 展示顺序） */
const homeCategoriesSortable = computed({
  get: () => formState.value.spec.pages.homeConfig.categories || [],
  set: (value: GeneralConfigCategoryItem[]) => {
    formState.value.spec.pages.homeConfig.categories = value;
  },
});

/** 弹窗确认：映射回 {name, displayName, cover, priority, postCount} 快照并关闭（候选弹窗已限制最多 3 个） */
const handleCategoryConfirm = (selected: AuditDataRef[]) => {
  formState.value.spec.pages.homeConfig.categories = selected.map((item) => ({
    name: item.name,
    displayName: item.title || item.name,
    cover: item.cover,
    priority: item.priority,
    postCount: item.postCount,
  }));
  categoryModalVisible.value = false;
};

const removeCategory = (item: GeneralConfigCategoryItem) => {
  const list = formState.value.spec.pages.homeConfig.categories || [];
  formState.value.spec.pages.homeConfig.categories = list.filter(
    (it) => it.name !== item.name
  );
};

// ===== 快捷导航项/功能入口共用编辑工具（仅维护名称/背景色/显示，key 固定、拖拽排序） =====

/** FormKit type="color" 回显值：空值兜底，rgba/hex 原样透传（保留透明度供 Sketch 滑块回显） */
function toColorInput(value?: string): string {
  return value || "#cccccc";
}

/** FormKit type="color" 选色（format="hex8" 输出 #rrggbbaa 含透明度）写回 bgColor（客户端 uh-home-quick-nav 直接读该色值渲染） */
function onNavBgColor(item: GeneralConfigQuickNavigationItem, value: unknown) {
  if (typeof value === "string") {
    item.bgColor = value;
  }
}

// ===== 我的页面功能入口（2026-09-10 新增：常用功能/其他功能两组） =====

/** 我的页面 myPageConfig 安全访问（defaultSpec 已含两组默认，旧数据可能缺失） */
const myPageConfig = computed<GeneralConfigMyPage>(
  () => formState.value.spec.pages.myPageConfig || {commonFeatures: [], otherFeatures: []}
);

/** 两组列表（写回 formState；VueDraggable 需要非 undefined 数组） */
const myPageCommonFeatures = computed({
  get: () => myPageConfig.value.commonFeatures || [],
  set: (value: GeneralConfigQuickNavigationItem[]) => {
    myPageConfig.value.commonFeatures = value;
  },
});

const myPageOtherFeatures = computed({
  get: () => myPageConfig.value.otherFeatures || [],
  set: (value: GeneralConfigQuickNavigationItem[]) => {
    myPageConfig.value.otherFeatures = value;
  },
});

/** 候选弹窗：当前打开的分组（common=常用功能 / other=其他功能；null=关闭） */
const myPageModalGroup = ref<"common" | "other" | null>(null);

/** 候选弹窗确认：选中的注册表条目快照追加到对应分组列表尾部 */
const handleMyPageConfirm = (selected: FeatureEntry[], group: "common" | "other") => {
  const list = group === "common"
    ? myPageCommonFeatures.value
    : myPageOtherFeatures.value;
  const appended = [...list, ...selected.map(toQuickNavigationItem)];
  if (group === "common") {
    myPageCommonFeatures.value = appended;
  } else {
    myPageOtherFeatures.value = appended;
  }
  myPageModalGroup.value = null;
};

/** 删除对应分组中的某项 */
const removeMyPageFeature = (group: "common" | "other", item: GeneralConfigQuickNavigationItem) => {
  const list = group === "common"
    ? myPageCommonFeatures.value
    : myPageOtherFeatures.value;
  const filtered = list.filter((it) => it.key !== item.key);
  if (group === "common") {
    myPageCommonFeatures.value = filtered;
  } else {
    myPageOtherFeatures.value = filtered;
  }
};

/** 恢复默认：恢复为注册表对应组的默认配置（全部快照字段、排序/visible 一并恢复；
 * 常用功能=home 组条目、其他功能=other 组条目，与 defaultConfig 默认填充一致） */
function restoreMyPageDefaults(group: "common" | "other") {
  const label = group === "common" ? "常用功能" : "其他功能";
  Dialog.warning({
    title: "恢复默认",
    description: `将「${label}」恢复为默认配置，确定继续吗？`,
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: () => {
      const defaults = featureEntriesByGroup(
        group === "common" ? "home" : "other"
      ).map(toQuickNavigationItem);
      if (group === "common") {
        myPageCommonFeatures.value = defaults;
      } else {
        myPageOtherFeatures.value = defaults;
      }
      Toast.success("已恢复默认");
    },
  });
}
</script>

<template>
  <!-- 页面与排版 → 首页 -->
  <template v-if="subTab === 'home'">
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
      <div class=":uno: mb-2 flex items-center justify-between">
        <div class=":uno: text-sm font-medium text-gray-700">快捷导航项</div>
        <VSpace>
          <VButton size="sm" type="secondary" @click="quickNavModalVisible = true">添加</VButton>
          <VButton size="sm" type="secondary" @click="restoreQuickNavigationDefaults">恢复默认</VButton>
        </VSpace>
      </div>
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
            <div class=":uno: flex min-w-0 flex-1 items-center gap-2 px-12">
              <span class=":uno: w-10 shrink-0 text-xs text-gray-700">名称</span>
              <FormKit
                v-model="item.title"
                :name="`nav_title_${index}`"
                type="text"
                placeholder="导航名称"
                outer-class=":uno: min-w-0 flex-1 !pt-0"
              />
            </div>
            <div class=":uno: flex min-w-0 flex-1 items-center gap-2">
              <span class=":uno: w-10 shrink-0 text-xs text-gray-700">背景色</span>
              <FormKit
                type="color"
                format="hex8"
                :model-value="toColorInput(item.bgColor)"
                @update:model-value="onNavBgColor(item, $event)"
                outer-class=":uno: w-14 shrink-0 !pt-0"
              />
            </div>
            <!-- 显示开关 -->
            <div class=":uno: flex shrink-0 items-center gap-2 text-xs text-gray-500">
              <span>显示</span>
              <VSwitch v-model="item.visible" />
            </div>
            <!-- 删除 -->
            <button
              type="button"
              class=":uno: shrink-0 text-gray-400 transition-all hover:text-red-600"
              title="删除"
              @click="removeQuickNavItem(item)"
            >
              <RiDeleteBinLine class=":uno: h-4 w-4" />
            </button>
          </div>
        </div>
      </VueDraggable>
    </div>

    <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
      <div>
        <div class=":uno: text-sm text-gray-700">显示精选分类</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">首页是否展示精选分类栏</div>
      </div>
      <VSwitch v-model="formState.spec.pages.homeConfig.useCategory" />
    </div>

    <!-- 首页分类栏选中（固定 3 个，快照含封面/名称，拖拽排序；复用审核配置候选弹窗 AuditCandidatesModal） -->
    <div
      v-if="formState.spec.pages.homeConfig.useCategory"
      class=":uno: mt-4 rounded-lg bg-gray-50 p-4"
    >
      <div class=":uno: mb-2 text-sm font-medium text-gray-700">分类栏展示（固定 3 个）</div>
      <p class=":uno: mb-3 text-xs text-gray-400">
        首页分类栏仅展示选中的分类（最多 3 个），保存后以快照（封面/名称）下发，app 端直接渲染、不再请求分类接口；数据在 Halo「分类」管理维护。
      </p>
      <div v-if="homeCategoriesSortable.length" class=":uno: mb-3">
        <VueDraggable
          v-model="homeCategoriesSortable"
          handle=".home-category-drag-handle"
        >
          <div
            v-for="item in homeCategoriesSortable"
            :key="item.name"
            class=":uno: mb-2 flex items-center gap-3 rounded-md border border-gray-100 bg-white px-3 py-2 last:mb-0"
          >
            <span class=":uno: home-category-drag-handle cursor-move text-gray-300 hover:text-gray-500">
              <RiDragMove2Line class=":uno: h-4 w-4" />
            </span>
            <div class=":uno: flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-md bg-gray-100 text-base">
              <img
                v-if="item.cover"
                :src="item.cover"
                class=":uno: h-full w-full object-cover"
                alt=""
              />
              <RiImageLine v-else class=":uno: h-5 w-5 text-gray-300" />
            </div>
            <div class=":uno: min-w-0 flex-1">
              <div class=":uno: truncate text-sm font-medium text-gray-700">
                {{ item.displayName || item.name }}
              </div>
            </div>
            <button
              class=":uno: rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
              title="移除该分类"
              @click="removeCategory(item)"
            >
              <RiDeleteBinLine class=":uno: h-4 w-4" />
            </button>
          </div>
        </VueDraggable>
      </div>
      <VButton size="sm" type="secondary" @click="categoryModalVisible = true">
        选择分类
      </VButton>
    </div>
  </template>

  <!-- 页面与排版 → 图库页（2026-09-08：瀑布流配置下线，app 端默认） -->
  <template v-if="subTab === 'gallery'">
    <FormKit v-model="formState.spec.pages.galleryConfig.pageTitle" name="gallery_page_title" label="页面标题" type="text" help="图库页展示标题，留空使用默认" />
  </template>

  <!-- 页面与排版 → 分类页（2026-09-08 新增） -->
  <template v-if="subTab === 'categoryPage'">
    <FormKit v-model="formState.spec.pages.categoryConfig!.pageTitle" name="category_page_title" label="页面标题" type="text" help="分类页展示标题，留空使用默认" />
  </template>

  <!-- 页面与排版 → 瞬间页（2026-09-08 新增） -->
  <template v-if="subTab === 'momentPage'">
    <FormKit v-model="formState.spec.pages.momentConfig!.pageTitle" name="moment_page_title" label="页面标题" type="text" help="瞬间页展示标题，留空使用默认" />
  </template>

  <!-- 页面与排版 → 关于页 -->
  <template v-if="subTab === 'aboutPage'">
    <FormKit v-model="formState.spec.pages.aboutConfig.pageTitle" name="about_page_title" label="页面标题" type="text" />
    <FormKit v-model="formState.spec.pages.aboutConfig.bgImageUrl" name="about_bg_image" label="资料卡背景图" type="attachment" :accepts="['image/*']" />
    <FormKit v-model="formState.spec.pages.aboutConfig.waveImageUrl" name="about_wave_image" label="资料卡波浪图" type="attachment" :accepts="['image/*']" />

    <!-- 页脚版权（2026-09-10 由应用设置迁入，显示于【关于】页面页脚） -->
    <div class=":uno: mt-6 rounded-lg bg-gray-50 p-4">
      <div class=":uno: mb-2 text-sm font-medium text-gray-700">页脚版权</div>
      <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
        <div>
          <div class=":uno: text-sm text-gray-700">显示版权信息</div>
          <div class=":uno: mt-0.5 text-xs text-gray-400">小程序关于页页脚展示的版权文案</div>
        </div>
        <VSwitch v-model="formState.spec.pages.aboutConfig.copyrightConfig!.enabled" />
      </div>
      <div class=":uno: mt-3">
        <FormKit v-model="formState.spec.pages.aboutConfig.copyrightConfig!.content" name="copyright_content" label="版权内容" type="textarea" />
      </div>
    </div>

    <!-- 功能入口（2026-09-10 新增：常用功能/其他功能两组，候选弹窗添加 + 拖拽排序 +
         名称/背景色/显示编辑 + 删除 + 恢复默认；编辑布局对齐首页快捷导航项） -->
    <div class=":uno: mt-6 rounded-lg bg-gray-50 p-4">
      <div class=":uno: mb-2 text-sm font-medium text-gray-700">功能入口</div>
      <p class=":uno: mb-3 text-xs text-gray-400">
        「我的」页面（about）展示的功能入口，分组配置；拖拽排序，顺序即展示顺序。
      </p>

      <!-- 常用功能（原「博客功能」改名） -->
      <div class=":uno: rounded-lg bg-white p-3">
        <div class=":uno: mb-2 flex items-center justify-between">
          <div class=":uno: text-sm font-medium text-gray-700">常用功能</div>
          <VSpace>
            <VButton size="sm" type="secondary" @click="myPageModalGroup = 'common'">添加</VButton>
            <VButton size="sm" type="secondary" @click="restoreMyPageDefaults('common')">恢复默认</VButton>
          </VSpace>
        </div>
        <VueDraggable v-model="myPageCommonFeatures" handle=".nav-drag-handle">
          <div
            v-for="(item, index) in myPageCommonFeatures"
            :key="index"
            class=":uno: mb-2 last:mb-0"
          >
            <div class=":uno: flex flex-wrap items-center gap-x-3 gap-y-2 rounded-lg border border-gray-100 bg-gray-50 px-3 py-2">
              <span class=":uno: nav-drag-handle cursor-move shrink-0 text-gray-400 hover:text-gray-600">
                <RiDragMove2Line class=":uno: h-4 w-4" />
              </span>
              <div class=":uno: flex w-32 shrink-0 items-center gap-2">
                <span class=":uno: w-10 shrink-0 text-xs text-gray-700">标识</span>
                <FormKit
                  v-model="item.key"
                  :name="`mypage_common_key_${index}`"
                  type="text"
                  disabled
                  outer-class=":uno: min-w-0 flex-1 !pt-0"
                />
              </div>
              <div class=":uno: flex min-w-0 flex-1 items-center gap-2 px-12">
                <span class=":uno: w-10 shrink-0 text-xs text-gray-700">名称</span>
                <FormKit
                  v-model="item.title"
                  :name="`mypage_common_title_${index}`"
                  type="text"
                  placeholder="导航名称"
                  outer-class=":uno: min-w-0 flex-1 !pt-0"
                />
              </div>
              <div class=":uno: flex min-w-0 flex-1 items-center gap-2">
                <span class=":uno: w-10 shrink-0 text-xs text-gray-700">背景色</span>
                <FormKit
                  type="color"
                  format="hex8"
                  :model-value="toColorInput(item.bgColor)"
                  @update:model-value="onNavBgColor(item, $event)"
                  outer-class=":uno: w-14 shrink-0 !pt-0"
                />
              </div>
              <div class=":uno: flex shrink-0 items-center gap-2 text-xs text-gray-500">
                <span>显示</span>
                <VSwitch v-model="item.visible" />
              </div>
              <button
                type="button"
                class=":uno: shrink-0 text-gray-400 transition-all hover:text-red-600"
                title="删除"
                @click="removeMyPageFeature('common', item)"
              >
                <RiDeleteBinLine class=":uno: h-4 w-4" />
              </button>
            </div>
          </div>
        </VueDraggable>
        <VEmpty
          v-if="!myPageCommonFeatures.length"
          title="暂无功能入口"
          message="点击「添加」选择功能"
          :class="':uno: py-6'"
        />
      </div>

      <!-- 其他功能 -->
      <div class=":uno: mt-4 rounded-lg bg-white p-3">
        <div class=":uno: mb-2 flex items-center justify-between">
          <div class=":uno: text-sm font-medium text-gray-700">其他功能</div>
          <VSpace>
            <VButton size="sm" type="secondary" @click="myPageModalGroup = 'other'">添加</VButton>
            <VButton size="sm" type="secondary" @click="restoreMyPageDefaults('other')">恢复默认</VButton>
          </VSpace>
        </div>
        <VueDraggable v-model="myPageOtherFeatures" handle=".nav-drag-handle">
          <div
            v-for="(item, index) in myPageOtherFeatures"
            :key="index"
            class=":uno: mb-2 last:mb-0"
          >
            <div class=":uno: flex flex-wrap items-center gap-x-3 gap-y-2 rounded-lg border border-gray-100 bg-gray-50 px-3 py-2">
              <span class=":uno: nav-drag-handle cursor-move shrink-0 text-gray-400 hover:text-gray-600">
                <RiDragMove2Line class=":uno: h-4 w-4" />
              </span>
              <div class=":uno: flex w-32 shrink-0 items-center gap-2">
                <span class=":uno: w-10 shrink-0 text-xs text-gray-700">标识</span>
                <FormKit
                  v-model="item.key"
                  :name="`mypage_other_key_${index}`"
                  type="text"
                  disabled
                  outer-class=":uno: min-w-0 flex-1 !pt-0"
                />
              </div>
              <div class=":uno: flex min-w-0 flex-1 items-center gap-2 px-12">
                <span class=":uno: w-10 shrink-0 text-xs text-gray-700">名称</span>
                <FormKit
                  v-model="item.title"
                  :name="`mypage_other_title_${index}`"
                  type="text"
                  placeholder="导航名称"
                  outer-class=":uno: min-w-0 flex-1 !pt-0"
                />
              </div>
              <div class=":uno: flex min-w-0 flex-1 items-center gap-2">
                <span class=":uno: w-10 shrink-0 text-xs text-gray-700">背景色</span>
                <FormKit
                  type="color"
                  format="hex8"
                  :model-value="toColorInput(item.bgColor)"
                  @update:model-value="onNavBgColor(item, $event)"
                  outer-class=":uno: w-14 shrink-0 !pt-0"
                />
              </div>
              <div class=":uno: flex shrink-0 items-center gap-2 text-xs text-gray-500">
                <span>显示</span>
                <VSwitch v-model="item.visible" />
              </div>
              <button
                type="button"
                class=":uno: shrink-0 text-gray-400 transition-all hover:text-red-600"
                title="删除"
                @click="removeMyPageFeature('other', item)"
              >
                <RiDeleteBinLine class=":uno: h-4 w-4" />
              </button>
            </div>
          </div>
        </VueDraggable>
        <VEmpty
          v-if="!myPageOtherFeatures.length"
          title="暂无功能入口"
          message="点击「添加」选择功能"
          :class="':uno: py-6'"
        />
      </div>
    </div>
  </template>

  <!-- 页面与排版 → 文章详情页（2026-09-10 由应用设置「关于与详情」迁入） -->
  <template v-if="subTab === 'postDetail'">
    <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
      <div>
        <div class=":uno: text-sm text-gray-700">显示评论相关</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">文章详情页是否展示评论相关功能</div>
      </div>
      <VSwitch v-model="formState.spec.pages.postDetailConfig!.showComment" />
    </div>
    <div class=":uno: mt-4 flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
      <div>
        <div class=":uno: text-sm text-gray-700">文章版权</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">文章底部是否展示版权声明</div>
      </div>
      <VSwitch v-model="formState.spec.pages.postDetailConfig!.copyrightEnabled" />
    </div>
    <div class=":uno: mt-4 rounded-lg bg-gray-50 p-4">
      <FormKit v-model="formState.spec.pages.postDetailConfig!.copyrightAuthor" name="post_copyright_author" label="文章版权作者" type="text" />
      <FormKit v-model="formState.spec.pages.postDetailConfig!.copyrightDesc" name="post_copyright_desc" label="文章版权描述" type="textarea" />
      <FormKit v-model="formState.spec.pages.postDetailConfig!.copyrightViolation" name="post_copyright_violation" label="文章侵权说明" type="textarea" />
    </div>
  </template>

  <!-- 页面与排版 → 免责声明页（2026-09-10 由应用设置迁入：不再需要启用开关，仅内容） -->
  <template v-if="subTab === 'disclaimersPage'">
    <p class=":uno: mb-3 text-xs text-gray-400">
      小程序端「免责声明」页面展示的内容（支持图文混排）；留空则不展示该页面。
    </p>
    <RichTextEditorField v-model="formState.spec.pages.disclaimers!.content" placeholder="输入免责声明内容，支持图文混排……留空则不展示免责声明页" />
  </template>
</template>

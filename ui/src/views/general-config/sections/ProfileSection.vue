<script setup lang="ts">
import {VButton, VEmpty, VSpace, VSwitch} from "@halo-dev/components";
import {computed, inject} from "vue";
import {VueDraggable} from "vue-draggable-plus";
import RiDragMove2Line from "~icons/ri/drag-move-2-line";
import RiDeleteBinLine from "~icons/ri/delete-bin-6-line";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import type { GeneralConfigSocialItem } from "@/types";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 应用设置分区（2026-09-10 组件化拆分）：
 * 应用信息 / 博主资料 / 社交信息 / 页脚版权 子 tab 表单。
 * 数据经 provide/inject 共享 formState，直接改嵌套属性触发父级 deep watch → dirty。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;

/** 社交项列表（2026-09-10 起动态列表；写回 formState，VueDraggable 需要非 undefined 数组） */
const socialItems = computed({
  get: () => formState.value.spec.profile.social?.items || [],
  set: (value: GeneralConfigSocialItem[]) => {
    if (!formState.value.spec.profile.social) {
      formState.value.spec.profile.social = {};
    }
    formState.value.spec.profile.social.items = value;
  },
});

/** 添加空行社交项（直接在列表下方追加一条空内容，名称/颜色给默认值便于识别） */
function addSocialItem() {
  socialItems.value = [
    ...socialItems.value,
    {
      key: "",
      name: "",
      content: "",
      color: "#8a8a8a",
      bgColor: "#8a8a8a1A",
      priority: (socialItems.value.length || 0) + 1,
      visible: true,
    },
  ];
}

/** 删除某条社交项 */
function removeSocialItem(item: GeneralConfigSocialItem) {
  socialItems.value = socialItems.value.filter((it) => it !== item);
}

/** FormKit type="color" 回显值：空值兜底，rgba/hex 原样透传（保留透明度） */
function toColorInput(value?: string): string {
  return value || "#cccccc";
}

/** FormKit type="color" 选色（format="hex8" 输出 #rrggbbaa 含透明度）写回颜色字段 */
function onSocialColor(item: GeneralConfigSocialItem, value: unknown) {
  if (typeof value === "string") {
    item.color = value;
  }
}

function onSocialBgColor(item: GeneralConfigSocialItem, value: unknown) {
  if (typeof value === "string") {
    item.bgColor = value;
  }
}
</script>

<template>
  <!-- 应用资料 → 应用信息 -->
  <template v-if="subTab === 'appInfo'">
    <FormKit v-model="formState.spec.profile.appInfo.name" name="appinfo_name" label="应用名称" type="text" help="小程序应用展示名称，如关于页标题等处使用" />
    <FormKit v-model="formState.spec.profile.appInfo.logo" name="appinfo_logo" label="应用图标" type="attachment" :accepts="['image/*']" help="小程序应用图标" />
  </template>

  <!-- 应用资料 → 博主资料 -->
  <template v-if="subTab === 'blogger'">
    <FormKit v-model="formState.spec.profile.blogger.nickname" name="blogger_nickname" label="昵称" type="text" />
    <FormKit v-model="formState.spec.profile.blogger.email" name="blogger_email" label="邮箱" type="text" />
    <FormKit v-model="formState.spec.profile.blogger.website" name="blogger_website" label="主页" type="text" placeholder="如 https://your-site.com" />
    <FormKit v-model="formState.spec.profile.blogger.avatar" name="blogger_avatar" label="头像" type="attachment" :accepts="['image/*']" />
    <FormKit v-model="formState.spec.profile.blogger.description" name="blogger_description" label="简介" type="textarea" />
    <!-- 介绍（2026-09-10 新增：富文本 HTML，app 端联系博主页 mp-html 渲染） -->
    <div class=":uno: mt-4">
      <div class=":uno: mb-2 text-sm text-gray-700">介绍</div>
      <RichTextEditorField v-model="formState.spec.profile.blogger.intro" placeholder="博主介绍，支持图文混排……app 端「联系博主」页面展示，留空不展示" />
    </div>
  </template>

  <!-- 应用资料 → 社交信息（2026-09-10 起动态列表，去 enabled 开关） -->
  <template v-if="subTab === 'social'">
    <div class=":uno: flex items-center justify-between gap-4 pb-3">
      <div>
        <div class=":uno: text-sm text-gray-700">社交信息</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">app 端「联系博主」页面展示的社交方式，点击复制内容；拖拽排序，顺序即展示顺序</div>
      </div>
      <VButton size="sm" type="secondary" @click="addSocialItem">添加</VButton>
    </div>
    <VueDraggable v-model="socialItems" handle=".social-drag-handle">
      <div
        v-for="(item, index) in socialItems"
        :key="index"
        class=":uno: mb-2 last:mb-0"
      >
        <div class=":uno: flex flex-wrap items-center gap-x-3 gap-y-2 rounded-lg border border-gray-100 bg-gray-50 px-3 py-2">
          <span class=":uno: social-drag-handle cursor-move shrink-0 text-gray-400 hover:text-gray-600">
            <RiDragMove2Line class=":uno: h-4 w-4" />
          </span>
          <!-- 平台标识 key -->
          <div class=":uno: flex w-32 shrink-0 items-center gap-2">
            <span class=":uno: w-10 shrink-0 text-xs text-gray-700">标识</span>
            <FormKit
              v-model="item.key"
              :name="`social_key_${index}`"
              type="text"
              placeholder="如 qq"
              outer-class=":uno: min-w-0 flex-1 !pt-0"
            />
          </div>
          <!-- 名称 -->
          <div class=":uno: flex w-32 shrink-0 items-center gap-2">
            <span class=":uno: w-10 shrink-0 text-xs text-gray-700">名称</span>
            <FormKit
              v-model="item.name"
              :name="`social_name_${index}`"
              type="text"
              placeholder="如 企鹅号"
              outer-class=":uno: min-w-0 flex-1 !pt-0"
            />
          </div>
          <!-- 内容 -->
          <div class=":uno: flex min-w-0 flex-1 items-center gap-2 px-12">
            <span class=":uno: w-10 shrink-0 text-xs text-gray-700">内容</span>
            <FormKit
              v-model="item.content"
              :name="`social_content_${index}`"
              type="text"
              placeholder="账号 / 地址 / 链接"
              outer-class=":uno: min-w-0 flex-1 !pt-0"
            />
          </div>
          <!-- 图标颜色 -->
          <div class=":uno: flex shrink-0 items-center gap-2">
            <span class=":uno: text-xs text-gray-700">颜色</span>
            <FormKit
              type="color"
              format="hex8"
              :model-value="toColorInput(item.color)"
              @update:model-value="onSocialColor(item, $event)"
              outer-class=":uno: w-12 shrink-0 !pt-0"
            />
          </div>
          <!-- 背景色 -->
          <div class=":uno: flex shrink-0 items-center gap-2">
            <span class=":uno: text-xs text-gray-700">背景</span>
            <FormKit
              type="color"
              format="hex8"
              :model-value="toColorInput(item.bgColor)"
              @update:model-value="onSocialBgColor(item, $event)"
              outer-class=":uno: w-12 shrink-0 !pt-0"
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
            @click="removeSocialItem(item)"
          >
            <RiDeleteBinLine class=":uno: h-4 w-4" />
          </button>
        </div>
      </div>
    </VueDraggable>
    <VEmpty
      v-if="!socialItems.length"
      title="暂无社交信息"
      message="点击「添加」增加一条社交方式"
      :class="':uno: py-6'"
    />
  </template>

  <!-- 应用资料 → 页脚版权（2026-09-10 由页面设置-关于页迁回；显示于【关于】页面页脚） -->
  <template v-if="subTab === 'copyright'">
    <p class=":uno: mb-3 text-xs text-gray-400">
      小程序「关于」页面页脚展示的版权文案。
    </p>
    <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
      <div>
        <div class=":uno: text-sm text-gray-700">显示版权信息</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">小程序关于页页脚展示的版权文案</div>
      </div>
      <VSwitch v-model="formState.spec.profile.copyrightConfig!.enabled" />
    </div>
    <div class=":uno: mt-3">
      <FormKit v-model="formState.spec.profile.copyrightConfig!.content" name="copyright_content" label="版权内容" type="textarea" />
    </div>
  </template>
</template>

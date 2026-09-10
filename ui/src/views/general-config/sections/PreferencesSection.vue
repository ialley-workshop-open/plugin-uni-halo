<script setup lang="ts">
import { inject } from "vue";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 偏好设置分区（2026-09-10 组件化拆分）：
 * 首页 / 文章页面 / 归档页面 三个子 tab（L0 站点默认偏好，用户可在小程序端覆盖）。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;

/** 卡片样式选项（与客户端 hermes/preferences.md §3.2 组件 layout 值对齐，旧 lr_ 与 tb_ 值体系废弃） */
const CARD_STYLE_OPTIONS = [
  {label: "上图下文", value: "image_top"},
  {label: "左文右图", value: "image_right"},
  {label: "上文下图", value: "image_bottom"},
  {label: "左图右文", value: "image_left"},
];
</script>

<template>
  <!-- 偏好设置 → 首页 -->
  <template v-if="subTab === 'home'">
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

  <!-- 偏好设置 → 文章列表 -->
  <template v-if="subTab === 'articles'">
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

  <!-- 偏好设置 → 文章归档 -->
  <template v-if="subTab === 'archives'">
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
</template>

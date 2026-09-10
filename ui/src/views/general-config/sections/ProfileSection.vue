<script setup lang="ts">
import {VSwitch} from "@halo-dev/components";
import { inject } from "vue";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 应用设置分区（2026-09-10 组件化拆分）：
 * 应用信息 / 博主资料 / 社交信息 三个子 tab 表单。
 * 数据经 provide/inject 共享 formState，直接改嵌套属性触发父级 deep watch → dirty。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;
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
    <FormKit v-model="formState.spec.profile.blogger.website" name="blogger_website" label="官网地址" type="text" placeholder="如 https://your-site.com" help="博主官网/博客地址，友链信息-作者信息下线后小程序端作者区直接使用该地址" />
    <FormKit v-model="formState.spec.profile.blogger.avatar" name="blogger_avatar" label="头像" type="attachment" :accepts="['image/*']" />
    <FormKit v-model="formState.spec.profile.blogger.description" name="blogger_description" label="简介" type="textarea" />
  </template>

  <!-- 应用资料 → 社交信息 -->
  <template v-if="subTab === 'social'">
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
</template>

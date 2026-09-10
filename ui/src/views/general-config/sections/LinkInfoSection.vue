<script setup lang="ts">
import { computed, inject } from "vue";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 友链信息分区（2026-09-10 组件化拆分）：
 * 基本配置（公开提交申请开关，2026-09-11 迁入）/
 * 站点信息（本站站点名片，字段对齐 Halo 官方友链提交 API）/
 * 小程序信息（申请信息弹窗展示）。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;

/** 站点信息订阅地址（textarea 换行分隔 ↔ 数组，对齐官方 feedUrls 字段） */
const feedUrlsText = computed({
  get: () => (formState.value.spec.linkInfo.siteInfo?.feedUrls || []).join("\n"),
  set: (value: string) => {
    if (!formState.value.spec.linkInfo.siteInfo) {
      formState.value.spec.linkInfo.siteInfo = {};
    }
    const list = value
      .split("\n")
      .map((s) => s.trim())
      .filter(Boolean);
    formState.value.spec.linkInfo.siteInfo.feedUrls = list;
  },
});
</script>

<template>
  <!-- 友链信息 → 基本配置（2026-09-11 新增：公开提交申请开关，原 setting 基本设置-友情链接） -->
  <template v-if="subTab === 'basic'">
    <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
      <div>
        <div class=":uno: text-sm text-gray-700">开放公开提交申请</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">开启后 app 端可提交小程序链接申请，后台「链接管理-申请审核」进行审核</div>
      </div>
      <VSwitch v-model="formState.spec.linkInfo.submissionEnabled" />
    </div>
    <p class=":uno: mt-3 text-xs text-gray-400">
      关闭后小程序端「提交申请」入口隐藏，公开提交接口返回「暂未开放提交申请」。
    </p>
  </template>

  <!-- 友链信息 → 站点信息 -->
  <template v-if="subTab === 'site'">
    <p class=":uno: mb-3 text-xs text-gray-400">
      配置本站站点名片信息，字段对齐 Halo 官方链接管理插件（plugin-links）的友链提交 API（网站名称 / 地址 / Logo / 描述 / 反链 / 订阅地址），供展示与友链申请使用；留空的项不展示。
    </p>
    <FormKit v-model="formState.spec.linkInfo.siteInfo!.displayName" name="link_site_display_name" label="网站名称" type="text" placeholder="如「UniHalo 博客」" />
    <FormKit v-model="formState.spec.linkInfo.siteInfo!.url" name="link_site_url" label="网站地址" type="text" placeholder="如 https://your-site.com" help="HTTP/HTTPS 地址" />
    <FormKit v-model="formState.spec.linkInfo.siteInfo!.logo" name="link_site_logo" label="网站 Logo" type="attachment" :accepts="['image/*']" placeholder="选择或粘贴 Logo 图片地址" />
    <FormKit v-model="formState.spec.linkInfo.siteInfo!.description" name="link_site_description" label="网站描述" type="textarea" placeholder="一句话介绍你的网站" />
    <FormKit v-model="formState.spec.linkInfo.siteInfo!.backlink" name="link_site_backlink" label="反链地址" type="text" placeholder="如 https://your-site.com/links" help="友链页面地址（反链）" />
    <FormKit v-model="feedUrlsText" name="link_site_feed_urls" label="订阅地址" type="textarea" placeholder="每行一个 RSS/Atom 地址" help="RSS/Atom 订阅地址，每行一个（存储为数组，对齐官方 feedUrls 字段）" />
  </template>

  <!-- 友链信息 → 小程序信息 -->
  <template v-if="subTab === 'info'">
    <p class=":uno: mb-3 text-xs text-gray-400">
      配置你自己的小程序展示信息，用于 app 小程序端「申请信息」弹窗展示（小程序名称 / 太阳码 / 跳转地址 / 描述 / 申请说明）；留空的项不在弹窗中展示。
    </p>
    <FormKit v-model="formState.spec.linkInfo.miniInfo!.displayName" name="link_display_name" label="小程序名称" type="text" placeholder="如「UniHalo 博客」" />
    <FormKit v-model="formState.spec.linkInfo.miniInfo!.miniProgramCode" name="link_mini_program_code" label="太阳码/小程序码" type="attachment" :accepts="['image/*']" placeholder="选择或粘贴小程序码图片地址" help="小程序码图片，弹窗中点击可预览" />
    <FormKit v-model="formState.spec.linkInfo.miniInfo!.link" name="link_url" label="跳转地址" type="text" placeholder="如 #小程序://小莫唐尼/AGLiOpse2vi6QJC" help="说明：微信打开小程序，点击右上角三个点找到复制链接。" />
    <FormKit v-model="formState.spec.linkInfo.miniInfo!.description" name="link_description" label="小程序描述" type="textarea" placeholder="一句话介绍你的小程序，如「记录生活与技术的个人博客」" />
    <FormKit v-model="formState.spec.linkInfo.miniInfo!.applyRemark" name="link_apply_remark" label="申请说明" type="textarea" placeholder="如「欢迎友链互换，请附上你的网站信息」" help="弹窗中「申请说明」栏展示的文案" />
  </template>
</template>

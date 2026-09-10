<script setup lang="ts">
import { inject } from "vue";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 资源设置分区（2026-09-10 组件化拆分）：
 * 加载占位（加载中/加载失败两图；2026-09-08 起默认图片/空图片配置已下线）。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;
</script>

<template>
  <!-- 资源与兜底 → 加载占位 -->
  <template v-if="subTab === 'loading'">
    <p class=":uno: mb-3 text-xs text-gray-400">
      小程序端图片加载占位资源，留空由客户端内置回退；也可引用插件内置资源，如
      <code class=":uno: rounded bg-gray-100 px-1">/plugins/plugin-uni-halo/assets/res/uni_halo_img_lazyload.gif</code>
      （素材放插件 <code class=":uno: rounded bg-gray-100 px-1">src/main/resources/static/assets/</code>，同名替换即生效）。
    </p>
    <FormKit v-model="formState.spec.assets.loadingGifUrl" name="assets_loading_gif" label="加载中的图片" type="attachment" :accepts="['image/*']" />
    <FormKit v-model="formState.spec.assets.loadingErrUrl" name="assets_loading_err" label="加载失败图片" type="attachment" :accepts="['image/*']" />
  </template>
</template>

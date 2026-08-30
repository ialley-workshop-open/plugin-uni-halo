<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "./button/SubmitButton.vue";
import { appsApi } from "../api";
import type { AppInfo } from "../types";

const props = withDefaults(
  defineProps<{
    appInfo?: AppInfo;
  }>(),
  {
    appInfo: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const saving = ref(false);

const isUpdateMode = computed(() => !!props.appInfo);

const modalTitle = computed(() => (isUpdateMode.value ? "编辑应用" : "新建应用"));

const formState = ref<AppInfo>({
  metadata: {
    name: "",
  },
  spec: {
    appid: "",
    name: "",
    appType: 0,
    description: "",
    iconUrl: "",
    screenshot: [],
    appAndroid: { name: "", url: "" },
    appIos: { name: "", url: "" },
    appHarmony: { name: "", url: "" },
  },
});

watch(
  () => props.appInfo,
  (appInfo) => {
    if (appInfo) {
      formState.value = cloneDeep(appInfo);
    }
  },
  {
    immediate: true,
  }
);

const handleSave = async () => {
  if (!formState.value.spec.appid?.trim()) {
    Toast.error("AppID 不能为空");
    return;
  }
  if (!formState.value.spec.name?.trim()) {
    Toast.error("应用名称不能为空");
    return;
  }
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await appsApi.update(formState.value.metadata.name, formState.value);
    } else {
      await appsApi.create(formState.value);
    }
    modal.value?.close();
    Toast.success("保存成功");
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <VModal ref="modal" :title="modalTitle" :width="640" @close="emit('close')">
    <div class=":uno: flex flex-col gap-4">
      <div class=":uno: flex flex-col gap-3">
        <h4 class=":uno: m-0 text-sm font-semibold text-gray-700">基础信息</h4>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">AppID *</label>
          <input
            v-model="formState.spec.appid"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary disabled:bg-gray-100"
            placeholder="应用唯一标识（uni-app 的 appid）"
            :disabled="isUpdateMode"
          />
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">应用名称 *</label>
          <input
            v-model="formState.spec.name"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="应用名称"
          />
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">应用类型</label>
          <select
            v-model="formState.spec.appType"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
          >
            <option :value="0">uni-app</option>
            <option :value="1">uni-app x</option>
          </select>
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">应用描述</label>
          <textarea
            v-model="formState.spec.description"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            rows="3"
          />
        </div>
      </div>

      <div class=":uno: flex flex-col gap-3">
        <h4 class=":uno: m-0 text-sm font-semibold text-gray-700">应用素材</h4>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">图标地址</label>
          <input
            v-model="formState.spec.iconUrl"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="Halo 附件永久链接"
          />
        </div>
      </div>

      <div class=":uno: flex flex-col gap-3">
        <h4 class=":uno: m-0 text-sm font-semibold text-gray-700">平台信息</h4>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">Android</label>
          <input
            v-model="formState.spec.appAndroid!.name"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="名称"
          />
          <input
            v-model="formState.spec.appAndroid!.url"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="apk 下载地址"
          />
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">iOS</label>
          <input
            v-model="formState.spec.appIos!.name"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="名称"
          />
          <input
            v-model="formState.spec.appIos!.url"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="AppStore 链接"
          />
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">Harmony</label>
          <input
            v-model="formState.spec.appHarmony!.name"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="名称"
          />
          <input
            v-model="formState.spec.appHarmony!.url"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="下载地址"
          />
        </div>
      </div>
    </div>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
          :disabled="saving"
          type="secondary"
          text="提交"
          @submit="handleSave"
        />
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

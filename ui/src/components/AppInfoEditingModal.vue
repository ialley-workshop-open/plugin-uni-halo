<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
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
    description: "",
    intro: "",
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
      if (!formState.value.spec.screenshot) {
        formState.value.spec.screenshot = [];
      }
    }
  },
  {
    immediate: true,
  }
);

const handleSubmit = () => {
  submitForm("app-info-form");
};

const handleSave = async () => {
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
    <FormKit
      id="app-info-form"
      type="form"
      name="app-info-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        v-model="formState.spec.appid"
        name="appid"
        label="AppID"
        type="text"
        validation="required"
        :validation-messages="{ required: 'AppID 不能为空' }"
        placeholder="应用唯一标识（uni-app 的 appid）"
        :disabled="isUpdateMode"
      />
      <FormKit
        v-model="formState.spec.name"
        name="name"
        label="应用名称"
        type="text"
        validation="required"
        :validation-messages="{ required: '应用名称不能为空' }"
        placeholder="应用名称"
      />
      <FormKit
        v-model="formState.spec.description"
        name="description"
        label="应用简介"
        type="textarea"
        rows="3"
        placeholder="简要描述该应用"
      />
      <FormKit
        v-model="formState.spec.intro"
        name="intro"
        label="应用介绍"
        type="textarea"
        rows="5"
        placeholder="详细介绍该应用的功能与特性"
      />
      <FormKit
        v-model="formState.spec.iconUrl"
        name="iconUrl"
        label="应用图标"
        type="attachment"
        :accepts="['image/*']"
        help="支持从附件库选择图片，或直接输入图片地址"
      />
      <FormKit
        v-model="formState.spec.screenshot"
        name="screenshot"
        label="应用截图"
        type="attachment"
        multiple
        :accepts="['image/*']"
        help="支持多张截图，可从附件库选择或直接上传"
      />
      <div class=":uno: mt-4 rounded-md border border-gray-200 p-4">
        <div class=":uno: mb-2">
          <span class=":uno: text-sm font-semibold text-gray-700">
            Android 平台信息
            <span class=":uno: ml-1 text-xs font-normal text-gray-400">(可选)</span>
          </span>
        </div>
        <p class=":uno: mb-3 text-xs text-gray-500">
          该配置更新的时候用不到，仅做基础信息，目的是方便在其他地方调用展示而已。
        </p>
        <FormKit
          v-model="formState.spec.appAndroid!.name"
          name="appAndroidName"
          label="Android 名称"
          type="text"
        />
        <FormKit
          v-model="formState.spec.appAndroid!.url"
          name="appAndroidUrl"
          label="Android 下载地址"
          type="attachment"
          :accepts="['.apk']"
          help="仅支持上传 .apk 格式文件，或直接输入下载地址"
        />
      </div>
    </FormKit>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
          :disabled="saving"
          type="secondary"
          text="提交"
          @submit="handleSubmit"
        />
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

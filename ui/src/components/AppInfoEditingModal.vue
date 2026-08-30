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
        v-model="formState.spec.appType"
        name="appType"
        label="应用类型"
        type="select"
        :options="[
          { label: 'uni-app', value: 0 },
          { label: 'uni-app x', value: 1 },
        ]"
      />
      <FormKit
        v-model="formState.spec.description"
        name="description"
        label="应用描述"
        type="textarea"
        rows="3"
      />
      <FormKit
        v-model="formState.spec.iconUrl"
        name="iconUrl"
        label="图标地址"
        type="text"
        placeholder="Halo 附件永久链接"
      />
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
        type="text"
        placeholder="apk 下载地址"
      />
      <FormKit
        v-model="formState.spec.appIos!.name"
        name="appIosName"
        label="iOS 名称"
        type="text"
      />
      <FormKit
        v-model="formState.spec.appIos!.url"
        name="appIosUrl"
        label="iOS 链接"
        type="text"
        placeholder="AppStore 链接"
      />
      <FormKit
        v-model="formState.spec.appHarmony!.name"
        name="appHarmonyName"
        label="Harmony 名称"
        type="text"
      />
      <FormKit
        v-model="formState.spec.appHarmony!.url"
        name="appHarmonyUrl"
        label="Harmony 下载地址"
        type="text"
      />
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

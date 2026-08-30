<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "./button/SubmitButton.vue";
import { appVersionsApi } from "../api";
import { PLATFORMS } from "../types";
import type { AppInfo, AppVersion } from "../types";

const props = withDefaults(
  defineProps<{
    appVersion?: AppVersion;
    apps: AppInfo[];
  }>(),
  {
    appVersion: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);

const saving = ref(false);

const isUpdateMode = computed(() => !!props.appVersion);

const modalTitle = computed(() => (isUpdateMode.value ? "编辑版本" : "发布新版"));

const selectedAppid = ref("");

const appOptions = computed(() => {
  return props.apps.map((app) => ({
    label: `${app.spec.name || ""}（${app.spec.appid || ""}）`,
    value: app.spec.appid || "",
  }));
});

const formState = ref<AppVersion>({
  metadata: {
    name: "",
  },
  spec: {
    appid: "",
    name: "",
    title: "",
    contents: "",
    platform: [],
    type: "native_app",
    version: "",
    minUniVersion: "",
    url: "",
    stablePublish: true,
    isSilently: false,
    isMandatory: false,
  },
});

// 回填 / 初始化时同步所属应用下拉
watch(
  () => props.appVersion,
  (appVersion) => {
    if (appVersion) {
      formState.value = cloneDeep(appVersion);
      selectedAppid.value = appVersion.spec.appid || "";
    }
  },
  {
    immediate: true,
  }
);

watch(selectedAppid, (appid) => {
  const app = props.apps.find((item) => item.spec.appid === appid);
  formState.value.spec.appid = appid;
  formState.value.spec.name = app?.spec.name || "";
});

const handleSubmit = () => {
  submitForm("app-version-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await appVersionsApi.update(formState.value.metadata.name, formState.value);
    } else {
      await appVersionsApi.create(formState.value);
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
      id="app-version-form"
      type="form"
      name="app-version-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        v-model="selectedAppid"
        name="appid"
        label="所属应用"
        type="select"
        validation="required"
        :validation-messages="{ required: '请选择所属应用' }"
        :options="[{ label: '请选择应用', value: '' }, ...appOptions]"
      />
      <FormKit
        v-model="formState.spec.title"
        name="title"
        label="更新标题"
        type="text"
        validation="required"
        :validation-messages="{ required: '更新标题不能为空' }"
        placeholder="例如：v1.1.0 新版本"
      />
      <FormKit
        v-model="formState.spec.contents"
        name="contents"
        label="更新内容"
        type="textarea"
        rows="4"
        placeholder="更新内容（可换行）"
      />
      <FormKit
        v-model="formState.spec.platform"
        name="platform"
        label="更新平台"
        type="checkbox"
        :options="PLATFORMS.map((p) => ({ label: p, value: p }))"
      />
      <FormKit
        v-model="formState.spec.type"
        name="type"
        label="包类型"
        type="select"
        :options="[
          { label: '整包（native_app）', value: 'native_app' },
          { label: 'wgt 资源包', value: 'wgt' },
        ]"
      />
      <FormKit
        v-model="formState.spec.version"
        name="version"
        label="版本号"
        type="text"
        validation="required"
        :validation-messages="{ required: '版本号不能为空' }"
        placeholder="须大于当前线上发行版本，如 1.1.0"
      />
      <FormKit
        v-if="formState.spec.type === 'wgt'"
        v-model="formState.spec.minUniVersion"
        name="minUniVersion"
        label="min_uni_version"
        type="text"
        placeholder="wgt 所需最低原生 App 版本"
      />
      <FormKit
        v-model="formState.spec.url"
        name="url"
        label="下载地址"
        type="text"
        placeholder="Android/Harmony 附件或下载地址；iOS 填 AppStore 链接"
      />
      <FormKit
        v-model="formState.spec.stablePublish"
        name="stablePublish"
        label="上线发行（同应用同平台仅一个上线版本）"
        type="checkbox"
      />
      <FormKit
        v-model="formState.spec.isMandatory"
        name="isMandatory"
        label="强制更新"
        type="checkbox"
      />
      <FormKit
        v-if="formState.spec.type === 'wgt'"
        v-model="formState.spec.isSilently"
        name="isSilently"
        label="静默更新（仅 wgt）"
        type="checkbox"
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

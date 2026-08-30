<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "./button/SubmitButton.vue";
import { appVersionsApi } from "../api";
import type { AppInfo, AppVersion } from "../types";

const props = withDefaults(
  defineProps<{
    appVersion?: AppVersion;
    apps: AppInfo[];
    initialAppid?: string;
  }>(),
  {
    appVersion: undefined,
    initialAppid: "",
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
    platform: ["Android"],
    type: "native_app",
    version: "",
    minUniVersion: "",
    url: "",
    stablePublish: true,
    isSilently: false,
    isMandatory: false,
  },
});

// 当前选中应用的最新版本（用于版本号提示）
const latestVersion = ref("");

// 下载地址按包类型约束文件格式：整包仅 apk，wgt 仅 wgt
const urlAccepts = computed(() => {
  return formState.value.spec.type === "wgt" ? [".wgt"] : [".apk"];
});

watch(
  [selectedAppid],
  async () => {
    latestVersion.value = "";
    if (!selectedAppid.value) {
      return;
    }
    try {
      const result = await appVersionsApi.list({
        appid: selectedAppid.value,
        page: 1,
        size: 1,
      });
      const first = result.items[0];
      if (first?.spec?.version) {
        latestVersion.value = first.spec.version;
      }
    } catch (error) {
      console.error("Failed to fetch latest version", error);
    }
  },
  {
    immediate: false,
  }
);

// 回填 / 初始化时同步所属应用下拉
watch(
  () => props.appVersion,
  (appVersion) => {
    if (appVersion) {
      formState.value = cloneDeep(appVersion);
      // 平台仅保留 Android（iOS / Harmony 已隐藏）
      if (formState.value.spec.platform?.length) {
        formState.value.spec.platform = formState.value.spec.platform.filter(
          (p) => p === "Android"
        );
      }
      selectedAppid.value = appVersion.spec.appid || "";
    } else if (props.initialAppid) {
      // 发布新版：预选所属应用
      selectedAppid.value = props.initialAppid;
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
        :options="[{ label: 'Android', value: 'Android' }]"
      />
      <FormKit
        v-model="formState.spec.type"
        name="type"
        label="包类型"
        type="select"
        :options="[
          { label: '整包', value: 'native_app' },
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
        help="当前应用最新版本："
      />
      <FormKit
        v-if="formState.spec.type === 'wgt'"
        v-model="formState.spec.minUniVersion"
        name="minUniVersion"
        label="最低原生版本"
        type="text"
        placeholder="例如：3.0.0"
        help="指支持该 wgt 资源包的最低原生 App 版本号。客户端原生 App 版本低于此值时无法应用此 wgt 更新，需通过整包更新升级。"
      />
      <FormKit
        v-model="formState.spec.url"
        name="url"
        label="下载地址"
        type="attachment"
        :accepts="urlAccepts"
        :help="formState.spec.type === 'wgt'
          ? '仅支持上传 .wgt 格式文件，或直接输入下载地址'
          : '仅支持上传 .apk 格式文件，或直接输入下载地址'"
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

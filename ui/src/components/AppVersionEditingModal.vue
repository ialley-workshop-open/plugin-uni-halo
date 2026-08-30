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
    versionCode: undefined,
    minUniVersion: "",
    url: "",
    stablePublish: true,
    isSilently: false,
    isMandatory: false,
  },
});

// 简单版本号比较（按 . 分段数字比较）
const compareVersions = (a: string, b: string): number => {
  if (!b) {
    return a ? 1 : 0;
  }
  const pa = a.split(".").map(Number);
  const pb = b.split(".").map(Number);
  for (let i = 0; i < Math.max(pa.length, pb.length); i++) {
    const na = pa[i] || 0;
    const nb = pb[i] || 0;
    if (na !== nb) {
      return na - nb;
    }
  }
  return 0;
};

// 当前选中应用上次发布的信息（名称 + 版本号，用于填写提示）
const latestVersion = ref("");
const latestVersionCode = ref<number>();

// 下载地址按包类型约束文件格式：整包仅 apk，wgt 仅 wgt。
// 注意：Halo attachment 的 accepts 按 MIME 类型匹配（附件库 mediaType），
// 不能用扩展名（.apk/.wgt），否则附件库中对应格式文件无法显示。
// 整包不能包含 application/octet-stream：wgt 文件在附件库中的 mediaType 就是
// application/octet-stream，会导致整包时 apk 与 wgt 同时被匹配。
const urlAccepts = computed(() => {
  return formState.value.spec.type === "wgt"
    ? ["application/octet-stream", "application/zip"]
    : ["application/vnd.android.package-archive"];
});

watch(
  [selectedAppid],
  async () => {
    latestVersion.value = "";
    latestVersionCode.value = undefined;
    if (!selectedAppid.value) {
      return;
    }
    try {
      const result = await appVersionsApi.list({
        appid: selectedAppid.value,
        stablePublish: "true",
        page: 1,
        size: 50,
      });
      let maxName = "";
      let maxCode: number | undefined;
      for (const item of result.items || []) {
        const name = item.spec?.version;
        const code = item.spec?.versionCode;
        if (!name && code === undefined) {
          continue;
        }
        const currentCode = code ?? -1;
        const bestCode = maxCode ?? -1;
        if (currentCode > bestCode
            || (currentCode === bestCode && compareVersions(name || "", maxName) > 0)) {
          maxName = name || "";
          maxCode = code;
        }
      }
      latestVersion.value = maxName;
      latestVersionCode.value = maxCode;
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
        label="应用版本名称"
        type="text"
        validation="required"
        :validation-messages="{ required: '应用版本名称不能为空' }"
        placeholder="例如：1.1.0"
        :help="latestVersion
          ? `上次发布：${latestVersion}${latestVersionCode !== undefined ? `（版本号 ${latestVersionCode}）` : ''}，须大于该值`
          : '当前暂无已上线的版本'"
      />
      <FormKit
        v-model="formState.spec.versionCode"
        name="versionCode"
        label="应用版本号"
        type="number"
        validation="required"
        :validation-messages="{ required: '应用版本号不能为空' }"
        placeholder="整数，例如：110"
        help="整数数值，须大于该应用已发布的最大版本号"
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
        validation="required"
        :validation-messages="{ required: '下载地址不能为空' }"
        :accepts="urlAccepts"
        :help="formState.spec.type === 'wgt'
          ? '仅支持上传 .wgt 格式文件，或直接输入下载地址'
          : '仅支持上传 .apk 格式文件，或直接输入下载地址'"
      />
      <FormKit
        v-model="formState.spec.url"
        name="urlPreview"
        label="地址预览"
        type="text"
        disabled
        placeholder="选择附件后将在此显示下载地址"
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

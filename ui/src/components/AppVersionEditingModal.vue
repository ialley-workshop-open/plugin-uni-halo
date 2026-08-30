<script lang="ts" setup>
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
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

const handleSave = async () => {
  if (!formState.value.spec.appid?.trim()) {
    Toast.error("请选择所属应用");
    return;
  }
  if (!formState.value.spec.title?.trim()) {
    Toast.error("更新标题不能为空");
    return;
  }
  if (!formState.value.spec.version?.trim()) {
    Toast.error("版本号不能为空");
    return;
  }
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
    <div class=":uno: flex flex-col gap-4">
      <div class=":uno: flex flex-col gap-3">
        <h4 class=":uno: m-0 text-sm font-semibold text-gray-700">基本信息</h4>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">所属应用 *</label>
          <select
            v-model="selectedAppid"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
          >
            <option value="">请选择应用</option>
            <option v-for="app in apps" :key="app.metadata.name" :value="app.spec.appid">
              {{ app.spec.name }}（{{ app.spec.appid }}）
            </option>
          </select>
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">更新标题 *</label>
          <input
            v-model="formState.spec.title"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="例如：v1.1.0 新版本"
          />
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">更新内容</label>
          <textarea
            v-model="formState.spec.contents"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            rows="4"
            placeholder="更新内容（可换行）"
          />
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">更新平台</label>
          <VSpace>
            <label
              v-for="platform in PLATFORMS"
              :key="platform"
              class=":uno: inline-flex items-center gap-1.5 text-sm text-gray-700"
            >
              <input v-model="formState.spec.platform" type="checkbox" :value="platform" />
              {{ platform }}
            </label>
          </VSpace>
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">包类型</label>
          <select
            v-model="formState.spec.type"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
          >
            <option value="native_app">整包（native_app）</option>
            <option value="wgt">wgt 资源包</option>
          </select>
        </div>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">版本号 *</label>
          <input
            v-model="formState.spec.version"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="须大于当前线上发行版本，如 1.1.0"
          />
        </div>
        <div v-if="formState.spec.type === 'wgt'" class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">min_uni_version</label>
          <input
            v-model="formState.spec.minUniVersion"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="wgt 所需最低原生 App 版本"
          />
        </div>
      </div>

      <div class=":uno: flex flex-col gap-3">
        <h4 class=":uno: m-0 text-sm font-semibold text-gray-700">安装包</h4>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: text-[13px] text-gray-500">下载地址</label>
          <input
            v-model="formState.spec.url"
            class=":uno: w-full rounded border border-gray-300 px-3 py-2 text-sm outline-none focus:border-primary"
            placeholder="Android/Harmony 附件或下载地址；iOS 填 AppStore 链接"
          />
        </div>
      </div>

      <div class=":uno: flex flex-col gap-3">
        <h4 class=":uno: m-0 text-sm font-semibold text-gray-700">发布策略</h4>
        <div class=":uno: flex flex-col gap-1.5">
          <label class=":uno: inline-flex items-center gap-1.5 text-sm text-gray-700">
            <input v-model="formState.spec.stablePublish" type="checkbox" />
            上线发行（同应用同平台仅一个上线版本）
          </label>
          <label class=":uno: inline-flex items-center gap-1.5 text-sm text-gray-700">
            <input v-model="formState.spec.isMandatory" type="checkbox" />
            强制更新
          </label>
          <label
            v-if="formState.spec.type === 'wgt'"
            class=":uno: inline-flex items-center gap-1.5 text-sm text-gray-700"
          >
            <input v-model="formState.spec.isSilently" type="checkbox" />
            静默更新（仅 wgt）
          </label>
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

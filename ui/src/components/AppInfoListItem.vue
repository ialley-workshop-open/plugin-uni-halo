<script lang="ts" setup>
import {
  VButton,
  VEntity,
  VEntityField,
  VStatusDot,
  VDropdownItem,
  VModal,
  VSpace,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useWindowSize } from "@vueuse/core";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import type { AppInfo } from "../types";

const props = withDefaults(
  defineProps<{
    app: AppInfo;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "editing", app: AppInfo): void;
  (event: "delete", app: AppInfo): void;
}>();

const router = useRouter();

const previewVisible = ref(false);

// 预览弹窗：宽度 80vw（VModal width 为 px 数值，动态计算）、高度 60vh
const { width: windowWidth } = useWindowSize();
const previewWidth = computed(() => Math.round(windowWidth.value * 0.8));

const createdText = computed(() => {
  return props.app.metadata.creationTimestamp
    ? utils.date.format(props.app.metadata.creationTimestamp)
    : "";
});

const routeToVersions = () => {
  router.push({
    name: "AppVersionList",
    query: { appid: props.app.spec.appid },
  });
};
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template #checkbox>
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField :width="'4rem'">
        <template #description>
          <img
            v-if="app.spec.iconUrl"
            :src="app.spec.iconUrl"
            class=":uno: h-10 w-10 cursor-pointer rounded object-cover hover:opacity-80"
            alt=""
            @click="previewVisible = true"
          />
          <div v-else class=":uno: h-10 w-10 rounded bg-gray-100" />
        </template>
      </VEntityField>
      <VEntityField :title="app.spec.appid || app.metadata.name" width="15rem">
        <template #description>
          <VSpace class=":uno: flex-wrap">
            <span class=":uno: truncate text-xs tabular-nums text-gray-500">
              {{ app.spec.name }}
            </span>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField v-if="app.spec.description">
        <template #description>
          <span class=":uno: truncate text-xs tabular-nums text-gray-500">
            {{ app.spec.description }}
          </span>
        </template>
      </VEntityField>
      <VEntityField>
        <template #description>
          <VStatusDot state="success" text="已配置" />
        </template>
      </VEntityField>
      <VEntityField :description="createdText" />
      <VEntityField>
        <template #description>
          <VButton size="sm" type="secondary" @click="routeToVersions">
            版本管理
          </VButton>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem @click="routeToVersions">
        版本管理
      </VDropdownItem>
      <VDropdownItem @click="emit('editing', app)">
        编辑
      </VDropdownItem>
      <VDropdownItem type="danger" @click="emit('delete', app)">
        删除
      </VDropdownItem>
    </template>
  </VEntity>

  <VModal
    v-model:visible="previewVisible"
    :title="app.spec.name || app.spec.appid"
    :width="previewWidth"
    height="80vh"
    :body-class="[':uno: !p-4']"
  >
    <img
      v-if="app.spec.iconUrl"
      :src="app.spec.iconUrl"
      class=":uno: h-full w-full object-contain"
      alt=""
    />
  </VModal>
</template>

<script lang="ts" setup>
import {
  VButton,
  VEntity,
  VEntityField,
  VStatusDot,
  VDropdownItem,
  VSpace,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { computed } from "vue";
import type { AppVersion } from "../types";
import { TYPE_LABELS } from "../types";

const props = withDefaults(
  defineProps<{
    version: AppVersion;
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const emit = defineEmits<{
  (event: "publish", appid: string): void;
  (event: "editing", version: AppVersion): void;
  (event: "toggle", version: AppVersion): void;
  (event: "delete", version: AppVersion): void;
}>();

const titleText = computed(() => {
  return props.version.spec.title || props.version.spec.version || props.version.metadata.name;
});

const typeText = computed(() => {
  return TYPE_LABELS[props.version.spec.type || ""] || props.version.spec.type || "未知";
});

const platformText = computed(() => {
  return props.version.spec.platform?.join(" / ") || "";
});

const publishedText = computed(() => {
  return props.version.metadata.creationTimestamp
    ? utils.date.format(props.version.metadata.creationTimestamp)
    : "";
});
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template #checkbox>
      <slot name="checkbox" />
    </template>
    <template #start>
      <VEntityField :title="titleText" width="15rem">
        <template #description>
          <VSpace class=":uno: flex-wrap">
            <span class=":uno: truncate text-xs tabular-nums text-gray-500">
              {{ version.spec.appid }}
            </span>
            <span v-if="version.spec.version" class=":uno: truncate text-xs tabular-nums text-gray-500">
              v{{ version.spec.version }}
            </span>
          </VSpace>
        </template>
      </VEntityField>
    </template>
    <template #end>
      <VEntityField :description="typeText" />
      <VEntityField v-if="platformText" :description="platformText" />
      <VEntityField>
        <template #description>
          <VStatusDot
            :state="version.spec.stablePublish ? 'success' : 'default'"
            :animate="!!version.spec.stablePublish"
            :text="version.spec.stablePublish ? '已上线' : '已下线'"
          />
        </template>
      </VEntityField>
      <VEntityField v-if="version.spec.isMandatory">
        <template #description>
          <VStatusDot state="error" text="强制更新" />
        </template>
      </VEntityField>
      <VEntityField v-else-if="version.spec.isSilently">
        <template #description>
          <VStatusDot state="warning" text="静默更新" />
        </template>
      </VEntityField>
      <VEntityField :description="publishedText" />
      <VEntityField>
        <template #description>
          <VButton size="sm" type="secondary" @click="emit('publish', version.spec.appid || '')">
            发布新版
          </VButton>
        </template>
      </VEntityField>
    </template>
    <template #dropdownItems>
      <VDropdownItem @click="emit('publish', version.spec.appid || '')">
        发布新版
      </VDropdownItem>
      <VDropdownItem @click="emit('toggle', version)">
        {{ version.spec.stablePublish ? "下线" : "上线" }}
      </VDropdownItem>
      <VDropdownItem @click="emit('editing', version)">
        编辑
      </VDropdownItem>
      <VDropdownItem type="danger" @click="emit('delete', version)">
        删除
      </VDropdownItem>
    </template>
  </VEntity>
</template>

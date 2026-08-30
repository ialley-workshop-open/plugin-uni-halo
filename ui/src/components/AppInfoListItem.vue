<script lang="ts" setup>
import {
  VEntity,
  VEntityField,
  VStatusDot,
  VDropdownItem,
  VSpace,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { computed } from "vue";
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

const appTypeText = computed(() => {
  return props.app.spec.appType === 1 ? "uni-app x" : "uni-app";
});

const createdText = computed(() => {
  return props.app.metadata.creationTimestamp
    ? utils.date.format(props.app.metadata.creationTimestamp)
    : "";
});
</script>

<template>
  <VEntity :is-selected="isSelected">
    <template #start>
      <VEntityField :title="app.spec.appid || app.metadata.name" width="15rem">
        <template #description>
          <VSpace class=":uno: flex-wrap">
            <span class=":uno: truncate text-xs tabular-nums text-gray-500">
              {{ app.spec.name }}
            </span>
            <span class=":uno: truncate text-xs tabular-nums text-gray-500">
              {{ appTypeText }}
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
    </template>
    <template #dropdownItems>
      <VDropdownItem @click="emit('editing', app)">
        编辑
      </VDropdownItem>
      <VDropdownItem type="danger" @click="emit('delete', app)">
        删除
      </VDropdownItem>
    </template>
  </VEntity>
</template>

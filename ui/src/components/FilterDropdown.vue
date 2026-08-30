<script lang="ts" setup>
import { VButton, VDropdown, VDropdownItem } from "@halo-dev/components";
import { computed } from "vue";

export interface FilterDropdownItem {
  label: string;
  value?: string;
}

const props = withDefaults(
  defineProps<{
    label: string;
    items: FilterDropdownItem[];
    modelValue?: string;
  }>(),
  {
    modelValue: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const currentLabel = computed(() => {
  const found = props.items.find((item) => item.value === props.modelValue);
  return found?.label || props.label;
});

const isActive = computed(() => !!props.modelValue);

function select(item: FilterDropdownItem) {
  // 点击当前已选项时清空（回到「全部」）
  const next = item.value === props.modelValue ? undefined : item.value;
  emit("update:modelValue", next);
}
</script>

<template>
  <VDropdown :triggers="['click']">
    <VButton size="sm" :type="isActive ? 'primary' : 'secondary'">
      {{ currentLabel }}
    </VButton>
    <template #popper>
      <VDropdownItem
        v-for="item in items"
        :key="item.label"
        :selected="item.value === modelValue"
        @click="select(item)"
      >
        {{ item.label }}
      </VDropdownItem>
    </template>
  </VDropdown>
</template>

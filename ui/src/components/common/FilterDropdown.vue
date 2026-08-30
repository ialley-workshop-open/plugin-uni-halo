<script lang="ts" setup>
import { IconArrowDown, VDropdown, VDropdownItem } from "@halo-dev/components";
import { computed } from "vue";

export interface FilterDropdownItem {
  label: string;
  value?: string | boolean | number;
}

const props = withDefaults(
  defineProps<{
    label: string;
    items: FilterDropdownItem[];
    modelValue?: string | boolean | number;
  }>(),
  {
    modelValue: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", modelValue: string | boolean | number | undefined): void;
}>();

const selectedItem = computed(() => {
  return props.items.find((item) => item.value === props.modelValue);
});

function handleSelect(item: FilterDropdownItem) {
  if (item.value === props.modelValue) {
    emit("update:modelValue", undefined);
    return;
  }
  emit("update:modelValue", item.value);
}
</script>

<template>
  <VDropdown>
    <div
      class=":uno: flex cursor-pointer select-none items-center text-sm text-gray-700 hover:text-black"
      :class="{ ':uno: font-semibold text-gray-700': modelValue !== undefined }"
    >
      <span v-if="!selectedItem" class=":uno: mr-0.5">
        {{ label }}
      </span>
      <span v-else class=":uno: mr-0.5"> {{ label }}：{{ selectedItem.label }} </span>
      <span>
        <IconArrowDown />
      </span>
    </div>
    <template #popper>
      <VDropdownItem
        v-for="(item, index) in items"
        :key="index"
        :selected="item.value === modelValue"
        @click="handleSelect(item)"
      >
        {{ item.label }}
      </VDropdownItem>
    </template>
  </VDropdown>
</template>

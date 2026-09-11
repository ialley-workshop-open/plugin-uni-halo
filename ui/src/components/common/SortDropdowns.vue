<script setup lang="ts">
import FilterDropdown from "@/components/common/FilterDropdown.vue";

/** 排序维度选项 */
export interface SortFieldOption {
  label: string;
  /** 选项值（对应后端 sort 参数；缺省=默认/清除） */
  value?: string;
}

/** 排序维度定义 */
export interface SortField {
  /** 维度标识（唯一） */
  key: string;
  /** 维度展示名（如 申请时间） */
  label: string;
  /** 维度选项；缺省按「默认/倒序」两项（倒序 value 取 key） */
  options?: SortFieldOption[];
}

const props = withDefaults(
  defineProps<{
    /** 排序维度列表（每个维度渲染一个下拉） */
    fields: SortField[];
    /** 当前生效的排序值（即后端 sort 参数；undefined=默认，互斥单值） */
    modelValue?: string;
  }>(),
  {
    modelValue: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string | undefined): void;
}>();

/** 维度下拉选项（缺省时默认/倒序两项） */
const optionsOf = (field: SortField): SortFieldOption[] =>
  field.options ?? [
    { label: "默认" },
    { label: "倒序", value: field.key },
  ];

/** 维度下拉当前值：modelValue 命中该维度某选项 → 该值；否则 undefined（未激活，显示维度名） */
const currentValueOf = (field: SortField) => {
  if (props.modelValue === undefined) {
    return undefined;
  }
  return optionsOf(field).some((option) => option.value === props.modelValue)
    ? props.modelValue
    : undefined;
};
</script>

<template>
  <FilterDropdown
    v-for="field in fields"
    :key="field.key"
    :label="field.label"
    :items="optionsOf(field)"
    :model-value="currentValueOf(field)"
    @update:model-value="(value) => emit('update:modelValue', typeof value === 'string' ? value : undefined)"
  />
</template>

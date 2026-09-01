<script setup lang="ts">
import { Sketch } from "@ckpack/vue-color";
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { noticeTypeApi } from "@/api";
import type { NoticeType } from "@/types";

const props = withDefaults(
  defineProps<{
    item?: NoticeType;
  }>(),
  {
    item: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  /** 保存成功（供父组件刷新类型选项并自动选中） */
  (event: "saved", type: NoticeType): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);
const colorPickerVisible = ref(false);

const isUpdateMode = computed(() => !!props.item);

const formState = ref<NoticeType>({
  metadata: { name: "" },
  spec: {
    displayName: "",
    color: "#10B981",
    priority: 0,
  },
});

watch(
  () => props.item,
  (item) => {
    if (item) {
      formState.value = cloneDeep(item);
      if (!formState.value.spec.color) {
        formState.value.spec.color = "#10B981";
      }
    }
  },
  {
    immediate: true,
  }
);

const colorValue = computed(() => {
  const color = formState.value.spec.color;
  return color && /^#[0-9a-fA-F]{3,8}$/.test(color) ? color : "#cccccc";
});

// SketchPicker 双向绑定对象（内部含 hex/rgba/hsv）
const colorObj = ref<{ hex: string }>({ hex: colorValue.value });

watch(
  () => formState.value.spec.color,
  (value) => {
    const next = value && /^#[0-9a-fA-F]{3,8}$/.test(value) ? value : "#cccccc";
    if (colorObj.value.hex !== next) {
      colorObj.value = { hex: next };
    }
  }
);

const handleColorChange = (value: { hex: string }) => {
  formState.value.spec.color = value.hex;
};

const handleSubmit = () => {
  submitForm("notice-type-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    let saved: NoticeType;
    if (isUpdateMode.value) {
      saved = await noticeTypeApi.update(formState.value.metadata.name, formState.value);
    } else {
      saved = await noticeTypeApi.create(formState.value);
    }
    modal.value?.close();
    emit("saved", saved);
    Toast.success("保存成功");
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <VModal
    ref="modal"
    :title="isUpdateMode ? '编辑公告类型' : '新建公告类型'"
    :width="480"
    @close="emit('close')"
  >
    <FormKit
      id="notice-type-form"
      type="form"
      name="notice-type-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        v-model="formState.spec.displayName"
        name="displayName"
        label="类型名称"
        type="text"
        validation="required"
        :validation-messages="{ required: '类型名称不能为空' }"
        placeholder="例如：活动、维护、更新"
      />
      <div class=":uno: flex items-end gap-2">
        <FormKit
          v-model="formState.spec.color"
          name="color"
          label="标签颜色"
          type="text"
          help="支持手动输入 hex，或点击右侧按钮从色板选择"
          placeholder="#10B981"
          wrapper-class=":uno: min-w-0 flex-1"
        />
        <VButton
          class=":uno: mb-1 shrink-0"
          type="secondary"
          size="sm"
          :style="{ backgroundColor: colorValue }"
          title="从色板选择颜色"
          @click="colorPickerVisible = !colorPickerVisible"
        >
          色板
        </VButton>
      </div>
      <div v-if="colorPickerVisible" class=":uno: relative mb-4">
        <div
          class=":uno: absolute left-0 top-0 z-10 rounded-md border border-gray-200 bg-white p-3 shadow-lg"
        >
          <Sketch
            :model-value="colorObj"
            @update:model-value="handleColorChange"
          />
        </div>
      </div>
      <FormKit
        v-model="formState.spec.priority"
        name="priority"
        label="排序"
        type="number"
        help="数值越大越靠前"
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

<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { miniProgramLinkGroupsApi } from "@/api";
import type { MiniProgramLinkGroup } from "@/types";

const props = withDefaults(
  defineProps<{
    item?: MiniProgramLinkGroup;
  }>(),
  {
    item: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
  /** 保存成功（供父组件刷新分组并自动选中） */
  (event: "saved", group: MiniProgramLinkGroup): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);

const isUpdateMode = computed(() => !!props.item);

const formState = ref<MiniProgramLinkGroup>({
  metadata: { name: "" },
  spec: {
    displayName: "",
    priority: 0,
  },
});

watch(
  () => props.item,
  (item) => {
    if (item) {
      formState.value = cloneDeep(item);
      if (formState.value.spec.priority === undefined) {
        formState.value.spec.priority = 0;
      }
    }
  },
  {
    immediate: true,
  }
);

const handleSubmit = () => {
  submitForm("link-group-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    let saved: MiniProgramLinkGroup;
    if (isUpdateMode.value) {
      saved = await miniProgramLinkGroupsApi.update(
        formState.value.metadata.name,
        formState.value
      );
    } else {
      saved = await miniProgramLinkGroupsApi.create(formState.value);
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
    :title="isUpdateMode ? '编辑分组' : '新建分组'"
    :width="480"
    @close="emit('close')"
  >
    <FormKit
      id="link-group-form"
      type="form"
      name="link-group-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        v-model="formState.spec.displayName"
        name="displayName"
        label="分组名称"
        type="text"
        validation="required"
        :validation-messages="{ required: '分组名称不能为空' }"
        placeholder="例如：工具、生活"
      />
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

<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "../../button/SubmitButton.vue";
import { loveStoryApi } from "@/api";
import type { LoveStory } from "@/types";

const props = withDefaults(
  defineProps<{
    story?: LoveStory;
  }>(),
  {
    story: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);

const isUpdateMode = computed(() => !!props.story);

const formState = ref<LoveStory>({
  metadata: { name: "" },
  spec: { title: "", content: "", date: "", location: "", images: [], priority: 0 },
});

watch(
  () => props.story,
  (story) => {
    if (story) {
      formState.value = cloneDeep(story);
      if (!formState.value.spec.images) {
        formState.value.spec.images = [];
      }
    }
  },
  {
    immediate: true,
  }
);

const handleSubmit = () => {
  submitForm("love-story-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await loveStoryApi.update(formState.value.metadata.name, formState.value);
    } else {
      await loveStoryApi.create(formState.value);
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
  <VModal ref="modal" :title="isUpdateMode ? '编辑故事' : '新建故事'" :width="720" @close="emit('close')">
    <FormKit
      id="love-story-form"
      type="form"
      name="love-story-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        v-model="formState.spec.title"
        name="title"
        label="故事标题"
        type="text"
        validation="required"
        :validation-messages="{ required: '故事标题不能为空' }"
        placeholder="例如：初见的那一天"
      />
      <FormKit
        v-model="formState.spec.date"
        name="date"
        label="故事时间"
        type="date"
        help="这段故事发生在什么时候"
        placeholder="选择故事发生的时间"
      />
      <FormKit
        v-model="formState.spec.location"
        name="location"
        label="故事地点"
        type="text"
        help="这段故事发生在哪里"
        placeholder="例如：学校操场"
      />
      <FormKit
        v-model="formState.spec.content"
        name="content"
        label="故事内容"
        type="textarea"
        rows="6"
        help="写下这段故事，支持 HTML 语法。"
        placeholder="写下这段故事，支持 HTML 语法"
      />
      <FormKit
        v-model="formState.spec.images"
        name="images"
        label="故事图片"
        type="attachment"
        multiple
        :accepts="['image/*']"
        help="可添加多张图片，从附件库选择或直接输入图片地址"
      />
      <FormKit
        v-model="formState.spec.priority"
        name="priority"
        label="排序"
        type="number"
        help="数值越大越靠前"
        placeholder="数值越大越靠前"
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

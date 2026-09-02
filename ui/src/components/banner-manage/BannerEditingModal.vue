<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import { bannerApi } from "@/api";
import type { Banner } from "@/types";

const props = withDefaults(
  defineProps<{
    item?: Banner;
  }>(),
  { item: undefined }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);

const isUpdateMode = computed(() => !!props.item);

// 纯自定义弹窗：作者信息由服务端默认取当前登录用户，不显示/编辑（决策 D15）
const formState = ref<Banner>({
  metadata: { name: "" },
  spec: {
    title: "",
    cover: "",
    date: "",
    content: "",
    remark: "",
    link: "",
    priority: 0,
  },
});

watch(
  () => props.item,
  (item) => {
    if (item) {
      formState.value = cloneDeep(item);
      if (!formState.value.spec.content) {
        formState.value.spec.content = "";
      }
      if (!formState.value.spec.cover) {
        formState.value.spec.cover = "";
      }
    }
  },
  {
    immediate: true,
  }
);

const handleSubmit = () => {
  submitForm("banner-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await bannerApi.update(formState.value.metadata.name, formState.value);
    } else {
      await bannerApi.create(formState.value);
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
  <VModal
    ref="modal"
    :title="isUpdateMode ? '编辑自定义轮播图' : '新建自定义轮播图'"
    :width="880"
    @close="emit('close')"
  >
    <FormKit
      id="banner-form"
      type="form"
      name="banner-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <!-- 左右布局：左侧基础表单（固定宽），右侧富文本内容（自适应） -->
      <div class=":uno: flex gap-4">
        <div class=":uno: w-80 shrink-0">
          <FormKit
            v-model="formState.spec.title"
            name="title"
            label="标题"
            type="text"
            validation="required"
            :validation-messages="{ required: '标题不能为空' }"
            placeholder="例如：iAlley 博客上线啦"
          />
          <FormKit
            v-model="formState.spec.cover"
            name="cover"
            label="封面图"
            type="attachment"
            validation="required"
            :validation-messages="{ required: '封面图不能为空' }"
          />
          <FormKit
            v-model="formState.spec.date"
            name="date"
            label="日期"
            type="date"
            help="留空则不展示"
          />
          <FormKit
            v-model="formState.spec.remark"
            name="remark"
            label="备注"
            type="textarea"
            rows="2"
            help="仅管理端可见，不对外展示"
          />
          <FormKit
            v-model="formState.spec.link"
            name="link"
            label="外链"
            type="text"
            help="点击弹窗中的跳转链接（可选）"
            placeholder="https://..."
          />
        </div>
        <div class=":uno: flex min-w-0 flex-1 flex-col gap-2">
          <div class=":uno: text-sm font-medium text-gray-700">内容详情</div>
          <div class=":uno: min-h-[320px] flex-1">
            <RichTextEditorField v-model="formState.spec.content" />
          </div>
        </div>
      </div>
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

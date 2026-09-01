<script setup lang="ts">
import { IconAddCircle, Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import NoticeTypeEditingModal from "@/components/notice-manage/NoticeTypeEditingModal.vue";
import { noticeApi, noticeTypeApi } from "@/api";
import { NOTICE_STATUS_OPTIONS, type Notice, type NoticeType } from "@/types";

const props = withDefaults(
  defineProps<{
    item?: Notice;
  }>(),
  {
    item: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);
const typeModalVisible = ref(false);

const queryClient = useQueryClient();

const isUpdateMode = computed(() => !!props.item);

const formState = ref<Notice>({
  metadata: { name: "" },
  spec: {
    title: "",
    content: "",
    summary: "",
    cover: "",
    link: "",
    typeName: "",
    status: "draft",
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

// 公告类型选项（类型数量少，一次拉取足够）
const { data: typesData } = useQuery({
  queryKey: ["uni-halo:notice-types-options"],
  queryFn: async () => {
    const result = await noticeTypeApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

const typeOptions = computed(() => {
  const options =
    typesData.value?.map((type) => ({
      label: type.spec.displayName || type.metadata.name,
      value: type.metadata.name,
    })) || [];
  // 编辑回显时若当前类型不在列表中（如已被删除），补一个占位选项避免空白
  const current = formState.value.spec.typeName;
  if (current && !options.some((option) => option.value === current)) {
    options.unshift({ label: current, value: current });
  }
  return options;
});

const handleSubmit = () => {
  submitForm("notice-form");
};

/** 新增类型保存成功后：刷新类型选项并自动选中 */
const handleTypeSaved = (type: NoticeType) => {
  typeModalVisible.value = false;
  formState.value.spec.typeName = type.metadata.name;
  queryClient.invalidateQueries({ queryKey: ["uni-halo:notice-types-options"] });
};

const handleSave = async () => {
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await noticeApi.update(formState.value.metadata.name, formState.value);
    } else {
      await noticeApi.create(formState.value);
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
    :title="isUpdateMode ? '编辑公告' : '新建公告'"
    :width="1080"
    @close="emit('close')"
  >
    <!-- 左右布局：左侧基础表单（固定宽），右侧摘要 + 富文本正文（自适应） -->
    <FormKit
      id="notice-form"
      type="form"
      name="notice-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <div class=":uno: flex gap-4">
        <div class=":uno: w-80 shrink-0 overflow-y-auto border-r border-gray-100 pr-4">
          <FormKit
            v-model="formState.spec.title"
            name="title"
            label="标题"
            type="text"
            validation="required"
            :validation-messages="{ required: '标题不能为空' }"
            placeholder="例如：国庆假期安排通知"
          />
          <FormKit
            v-model="formState.spec.cover"
            name="cover"
            label="封面图"
            type="attachment"
          />
          <FormKit
            v-model="formState.spec.link"
            name="link"
            label="外链"
            type="text"
            help="关联的跳转地址（可选）"
            placeholder="https://..."
          />
          <FormKit
            v-model="formState.spec.typeName"
            name="typeName"
            label="公告类型"
            type="select"
            :options="typeOptions"
            placeholder="请选择公告类型"
          />
          <!-- 新增公告类型：单独一行显示在类型选择下方 -->
          <div class=":uno: mb-4">
            <VButton
              type="secondary"
              size="sm"
              title="新增公告类型"
              @click="typeModalVisible = true"
            >
              <template #icon>
                <IconAddCircle />
              </template>
              新增公告类型
            </VButton>
          </div>
          <FormKit
            v-model="formState.spec.status"
            name="status"
            label="状态"
            type="select"
            :options="NOTICE_STATUS_OPTIONS"
          />
          <FormKit
            v-model="formState.spec.priority"
            name="priority"
            label="排序"
            type="number"
            help="数值越大越靠前"
          />
        </div>

        <div class=":uno: flex min-w-0 flex-1 flex-col gap-3">
          <FormKit
            v-model="formState.spec.summary"
            name="summary"
            label="摘要"
            type="textarea"
            rows="2"
            help="留空时自动从正文提取"
            placeholder="简要描述公告内容（可选）"
          />
          <div class=":uno: text-sm font-medium text-gray-700">正文</div>
          <!-- 富文本占满剩余空间，高度最小 400px -->
          <div class=":uno: min-h-[400px] flex-1">
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
  <!-- 新增公告类型弹窗：不嵌套在主弹窗内（避免 VModal 嵌套触发 Vue insertBefore
       错误），且放在主弹窗之后（两者 z-index 均为 2000，DOM 靠后者在上，避免被遮盖）；
       保存成功后自动刷新选项并选中 -->
  <NoticeTypeEditingModal
    v-if="typeModalVisible"
    @close="typeModalVisible = false"
    @saved="handleTypeSaved"
  />
</template>

<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import LinkGroupEditingModal from "@/components/link-manage/LinkGroupEditingModal.vue";
import { miniProgramLinksApi, miniProgramLinkGroupsApi } from "@/api";
import type { MiniProgramLink, MiniProgramLinkGroup } from "@/types";

const props = withDefaults(
  defineProps<{
    item?: MiniProgramLink;
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
const groupModalVisible = ref(false);

const queryClient = useQueryClient();

const isUpdateMode = computed(() => !!props.item);

const formState = ref<MiniProgramLink>({
  metadata: { name: "" },
  spec: {
    displayName: "",
    miniProgramCode: "",
    link: "",
    avatar: "",
    authorName: "",
    website: "",
    groupName: "",
    description: "",
    screenshots: [],
    visible: true,
    priority: 0,
  },
});

/** 可见性下拉绑定（select 值为字符串，提交时转 boolean） */
const visibleText = ref("true");

watch(
  () => props.item,
  (item) => {
    if (item) {
      const cloned = cloneDeep(item);
      if (!cloned.spec.screenshots) {
        cloned.spec.screenshots = [];
      }
      if (cloned.spec.visible === undefined) {
        cloned.spec.visible = true;
      }
      formState.value = cloned;
      visibleText.value = cloned.spec.visible ? "true" : "false";
    }
  },
  {
    immediate: true,
  }
);

// 分组选项（分组数量少，一次拉取足够）
const { data: groupsData } = useQuery({
  queryKey: ["uni-halo:mini-program-link-groups-options"],
  queryFn: async () => {
    const result = await miniProgramLinkGroupsApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

const groupOptions = computed(() => {
  const options =
    groupsData.value?.map((group) => ({
      label: group.spec.displayName || group.metadata.name,
      value: group.metadata.name,
    })) || [];
  // 编辑回显时若当前分组不在列表中（如已被删除），补一个占位选项避免空白
  const current = formState.value.spec.groupName;
  if (current && !options.some((option) => option.value === current)) {
    options.unshift({ label: current, value: current });
  }
  return options;
});

/** 新建分组保存成功后：刷新分组选项（下拉）与列表页分组栏 */
const handleGroupSaved = (group: MiniProgramLinkGroup) => {
  groupModalVisible.value = false;
  formState.value.spec.groupName = group.metadata.name;
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-options"] });
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-filter"] });
};

const handleSubmit = () => {
  submitForm("link-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    formState.value.spec.visible = visibleText.value === "true";
    if (isUpdateMode.value) {
      await miniProgramLinksApi.update(formState.value.metadata.name, formState.value);
    } else {
      await miniProgramLinksApi.create(formState.value);
    }
    queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-links"] });
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
    :title="isUpdateMode ? '编辑链接' : '新建链接'"
    :width="760"
    @close="emit('close')"
  >
    <FormKit
      id="link-form"
      type="form"
      name="link-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <!-- 基本信息 -->
      <div class=":uno: md:grid md:grid-cols-4 md:gap-6 mb-6">
        <div class=":uno: md:col-span-1">
          <div class=":uno: sticky top-0">
            <span class=":uno: text-base font-medium text-gray-900">基本信息</span>
          </div>
        </div>
        <div class=":uno: mt-5 md:col-span-3 md:mt-0">
          <FormKit
            v-model="formState.spec.displayName"
            name="displayName"
            label="小程序名称"
            type="text"
            validation="required"
            :validation-messages="{ required: '小程序名称不能为空' }"
            placeholder="例如：某工具箱"
          />
          <FormKit
            v-model="formState.spec.miniProgramCode"
            name="miniProgramCode"
            label="太阳码"
            type="attachment"
            validation="required"
            :validation-messages="{ required: '太阳码不能为空' }"
            help="小程序码图片，从附件库选择或直接输入图片地址"
          />
          <FormKit
            v-model="formState.spec.link"
            name="link"
            label="小程序地址"
            type="text"
            help="跳转链接（可选）"
            placeholder="https://..."
          />
          <FormKit
            v-model="formState.spec.groupName"
            name="groupName"
            label="分组"
            type="select"
            :options="groupOptions"
            placeholder="请选择分组（留空归「未分组」）"
          />
          <!-- 新建分组：独立一行在分组下拉下方 -->
          <div class=":uno: mb-4">
            <VButton size="sm" type="secondary" @click="groupModalVisible = true">
              新建分组
            </VButton>
          </div>
          <FormKit
            v-model="formState.spec.description"
            name="description"
            label="描述"
            type="textarea"
            rows="3"
            placeholder="小程序简介（可选）"
          />
          <FormKit
            v-model="formState.spec.screenshots"
            name="screenshots"
            label="预览图"
            type="attachment"
            multiple
            :accepts="['image/*']"
            help="支持多张预览图，可从附件库选择或直接上传（参考应用管理的截图上传）"
          />
        </div>
      </div>

      <!-- 作者信息（顺序：头像、昵称、网站） -->
      <div class=":uno: md:grid md:grid-cols-4 md:gap-6 pt-6 mb-6">
        <div class=":uno: md:col-span-1">
          <div class=":uno: sticky top-0">
            <span class=":uno: text-base font-medium text-gray-900">作者信息</span>
          </div>
        </div>
        <div class=":uno: mt-5 md:col-span-3 md:mt-0">
          <FormKit
            v-model="formState.spec.avatar"
            name="avatar"
            label="头像"
            type="attachment"
            help="作者头像图片（可选）"
          />
          <FormKit
            v-model="formState.spec.authorName"
            name="authorName"
            label="昵称"
            type="text"
            placeholder="作者昵称（可选）"
          />
          <FormKit
            v-model="formState.spec.website"
            name="website"
            label="网站"
            type="text"
            help="归属作者信息（可选）"
            placeholder="https://..."
          />
        </div>
      </div>

      <!-- 其他 -->
      <div class=":uno: md:grid md:grid-cols-4 md:gap-6 pt-6">
        <div class=":uno: md:col-span-1">
          <div class=":uno: sticky top-0">
            <span class=":uno: text-base font-medium text-gray-900">其他</span>
          </div>
        </div>
        <div class=":uno: mt-5 md:col-span-3 md:mt-0">
          <FormKit
            v-model="visibleText"
            name="visible"
            label="可见性"
            type="select"
            :options="[
              { label: '显示（app 端可见）', value: 'true' },
              { label: '隐藏（仅控制台可见）', value: 'false' },
            ]"
          />
          <FormKit
            v-model="formState.spec.priority"
            name="priority"
            label="排序"
            type="number"
            help="数值越大越靠前"
          />
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
  <!-- 新建分组弹窗：与主弹窗平级（不嵌套，避免 VModal 嵌套触发 Vue insertBefore
       错误）；保存成功后刷新分组选项并自动选中 -->
  <LinkGroupEditingModal
    v-if="groupModalVisible"
    @close="groupModalVisible = false"
    @saved="handleGroupSaved"
  />
</template>

<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { useQuery } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { miniProgramLinkGroupsApi, miniProgramLinkSubmissionsApi } from "@/api";
import type { MiniProgramLinkSubmission } from "@/types";

const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);

const formState = ref<MiniProgramLinkSubmission>({
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
    email: "",
  },
});

// 分组选项（分组数量少，一次拉取足够）
const { data: groupsData } = useQuery({
  queryKey: ["uni-halo:mini-program-link-groups-options"],
  queryFn: async () => {
    const result = await miniProgramLinkGroupsApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

const groupOptions = computed(() => {
  return (
    groupsData.value?.map((group) => ({
      label: group.spec.displayName || group.metadata.name,
      value: group.metadata.name,
    })) || []
  );
});

const handleSubmit = () => {
  submitForm("submission-form");
};

const handleSave = async () => {
  try {
    saving.value = true;
    await miniProgramLinkSubmissionsApi.create(formState.value);
    modal.value?.close();
    emit("saved");
    Toast.success("申请已提交，进入待审核");
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
    title="新增申请"
    :width="680"
    @close="emit('close')"
  >
    <!-- 申请表单：仅设计字段（测试用，提交后进入待审核） -->
    <FormKit
      id="submission-form"
      type="form"
      name="submission-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
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
      <FormKit
        v-model="formState.spec.description"
        name="description"
        label="描述"
        type="textarea"
        rows="3"
        placeholder="小程序简介（可选）"
      />
      <FormKit
        v-model="formState.spec.applyRemark"
        name="applyRemark"
        label="申请说明"
        type="textarea"
        rows="3"
        placeholder="填写申请说明，方便管理员了解申请意图（可选）"
      />
      <FormKit
        v-model="formState.spec.screenshots"
        name="screenshots"
        label="预览图"
        type="attachment"
        multiple
        :accepts="['image/*']"
        help="支持多张预览图，可从附件库选择或直接上传"
      />
      <FormKit
        v-model="formState.spec.avatar"
        name="avatar"
        label="头像"
        type="attachment"
        help="作者头像（可选）"
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
        help="作者网站（可选）"
        placeholder="https://..."
      />
      <FormKit
        v-model="formState.spec.email"
        name="email"
        label="联系邮箱"
        type="text"
        help="非必填；填写后审核结果将通过邮件通知"
        placeholder="example@mail.com"
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

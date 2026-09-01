<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { computed, ref } from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import { miniProgramLinkSubmissionsApi } from "@/api";

const props = defineProps<{
  /** approve=通过 / reject=拒绝 */
  action: "approve" | "reject";
  /** 目标申请 name 列表（单条或批量） */
  names: string[];
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "done"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const reason = ref("");
const saving = ref(false);

const isReject = computed(() => props.action === "reject");
const isBatch = computed(() => props.names.length > 1);

const title = computed(() => (isReject.value ? "审核拒绝" : "审核通过"));

const description = computed(() => {
  const target = isBatch.value ? `选中的 ${props.names.length} 条申请` : "该条申请";
  return isReject.value
    ? `确定拒绝${target}吗？拒绝后不会生成链接，需填写拒绝原因。`
    : `确定通过${target}吗？通过后会自动生成链接并公开显示。`;
});

const handleSubmit = () => {
  submitForm("audit-confirm-form");
};

const handleConfirm = async () => {
  try {
    saving.value = true;
    await Promise.all(
      props.names.map((name) =>
        isReject.value
          ? miniProgramLinkSubmissionsApi.reject(name, reason.value.trim())
          : miniProgramLinkSubmissionsApi.approve(name, reason.value.trim() || undefined)
      )
    );
    Toast.success(isReject.value ? "已拒绝" : "已通过");
    modal.value?.close();
    emit("done");
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
    :title="title"
    :width="480"
    @close="emit('close')"
  >
    <FormKit
      id="audit-confirm-form"
      type="form"
      name="audit-confirm-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleConfirm"
    >
      <div class=":uno: mb-4 text-sm text-gray-600">
        {{ description }}
      </div>
      <FormKit
        v-model="reason"
        name="reason"
        label="审核说明"
        type="textarea"
        :validation="isReject ? 'required' : undefined"
        :validation-messages="{ required: '拒绝原因不能为空' }"
        :placeholder="isReject ? '请填写拒绝原因（必填）' : '审核说明（可选）'"
        rows="3"
      />
    </FormKit>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
          :disabled="saving"
          :type="isReject ? 'danger' : 'primary'"
          :text="title"
          @submit="handleSubmit"
        />
        <VButton @click="modal?.close()">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import SubmissionDetailContent from "@/components/link-manage/SubmissionDetailContent.vue";
import LinkGroupSelectField from "@/components/link-manage/LinkGroupSelectField.vue";
import { miniProgramLinkSubmissionsApi } from "@/api";
import type { MiniProgramLinkSubmission } from "@/types";

const props = defineProps<{
  item: MiniProgramLinkSubmission;
}>();

const emit = defineEmits<{
  (event: "close"): void;
  (event: "done"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const reason = ref("");
const groupName = ref(props.item.spec?.groupName || "");
const saving = ref(false);

const queryClient = useQueryClient();

const isPending = computed(() => (props.item.spec?.status || "PENDING") === "PENDING");

const handleApprove = async () => {
  try {
    saving.value = true;
    await miniProgramLinkSubmissionsApi.approve(
      props.item.metadata.name,
      reason.value.trim() || undefined,
      groupName.value
    );
    Toast.success("已通过，链接已公开");
    finish();
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    saving.value = false;
  }
};

const handleReject = async () => {
  if (!reason.value.trim()) {
    Toast.error("请填写拒绝原因");
    return;
  }
  try {
    saving.value = true;
    await miniProgramLinkSubmissionsApi.reject(
      props.item.metadata.name,
      reason.value.trim(),
      groupName.value
    );
    Toast.success("已拒绝");
    finish();
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    saving.value = false;
  }
};

const finish = () => {
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-submissions"] });
  modal.value?.close();
  emit("done");
};
</script>

<template>
  <VModal
    ref="modal"
    :title="isPending ? '审核申请' : '审核结果'"
    :width="640"
    @close="emit('close')"
  >
    <!-- 申请详情（仅设计字段，纯展示组件避免 VModal 嵌套） -->
    <SubmissionDetailContent :item="item" />

    <!-- 审核操作区：仅待审核状态显示（分组选择 + FormKit textarea 审核说明） -->
    <div v-if="isPending" class=":uno: mt-4 border-t border-gray-100 pt-4">
      <LinkGroupSelectField v-model="groupName" help="选择一个分组" />
      <FormKit
        v-model="reason"
        name="reason"
        label="审核说明"
        type="textarea"
        rows="3"
        :placeholder="'填写审核说明（通过时可选；拒绝时必填）'"
      />
    </div>

    <template v-if="isPending" #footer>
      <VSpace>
        <VButton
          :loading="saving"
          :disabled="saving"
          type="danger"
          @click="handleReject"
        >
          拒绝
        </VButton>
        <VButton
          :loading="saving"
          :disabled="saving"
          type="primary"
          @click="handleApprove"
        >
          通过
        </VButton>
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
    <template v-else #footer>
      <VButton @click="modal?.close()">关闭</VButton>
    </template>
  </VModal>
</template>

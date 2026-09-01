<script setup lang="ts">
import { VModal, VButton } from "@halo-dev/components";
import { ref } from "vue";
import SubmissionDetailContent from "@/components/link-manage/SubmissionDetailContent.vue";
import type { MiniProgramLinkSubmission } from "@/types";

defineProps<{
  item: MiniProgramLinkSubmission;
}>();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
</script>

<template>
  <VModal
    ref="modal"
    title="申请详情"
    :width="640"
    @close="emit('close')"
  >
    <SubmissionDetailContent :item="item" />

    <template #footer>
      <VButton @click="modal?.close()">关闭</VButton>
    </template>
  </VModal>
</template>

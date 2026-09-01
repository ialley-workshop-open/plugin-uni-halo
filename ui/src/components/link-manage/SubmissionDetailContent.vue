<script setup lang="ts">
import { computed, ref } from "vue";
import { useQuery } from "@tanstack/vue-query";
import type { MiniProgramLinkSubmission } from "@/types";
import { SUBMISSION_STATUS_LABELS } from "@/types";
import { miniProgramLinkGroupsApi } from "@/api";
import ImagePreviewModal from "@/components/common/ImagePreviewModal.vue";

const props = defineProps<{
  item: MiniProgramLinkSubmission;
}>();

const previewVisible = ref(false);
const previewUrl = ref("");

const spec = computed(() => props.item.spec || {});

const statusLabel = computed(
  () => SUBMISSION_STATUS_LABELS[props.item.spec?.status || "PENDING"] || "未知"
);

const statusBadgeClass = computed(() => {
  switch (props.item.spec?.status) {
    case "APPROVED":
      return "bg-green-50 text-green-600";
    case "REJECTED":
      return "bg-red-50 text-red-600";
    default:
      return "bg-amber-50 text-amber-600";
  }
});

const reviewed = computed(
  () => (props.item.spec?.status || "PENDING") !== "PENDING"
);

// 分组名映射（展示实际分组名称而非 metadata.name）
const { data: groupsData } = useQuery({
  queryKey: ["uni-halo:mini-program-link-groups-options"],
  queryFn: async () => {
    const result = await miniProgramLinkGroupsApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

const groupLabel = computed(() => {
  const name = spec.value.groupName;
  if (!name) {
    return "未分组";
  }
  return (
    groupsData.value?.find((group) => group.metadata.name === name)?.spec.displayName || name
  );
});

const formatTime = (value?: string | null) => {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`;
};

const preview = (url?: string) => {
  if (!url) {
    return;
  }
  previewUrl.value = url;
  previewVisible.value = true;
};
</script>

<template>
  <div class=":uno: space-y-4">
    <!-- 太阳码 + 基础信息 -->
    <div class=":uno: flex gap-4">
      <img
        v-if="spec.miniProgramCode"
        :src="spec.miniProgramCode"
        alt="太阳码"
        class=":uno: h-20 w-20 cursor-pointer rounded object-cover"
        @click="preview(spec.miniProgramCode)"
      />
      <div class=":uno: min-w-0 flex-1">
        <div class=":uno: flex items-center gap-2">
          <span class=":uno: text-base font-semibold text-gray-900">
            {{ spec.displayName || "未命名" }}
          </span>
          <span
            class=":uno: rounded px-1.5 py-0.5 text-xs"
            :class="statusBadgeClass"
          >
            {{ statusLabel }}
          </span>
        </div>
        <div class=":uno: mt-1 text-sm text-gray-600">
          <span class=":uno: text-gray-500">申请分组：</span>{{ groupLabel }}
        </div>
        <div v-if="spec.description" class=":uno: mt-1 text-sm text-gray-600">
          <span class=":uno: text-gray-500">应用描述：</span>{{ spec.description }}
        </div>
      </div>
    </div>

    <!-- 申请说明 -->
    <div v-if="spec.applyRemark" class=":uno: text-sm text-gray-700">
      <span class=":uno: text-gray-500">申请说明：</span>{{ spec.applyRemark }}
    </div>

    <!-- 详情字段 -->
    <div class=":uno: grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
      <div v-if="spec.link" class=":uno: col-span-2">
        <span class=":uno: text-gray-500">小程序地址：</span>
        <span class=":uno: break-all text-gray-800">{{ spec.link }}</span>
      </div>
      <div v-if="spec.authorName">
        <span class=":uno: text-gray-500">作者昵称：</span>
        <span class=":uno: text-gray-800">{{ spec.authorName }}</span>
      </div>
      <div v-if="spec.website">
        <span class=":uno: text-gray-500">作者网站：</span>
        <span class=":uno: break-all text-gray-800">{{ spec.website }}</span>
      </div>
      <div v-if="spec.email">
        <span class=":uno: text-gray-500">联系邮箱：</span>
        <span class=":uno: text-gray-800">{{ spec.email }}</span>
      </div>
      <div v-if="spec.submittedAt">
        <span class=":uno: text-gray-500">提交时间：</span>
        <span class=":uno: text-gray-800">{{ formatTime(spec.submittedAt) }}</span>
      </div>
    </div>

    <!-- 作者头像 -->
    <div v-if="spec.avatar" class=":uno: flex items-center gap-2 text-sm">
      <span class=":uno: text-gray-500">作者头像：</span>
      <img
        :src="spec.avatar"
        alt="作者头像"
        class=":uno: h-8 w-8 cursor-pointer rounded-full object-cover"
        @click="preview(spec.avatar)"
      />
    </div>

    <!-- 预览图 -->
    <div v-if="spec.screenshots?.length">
      <div class=":uno: mb-1 text-sm text-gray-500">预览图：</div>
      <div class=":uno: flex flex-wrap gap-2">
        <img
          v-for="(image, index) in spec.screenshots"
          :key="index"
          :src="image"
          alt="预览图"
          class=":uno: h-16 w-24 cursor-pointer rounded object-cover"
          @click="preview(image)"
        />
      </div>
    </div>

    <!-- 底部审核信息区：审核状态 / 审核原因 / 审核日期（已审核时显示） -->
    <div
      v-if="reviewed"
      class=":uno: space-y-1 rounded-md border border-gray-100 bg-gray-50 p-3"
    >
      <div class=":uno: text-xs text-gray-500">审核信息</div>
      <div class=":uno: flex items-center gap-2 text-sm">
        <span class=":uno: text-gray-500">审核状态：</span>
        <span class=":uno: rounded px-1.5 py-0.5 text-xs" :class="statusBadgeClass">
          {{ statusLabel }}
        </span>
      </div>
      <div v-if="spec.reason" class=":uno: text-sm">
        <span class=":uno: text-gray-500">审核原因：</span>
        <span class=":uno: text-gray-800">{{ spec.reason }}</span>
      </div>
      <div v-if="spec.reviewedAt" class=":uno: text-sm">
        <span class=":uno: text-gray-500">审核日期：</span>
        <span class=":uno: text-gray-800">{{ formatTime(spec.reviewedAt) }}</span>
      </div>
    </div>
  </div>

  <ImagePreviewModal
    v-model:visible="previewVisible"
    :images="previewUrl ? [previewUrl] : []"
    title="图片预览"
  />
</template>

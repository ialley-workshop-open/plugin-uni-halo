<script setup lang="ts">
import {
  Dialog,
  IconAddCircle,
  IconRefreshLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
  VDropdownItem,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref, watch } from "vue";
import SubmissionAuditModal from "@/components/link-manage/SubmissionAuditModal.vue";
import SubmissionDetailModal from "@/components/link-manage/SubmissionDetailModal.vue";
import SubmissionFormModal from "@/components/link-manage/SubmissionFormModal.vue";
import AuditConfirmModal from "@/components/link-manage/AuditConfirmModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import ImagePreviewModal from "@/components/common/ImagePreviewModal.vue";
import { miniProgramLinkSubmissionsApi } from "@/api";
import { SUBMISSION_STATUS_LABELS, SUBMISSION_STATUS_OPTIONS } from "@/types";
import type { MiniProgramLinkSubmission } from "@/types";

/** 排序选项（后端 sort 参数，均倒序） */
const SORT_OPTIONS = [
  { label: "申请时间", value: "submittedAt" },
  { label: "审核时间", value: "reviewedAt" },
  { label: "状态", value: "status" },
];

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const status = ref<string>();
const sortBy = ref<string>();
const total = ref(0);

const auditModal = ref(false);
const detailModal = ref(false);
const formModal = ref(false);
const selectedSubmission = ref<MiniProgramLinkSubmission>();

// 行内/批量审核确认弹窗
const auditConfirmVisible = ref(false);
const auditAction = ref<"approve" | "reject">("approve");
const auditNames = ref<string[]>([]);

// 太阳码预览
const previewVisible = ref(false);
const previewUrl = ref("");

const checkAll = ref(false);
const selectedNames = ref<string[]>([]);

const { data: submissions, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:mini-program-link-submissions", page, size, status, sortBy, keyword],
  queryFn: async () => {
    const result = await miniProgramLinkSubmissionsApi.list({
      page: page.value,
      size: size.value,
      status: status.value,
      sort: sortBy.value,
      keyword: keyword.value || undefined,
    });
    total.value = result.total;
    return result;
  },
});

watch(
  () => [keyword.value, status.value, sortBy.value],
  () => {
    page.value = 1;
  }
);

watch(
  () => selectedNames.value,
  (newValue) => {
    checkAll.value = newValue.length === (submissions.value?.items.length || 0);
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedNames.value =
      submissions.value?.items.map((submission) => submission.metadata.name) || [];
  } else {
    selectedNames.value = [];
  }
};

const checkSelection = (submission: MiniProgramLinkSubmission) =>
  selectedNames.value.includes(submission.metadata.name);

const isPending = (submission: MiniProgramLinkSubmission) =>
  (submission.spec.status || "PENDING") === "PENDING";

const handleOpenAudit = (submission: MiniProgramLinkSubmission) => {
  selectedSubmission.value = submission;
  auditModal.value = true;
};

const handleViewDetail = (submission: MiniProgramLinkSubmission) => {
  selectedSubmission.value = submission;
  detailModal.value = true;
};

/** 行内快速审核：打开确认弹窗（单条） */
const handleQuickAudit = (action: "approve" | "reject", submission: MiniProgramLinkSubmission) => {
  auditAction.value = action;
  auditNames.value = [submission.metadata.name];
  auditConfirmVisible.value = true;
};

/** 批量审核：打开确认弹窗（选中项） */
const handleBatchAudit = (action: "approve" | "reject") => {
  if (!selectedNames.value.length) {
    return;
  }
  auditAction.value = action;
  auditNames.value = [...selectedNames.value];
  auditConfirmVisible.value = true;
};

const handleDelete = (submission: MiniProgramLinkSubmission) => {
  Dialog.warning({
    title: "确定要删除这条申请吗？",
    description: "删除后不影响已生成的链接，该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await miniProgramLinkSubmissionsApi.delete(submission.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({
          queryKey: ["uni-halo:mini-program-link-submissions"],
        });
      }
    },
  });
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的申请吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await Promise.all(
          selectedNames.value.map((name) => miniProgramLinkSubmissionsApi.delete(name))
        );
        selectedNames.value = [];
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({
          queryKey: ["uni-halo:mini-program-link-submissions"],
        });
      }
    },
  });
};

const onAuditConfirmDone = () => {
  auditConfirmVisible.value = false;
  selectedNames.value = [];
  refetch();
};

const statusColorClass = computed(
  () => (submission: MiniProgramLinkSubmission) => {
    switch (submission.spec.status) {
      case "APPROVED":
        return "text-green-600";
      case "REJECTED":
        return "text-red-500";
      default:
        return "text-amber-500";
    }
  }
);

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
</script>

<template>
  <SubmissionAuditModal
    v-if="auditModal && selectedSubmission"
    :item="selectedSubmission"
    @close="auditModal = false"
    @done="refetch"
  />
  <SubmissionDetailModal
    v-if="detailModal && selectedSubmission"
    :item="selectedSubmission"
    @close="detailModal = false"
  />
  <!-- 新增申请（测试用） -->
  <SubmissionFormModal
    v-if="formModal"
    @close="formModal = false"
    @saved="refetch"
  />
  <!-- 行内/批量审核确认弹窗 -->
  <AuditConfirmModal
    v-if="auditConfirmVisible"
    :action="auditAction"
    :names="auditNames"
    @close="auditConfirmVisible = false"
    @done="onAuditConfirmDone"
  />

  <VPageHeader title="UniHalo-申请审核">
    <template #actions>
      <VSpace>
        <VButton
          v-permission="['plugin:uni-halo:link:manage']"
          type="secondary"
          @click="formModal = true"
        >
          <template #icon>
            <IconAddCircle />
          </template>
          新增申请
        </VButton>
        <VButton type="secondary" @click="refetch">
          <template #icon>
            <IconRefreshLine :class="{ 'animate-spin text-gray-900': isFetching }" />
          </template>
          刷新
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 flex flex-col gap-4 md:m-4">
    <VCard :body-class="[':uno: !p-0']">
      <template #header>
        <div class=":uno: block w-full bg-gray-50 px-4 py-3">
          <div class=":uno: relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center">
            <div class=":uno: hidden items-center sm:flex">
              <input
                v-model="checkAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
              <template v-if="!selectedNames.length">
                <SearchInput v-model="keyword" placeholder="名称/作者/邮箱（回车搜索）" />
              </template>
              <VSpace v-else spacing="sm">
                <VButton size="sm" type="primary" @click="handleBatchAudit('approve')">
                  通过
                </VButton>
                <VButton size="sm" type="danger" @click="handleBatchAudit('reject')">
                  拒绝
                </VButton>
                <VButton size="sm" type="danger" plain @click="handleDeleteInBatch">
                  删除
                </VButton>
              </VSpace>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <FilterDropdown
                v-model="status"
                label="状态"
                :items="SUBMISSION_STATUS_OPTIONS"
                @update:model-value="() => refetch()"
              />
              <FilterDropdown
                v-model="sortBy"
                label="排序"
                :items="SORT_OPTIONS"
                @update:model-value="() => refetch()"
              />
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!submissions?.items.length" appear name="fade">
        <VEmpty message="暂无申请，可引导用户在小程序端提交" title="还没有申请" />
      </Transition>

      <Transition v-else appear name="fade">
        <VEntityContainer>
          <VEntity
            v-for="submission in submissions?.items"
            :key="submission.metadata.name"
            :is-selected="checkSelection(submission)"
          >
            <template #checkbox>
              <input
                v-model="selectedNames"
                :value="submission.metadata.name"
                name="submission-checkbox"
                type="checkbox"
              />
            </template>
            <template #start>
              <VEntityField v-if="submission.spec.miniProgramCode" width="6rem">
                <template #description>
                  <img
                    :src="submission.spec.miniProgramCode"
                    :alt="submission.spec.displayName || '太阳码'"
                    loading="lazy"
                    class=":uno: h-10 w-10 cursor-pointer rounded object-cover hover:opacity-80"
                    @click="() => { previewUrl = submission.spec.miniProgramCode || ''; previewVisible = true; }"
                  />
                </template>
              </VEntityField>
              <VEntityField width="16rem">
                <template #title>
                  <span class=":uno: font-semibold text-gray-900">
                    {{ submission.spec.displayName || submission.metadata.name }}
                  </span>
                </template>
                <template #description>
                  <span class=":uno: text-xs text-gray-500">
                    {{ submission.spec.description || "暂无描述" }}
                  </span>
                </template>
              </VEntityField>
            </template>
            <template #end>
              <VEntityField>
                <template #description>
                  <span
                    class=":uno: truncate text-xs text-gray-500"
                    style="max-width: 14rem"
                  >
                    <template v-if="submission.spec.applyRemark">
                      <span class=":uno: text-gray-400">申请说明：</span>
                      {{ submission.spec.applyRemark }}
                    </template>
                    <span v-else class=":uno: text-gray-400">暂无申请说明</span>
                  </span>
                </template>
              </VEntityField>
              <VEntityField>
                <template #description>
                  <div class=":uno: flex items-center gap-1.5">
                    <img
                      v-if="submission.spec.avatar"
                      :src="submission.spec.avatar"
                      :alt="submission.spec.authorName || '作者头像'"
                      class=":uno: h-5 w-5 flex-shrink-0 rounded-full object-cover"
                    />
                    <span v-else class=":uno: h-5 w-5 flex-shrink-0 rounded-full bg-gray-100" />
                    <span class=":uno: truncate text-xs text-gray-600">
                      {{ submission.spec.authorName || "未填写作者" }}
                    </span>
                  </div>
                </template>
              </VEntityField>
              <VEntityField>
                <template #description>
                  <span class=":uno: text-xs font-medium" :class="statusColorClass(submission)">
                    {{ SUBMISSION_STATUS_LABELS[submission.spec.status || "PENDING"] }}
                  </span>
                </template>
              </VEntityField>
              <VEntityField>
                <template #description>
                  <span class=":uno: text-xs text-gray-500">
                    {{ formatTime(submission.spec.submittedAt || submission.metadata.creationTimestamp) }}
                  </span>
                </template>
              </VEntityField>
              <!-- 行内审核按钮：仅待审核显示（审核 / 通过 / 拒绝） -->
              <VEntityField v-if="isPending(submission)">
                <template #description>
                  <VSpace spacing="sm">
                    <VButton
                      size="sm"
                      type="secondary"
                      @click="handleOpenAudit(submission)"
                    >
                      审核
                    </VButton>
                    <VButton
                      size="sm"
                      type="primary"
                      @click="handleQuickAudit('approve', submission)"
                    >
                      通过
                    </VButton>
                    <VButton
                      size="sm"
                      type="danger"
                      plain
                      @click="handleQuickAudit('reject', submission)"
                    >
                      拒绝
                    </VButton>
                  </VSpace>
                </template>
              </VEntityField>
            </template>
            <template #dropdownItems>
              <VDropdownItem
                v-if="isPending(submission)"
                @click="handleOpenAudit(submission)"
              >
                审核
              </VDropdownItem>
              <VDropdownItem v-else @click="handleViewDetail(submission)">
                查看详情
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleDelete(submission)">
                删除
              </VDropdownItem>
            </template>
          </VEntity>
        </VEntityContainer>
      </Transition>

      <template #footer>
        <VPagination
          v-model:page="page"
          v-model:size="size"
          page-label="页"
          size-label="条 / 页"
          :total-label="`共 ${total} 项数据`"
          :total="total"
          :size-options="[20, 30, 50, 100]"
        />
      </template>
    </VCard>
  </div>

  <ImagePreviewModal
    v-model:visible="previewVisible"
    :images="previewUrl ? [previewUrl] : []"
    title="太阳码预览"
  />
</template>

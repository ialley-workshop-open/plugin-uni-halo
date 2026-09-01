<script setup lang="ts">
import {
  Dialog,
  IconRefreshLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VSpace,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, reactive, ref, watch } from "vue";
import { VueDraggable } from "vue-draggable-plus";
import RiArticleLine from "~icons/ri/article-line";
import RiFolder2Line from "~icons/ri/folder-2-line";
import RiGalleryLine from "~icons/ri/gallery-line";
import RiMessage3Line from "~icons/ri/message-3-line";
import RiLink from "~icons/ri/link";
import RiDragMove2Line from "~icons/ri/drag-move-2-line";
import RiDeleteBinLine from "~icons/ri/delete-bin-6-line";
import RiImageLine from "~icons/ri/image-line";
import RiShieldCheckLine from "~icons/ri/shield-check-line";
import AuditCandidatesModal from "@/components/audit-config/AuditCandidatesModal.vue";
import { auditDataApi } from "@/api";
import {
  type AuditCandidateType,
  type AuditDataRef,
} from "@/types";

const queryClient = useQueryClient();

interface BlockDef {
  type: AuditCandidateType;
  title: string;
  desc: string;
  icon: object;
}

/** 五类内容区块定义（顺序即页面展示顺序） */
const BLOCKS: BlockDef[] = [
  {
    type: "post",
    title: "文章",
    desc: "审核模式下首页 / 文章列表 / 归档仅展示选中的文章（建议不超过 20 篇）· 依赖：Halo 核心",
    icon: RiArticleLine,
  },
  {
    type: "category",
    title: "分类",
    desc: "审核模式下首页分类栏 / 分类页仅展示选中的分类 · 依赖：Halo 核心",
    icon: RiFolder2Line,
  },
  {
    type: "galleryGroup",
    title: "图库分组",
    desc: "审核模式下图库页仅展示所选分组内的照片（未分组照片不展示）· 依赖：plugin-photos",
    icon: RiGalleryLine,
  },
  {
    type: "moment",
    title: "瞬间",
    desc: "审核模式下瞬间页仅展示选中的瞬间 · 依赖：plugin-moments",
    icon: RiMessage3Line,
  },
  {
    type: "linkGroup",
    title: "链接分组",
    desc: "审核模式下友情链接页仅展示所选分组内的链接 · 依赖：plugin-links",
    icon: RiLink,
  },
];

/** 类型 → spec 字段名映射（仅引用列表字段，排除 description） */
const SPEC_FIELD: Record<
  AuditCandidateType,
  "posts" | "categories" | "galleryGroups" | "moments" | "linkGroups"
> = {
  post: "posts",
  category: "categories",
  galleryGroup: "galleryGroups",
  moment: "moments",
  linkGroup: "linkGroups",
};

const modalType = ref<AuditCandidateType | null>(null);
const saving = ref(false);

const { data: detail, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:audit-data"],
  queryFn: () => auditDataApi.get(),
});

// 已选引用（对象快照：name + title/cover/subTitle/extra，VueDraggable 直接绑定实现拖拽排序）
const selectedItems = reactive<Record<AuditCandidateType, AuditDataRef[]>>({
  post: [],
  category: [],
  galleryGroup: [],
  moment: [],
  linkGroup: [],
});

// 各类型「当前有效」的 name 集合（来自服务端 selections 最新详情，缺失即已失效）
const validNames = reactive<Record<AuditCandidateType, Set<string>>>({
  post: new Set(),
  category: new Set(),
  galleryGroup: new Set(),
  moment: new Set(),
  linkGroup: new Set(),
});

const initialSnapshot = ref("");

const buildSpec = () => ({
  posts: selectedItems.post,
  categories: selectedItems.category,
  galleryGroups: selectedItems.galleryGroup,
  moments: selectedItems.moment,
  linkGroups: selectedItems.linkGroup,
});

/** 是否有未保存修改 */
const dirty = computed(() => JSON.stringify(buildSpec()) !== initialSnapshot.value);

watch(
  () => detail.value,
  (val) => {
    if (!val) {
      return;
    }
    const spec = val.config.spec || {};
    (Object.keys(SPEC_FIELD) as AuditCandidateType[]).forEach((type) => {
      const field = SPEC_FIELD[type];
      selectedItems[type] = [...(spec[field] || [])];
      validNames[type] = new Set((val.selections?.[type] || []).map((item) => item.name));
    });
    initialSnapshot.value = JSON.stringify(spec);
  },
  { immediate: true }
);

/** 引用已删除（服务端最新详情缺失）→ 标记失效 */
const isInvalid = (type: AuditCandidateType, item: AuditDataRef) =>
  !validNames[type].has(item.name);

const handleRemove = (type: AuditCandidateType, item: AuditDataRef) => {
  selectedItems[type] = selectedItems[type].filter((ref) => ref.name !== item.name);
  batchSelected[type] = batchSelected[type].filter((name) => name !== item.name);
};

// ===== 批量操作（每类独立勾选，对齐公告列表的批量删除模式） =====

// 每类勾选的引用 name（批量删除）
const batchSelected = reactive<Record<AuditCandidateType, string[]>>({
  post: [],
  category: [],
  galleryGroup: [],
  moment: [],
  linkGroup: [],
});

const isChecked = (type: AuditCandidateType, item: AuditDataRef) =>
  batchSelected[type].includes(item.name);

const toggleCheck = (type: AuditCandidateType, item: AuditDataRef) => {
  const index = batchSelected[type].indexOf(item.name);
  if (index >= 0) {
    batchSelected[type].splice(index, 1);
  } else {
    batchSelected[type].push(item.name);
  }
};

const isAllChecked = (type: AuditCandidateType) =>
  selectedItems[type].length > 0 &&
  selectedItems[type].every((item) => batchSelected[type].includes(item.name));

const toggleCheckAll = (type: AuditCandidateType) => {
  batchSelected[type] = isAllChecked(type)
    ? []
    : selectedItems[type].map((item) => item.name);
};

const handleBatchDelete = (type: AuditCandidateType) => {
  const names = [...batchSelected[type]];
  Dialog.warning({
    title: "确定要删除选中的条目吗？",
    description: "删除后审核模式下不再展示这些数据，可重新选择。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: () => {
      selectedItems[type] = selectedItems[type].filter(
        (item) => !names.includes(item.name)
      );
      batchSelected[type] = [];
    },
  });
};

const handleModalConfirm = (type: AuditCandidateType, selected: AuditDataRef[]) => {
  selectedItems[type] = selected;
  // 本地同步有效集合，避免刚选中的条目被误标「已失效」；保存刷新后以服务端为准
  validNames[type] = new Set(selected.map((item) => item.name));
  modalType.value = null;
};

const handleSave = async () => {
  try {
    saving.value = true;
    await auditDataApi.save({
      metadata: { name: "audit-data-config" },
      spec: buildSpec(),
    });
    Toast.success("保存成功");
    await queryClient.invalidateQueries({ queryKey: ["uni-halo:audit-data"] });
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <AuditCandidatesModal
    v-if="modalType"
    :type="modalType"
    :selected="selectedItems[modalType]"
    @update:visible="modalType = null"
    @confirm="(selected) => handleModalConfirm(modalType!, selected)"
  />

  <VPageHeader title="UniHalo-审核配置">
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="refetch">
          <template #icon>
            <IconRefreshLine :class="{ 'animate-spin text-gray-900': isFetching }" />
          </template>
          刷新
        </VButton>
        <VButton type="primary" :loading="saving" :disabled="!dirty" @click="handleSave">
          保存修改
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 flex flex-col gap-4 p-4 md:m-4 md:mt-0">
    <!-- 说明 -->
    <VCard>
      <div class=":uno: flex items-start gap-3 py-1">
        <RiShieldCheckLine class=":uno: mt-0.5 h-5 w-5 shrink-0 text-primary" />
        <div class=":uno: text-sm leading-6 text-gray-500">
          审核模式开启后，小程序端（微信审核等场景）<b class=":uno: text-gray-700"
            >仅展示以下挑选的数据</b
          >。开关在「设置 → 审核配置」中开启；此处仅维护「展示哪些数据」。
          数据均为站内真实数据的引用，被删除后自动标记<span class=":uno: text-red-500">已失效</span>，保存时剔除。
        </div>
      </div>
    </VCard>

    <VLoading v-if="isLoading" />

    <VCard
      v-for="block in BLOCKS"
      :key="block.type"
      :body-class="[':uno: !p-0']"
    >
      <template #header>
        <div class=":uno: flex w-full items-center gap-3 px-4 py-3">
          <div
            class=":uno: flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-[#0E1731]/[0.06]"
          >
            <component :is="block.icon" class=":uno: h-5 w-5 text-[#0E1731]" />
          </div>
          <div class=":uno: min-w-0 flex-1">
            <div class=":uno: text-sm font-semibold text-gray-800">{{ block.title }}</div>
            <div class=":uno: mt-0.5 truncate text-xs text-gray-400">{{ block.desc }}</div>
          </div>
          <div class=":uno: ml-auto flex shrink-0 items-center gap-3">
            <span
              class=":uno: rounded-full px-2.5 py-0.5 text-xs"
              :class="
                selectedItems[block.type].length
                  ? ':uno: bg-gray-100 text-gray-600'
                  : ':uno: bg-amber-50 text-amber-600'
              "
            >
              已选 {{ selectedItems[block.type].length }} 条
            </span>
            <VButton size="sm" type="secondary" @click="modalType = block.type">
              选择数据
            </VButton>
          </div>
        </div>
      </template>

      <div class=":uno: p-4">
        <!-- 批量操作条（全选 + 删除选中，勾选后出现） -->
        <div
          v-if="selectedItems[block.type].length"
          class=":uno: mb-2 flex items-center gap-3 rounded-md bg-gray-50 px-3 py-2"
        >
          <input
            type="checkbox"
            :checked="isAllChecked(block.type)"
            @change="toggleCheckAll(block.type)"
          />
          <span class=":uno: text-xs text-gray-500">全选</span>
          <VButton
            v-if="batchSelected[block.type].length"
            size="sm"
            type="danger"
            class=":uno: ml-auto"
            @click="handleBatchDelete(block.type)"
          >
            删除选中（{{ batchSelected[block.type].length }}）
          </VButton>
        </div>
        <!-- 已选列表（拖拽排序，顺序即小程序端展示顺序） -->
        <VueDraggable
          v-if="selectedItems[block.type].length"
          v-model="selectedItems[block.type]"
          handle=".audit-drag-handle"
        >
          <div
            v-for="item in selectedItems[block.type]"
            :key="item.name"
            class=":uno: mb-2 flex items-center gap-3 rounded-md border border-gray-100 bg-gray-50/60 px-3 py-2 last:mb-0"
          >
            <input
              type="checkbox"
              :checked="isChecked(block.type, item)"
              class=":uno: shrink-0"
              @change="toggleCheck(block.type, item)"
            />
            <span class=":uno: audit-drag-handle cursor-move text-gray-300 hover:text-gray-500">
              <RiDragMove2Line class=":uno: h-4 w-4" />
            </span>
            <div
              class=":uno: flex h-10 w-10 shrink-0 items-center justify-center overflow-hidden rounded-md bg-gray-100 text-base"
            >
              <img
                v-if="item.cover"
                :src="item.cover"
                class=":uno: h-full w-full object-cover"
                alt=""
              />
              <RiImageLine v-else class=":uno: h-5 w-5 text-gray-300" />
            </div>
            <div class=":uno: min-w-0 flex-1">
              <div class=":uno: flex items-center gap-2">
                <span class=":uno: truncate text-sm text-gray-700">
                  {{ item.title || item.name }}
                </span>
                <span
                  v-if="isInvalid(block.type, item)"
                  class=":uno: shrink-0 rounded bg-red-50 px-1.5 py-0.5 text-xs text-red-500"
                >
                  已失效
                </span>
              </div>
              <div class=":uno: mt-0.5 truncate text-xs text-gray-400">
                {{ [item.subTitle, item.extra].filter(Boolean).join(" · ") }}
              </div>
            </div>
            <button
              class=":uno: rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
              title="移除"
              @click="handleRemove(block.type, item)"
            >
              <RiDeleteBinLine class=":uno: h-4 w-4" />
            </button>
          </div>
        </VueDraggable>
        <VEmpty
          v-else
          title="未配置"
          message="审核模式下该模块显示为空，点击右上角按钮挑选数据"
        />
      </div>
    </VCard>
  </div>
</template>

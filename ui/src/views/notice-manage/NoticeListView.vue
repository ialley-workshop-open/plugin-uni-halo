<script setup lang="ts">
import {
  Dialog,
  IconAddCircle,
  IconGrid,
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
import { VueDraggable } from "vue-draggable-plus";
import { computed, ref, watch } from "vue";
import NoticeEditingModal from "@/components/notice-manage/NoticeEditingModal.vue";
import NoticeTypeEditingModal from "@/components/notice-manage/NoticeTypeEditingModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import ImagePreviewModal from "@/components/common/ImagePreviewModal.vue";
import RiDragMove2Line from "~icons/ri/drag-move-2-line";
import RiEditLine from "~icons/ri/edit-line";
import RiDeleteBinLine from "~icons/ri/delete-bin-6-line";
import { noticeApi, noticeTypeApi } from "@/api";
import {
  NOTICE_SORT_OPTIONS,
  NOTICE_STATUS_LABELS,
  NOTICE_STATUS_OPTIONS,
  type Notice,
  type NoticeStatus,
  type NoticeType,
} from "@/types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const status = ref<NoticeStatus>();
const type = ref<string>();
const sort = ref("date_desc");
const total = ref(0);

const editingModal = ref(false);
const selectedNotice = ref<Notice>();
const typeEditingVisible = ref(false);
const editingType = ref<NoticeType>();

// 公告封面预览
const previewVisible = ref(false);
const previewUrl = ref("");

const checkAll = ref(false);
const selectedNoticeNames = ref<string[]>([]);

const { data: notices, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:notices", page, size, status, type, sort, keyword],
  queryFn: async () => {
    const result = await noticeApi.list({
      page: page.value,
      size: size.value,
      status: status.value,
      type: type.value,
      sort: sort.value,
      keyword: keyword.value || undefined,
    });
    total.value = result.total;
    return result;
  },
});

// 公告类型（供左侧类型栏与列表标签展示使用）
const { data: types } = useQuery({
  queryKey: ["uni-halo:notice-types-filter"],
  queryFn: async () => {
    const result = await noticeTypeApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

// 左侧类型栏数据（VueDraggable 拖拽用可变数组）
const typeList = ref<NoticeType[]>([]);
watch(
  () => types.value,
  (val) => {
    typeList.value = val ? [...val] : [];
  },
  { immediate: true }
);

const toggleType = (value: string) => {
  type.value = type.value === value ? undefined : value;
};

const handleTypeCreate = () => {
  editingType.value = undefined;
  typeEditingVisible.value = true;
};

const handleTypeEdit = (typeItem: NoticeType) => {
  editingType.value = typeItem;
  typeEditingVisible.value = true;
};

const handleTypeSaved = () => {
  typeEditingVisible.value = false;
  editingType.value = undefined;
  queryClient.invalidateQueries({ queryKey: ["uni-halo:notice-types-filter"] });
};

const handleTypeDelete = (typeItem: NoticeType) => {
  Dialog.warning({
    title: "确定要删除这个公告类型吗？",
    description: "删除后已关联的公告将不再显示类型标签，该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await noticeTypeApi.delete(typeItem.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:notice-types-filter"] });
      }
    },
  });
};

/** 拖拽排序结束：按新顺序保存排序 */
const handleTypeDragEnd = async () => {
  const names = typeList.value.map((t) => t.metadata.name);
  try {
    await noticeTypeApi.sortOrder(names);
    queryClient.invalidateQueries({ queryKey: ["uni-halo:notice-types-filter"] });
  } catch (error) {
    Toast.error((error as Error).message);
  }
};

/** 类型名称/颜色映射（列表展示用） */
const typeInfoMap = computed(() => {
  const map: Record<string, { displayName?: string; color?: string }> = {};
  types.value?.forEach((t) => {
    map[t.metadata.name] = {
      displayName: t.spec.displayName,
      color: t.spec.color,
    };
  });
  return map;
});

const typeBadgeStyle = (notice: Notice) => {
  const info = notice.spec.typeName ? typeInfoMap.value[notice.spec.typeName] : undefined;
  return {
    backgroundColor: info && isValidColor(info.color) ? info.color : "#10B981",
  };
};

const typeBadgeText = (notice: Notice) => {
  const info = notice.spec.typeName ? typeInfoMap.value[notice.spec.typeName] : undefined;
  return info?.displayName || notice.spec.typeName || "";
};

watch(
  () => [keyword.value, status.value, type.value, sort.value],
  () => {
    page.value = 1;
  }
);

watch(
  () => selectedNoticeNames.value,
  (newValue) => {
    checkAll.value = newValue.length === (notices.value?.items.length || 0);
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedNoticeNames.value =
      notices.value?.items.map((notice) => notice.metadata.name) || [];
  } else {
    selectedNoticeNames.value = [];
  }
};

const checkSelection = (notice: Notice) => {
  return selectedNoticeNames.value.includes(notice.metadata.name);
};

const handleOpenEditingModal = (notice?: Notice) => {
  selectedNotice.value = notice;
  editingModal.value = true;
};

const handleDelete = (notice: Notice) => {
  Dialog.warning({
    title: "确定要删除这条公告吗？",
    description: "删除后 app 端将不可见，该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await noticeApi.delete(notice.metadata.name);
        Toast.success("删除成功");
        // 删除当前页最后一条后回退页码，避免停留在空页；否则显式刷新列表
        if (page.value > 1 && notices.value?.items.length === 1) {
          page.value -= 1;
        } else {
          refetch();
        }
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:notices"] });
      }
    },
  });
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的公告吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      const names = [...selectedNoticeNames.value];
      try {
        await Promise.all(names.map((name) => noticeApi.delete(name)));
        selectedNoticeNames.value = [];
        Toast.success("删除成功");
        // 删除覆盖当前页全部条目时回退页码，否则显式刷新列表
        if (page.value > 1 && notices.value?.items.length === names.length) {
          page.value -= 1;
        } else {
          refetch();
        }
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:notices"] });
      }
    },
  });
};

const onModalClose = () => {
  editingModal.value = false;
  selectedNotice.value = undefined;
  refetch();
};

/** 状态标签颜色（复用 Halo 语义色） */
const statusColorClass = computed(
  () => (notice: Notice) => {
    switch (notice.spec.status) {
      case "published":
        return "text-green-600";
      case "offline":
        return "text-gray-400";
      default:
        return "text-orange-500";
    }
  }
);

const isValidColor = (color?: string) =>
  /^#[0-9a-fA-F]{3,8}$/.test(color || "");

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
  <NoticeEditingModal
    v-if="editingModal"
    :item="selectedNotice"
    @close="onModalClose"
  />

  <!-- 类型编辑弹窗（新建/编辑，与公告编辑弹窗平级挂载） -->
  <NoticeTypeEditingModal
    v-if="typeEditingVisible"
    :item="editingType"
    @close="typeEditingVisible = false"
    @saved="handleTypeSaved"
  />

  <VPageHeader title="UniHalo-公告管理">
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="refetch">
          <template #icon>
            <IconRefreshLine :class="{ 'animate-spin text-gray-900': isFetching }" />
          </template>
          刷新
        </VButton>
        <VButton
          v-permission="['plugin:uni-halo:notice:manage']"
          type="primary"
          @click="handleOpenEditingModal()"
        >
          <template #icon>
            <IconAddCircle />
          </template>
          新建公告
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 flex flex-col gap-4 md:m-4 lg:flex-row">
    <!-- 左侧公告类型栏：类型筛选 + 类型管理入口（对齐应用管理-版本管理布局） -->
    <div class=":uno: w-full flex-shrink-0 lg:w-64">
      <VCard :body-class="[':uno: !p-0']">
        <div class=":uno: flex items-center justify-between border-b border-gray-100 px-4 py-3">
          <span class=":uno: text-sm font-semibold text-gray-700">公告类型</span>
          <VButton size="sm" type="secondary" @click="handleTypeCreate">
            <template #icon>
              <IconAddCircle />
            </template>
            新建类型
          </VButton>
        </div>
        <div
          class=":uno: flex cursor-pointer items-center gap-2 px-4 py-3 text-sm"
          :class="
            type === undefined
              ? ':uno: bg-gray-50 font-medium text-gray-900'
              : ':uno: text-gray-700 hover:bg-gray-50'
          "
          @click="type = undefined"
        >
          <IconGrid class=":uno: h-4 w-4 text-gray-400" />
          全部公告
        </div>
        <VueDraggable v-model="typeList" handle=".type-drag-handle" @end="handleTypeDragEnd">
          <div
            v-for="t in typeList"
            :key="t.metadata.name"
            class=":uno: group flex cursor-pointer items-center gap-2 px-4 py-3 text-sm"
            :class="
              type === t.metadata.name
                ? ':uno: bg-gray-50'
                : ':uno: hover:bg-gray-50'
            "
          >
            <span class=":uno: type-drag-handle cursor-move text-gray-300 hover:text-gray-500">
              <RiDragMove2Line class=":uno: h-4 w-4" />
            </span>
            <span
              class=":uno: inline-block h-3.5 w-3.5 rounded-full border border-gray-200"
              :style="{
                backgroundColor: isValidColor(t.spec.color) ? t.spec.color : '#cccccc',
              }"
            />
            <span class=":uno: min-w-0 flex-1 truncate" @click="toggleType(t.metadata.name)">
              {{ t.spec.displayName || t.metadata.name }}
            </span>
            <span class=":uno: hidden items-center gap-1 group-hover:flex">
              <button
                class=":uno: rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                title="编辑"
                @click="handleTypeEdit(t)"
              >
                <RiEditLine class=":uno: h-3.5 w-3.5" />
              </button>
              <button
                class=":uno: rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
                title="删除"
                @click="handleTypeDelete(t)"
              >
                <RiDeleteBinLine class=":uno: h-3.5 w-3.5" />
              </button>
            </span>
          </div>
        </VueDraggable>
      </VCard>
    </div>

    <!-- 右侧公告列表 -->
    <div class=":uno: min-w-0 flex-1">
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
              <template v-if="!selectedNoticeNames.length">
                <SearchInput v-model="keyword" placeholder="公告标题/摘要（回车搜索）" />
              </template>
              <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                删除
              </VButton>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <FilterDropdown
                v-model="status"
                label="状态"
                :items="NOTICE_STATUS_OPTIONS"
                @update:model-value="() => refetch()"
              />
              <FilterDropdown
                v-model="sort"
                label="排序"
                :items="NOTICE_SORT_OPTIONS"
                @update:model-value="() => refetch()"
              />
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!notices?.items.length" appear name="fade">
        <VEmpty message="发布公告，让 app 端用户及时了解最新动态" title="还没有公告">
          <template #actions>
            <VButton
              v-permission="['plugin:uni-halo:notice:manage']"
              type="secondary"
              @click="handleOpenEditingModal()"
            >
              <template #icon>
                <IconAddCircle />
              </template>
              新建公告
            </VButton>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <VEntityContainer>
          <VEntity
            v-for="notice in notices?.items"
            :key="notice.metadata.name"
            :is-selected="checkSelection(notice)"
          >
            <template #checkbox>
              <input
                v-model="selectedNoticeNames"
                :value="notice.metadata.name"
                name="notice-checkbox"
                type="checkbox"
              />
            </template>
            <template #start>
              <VEntityField v-if="notice.spec.cover" width="6rem">
                <template #description>
                  <img
                    :src="notice.spec.cover"
                    :alt="notice.spec.title || '封面'"
                    loading="lazy"
                    class=":uno: h-10 w-16 cursor-pointer rounded object-cover hover:opacity-80"
                    @click="() => { previewUrl = notice.spec.cover || ''; previewVisible = true; }"
                  />
                </template>
              </VEntityField>
              <VEntityField :title="notice.spec.title || notice.metadata.name" width="16rem">
                <template #description>
                  <span class=":uno: text-xs text-gray-500">
                    {{ formatTime(notice.spec.publishTime || notice.metadata.creationTimestamp) }}
                  </span>
                </template>
              </VEntityField>
            </template>
            <template #end>
              <VEntityField>
                <template #description>
                  <span
                    class=":uno: truncate text-xs text-gray-500"
                    style="max-width: 20rem"
                  >
                    {{ notice.spec.summary || "暂无摘要" }}
                  </span>
                </template>
              </VEntityField>
              <VEntityField v-if="notice.spec.typeName">
                <template #description>
                  <span
                    class=":uno: rounded px-1.5 py-0.5 text-xs text-white"
                    :style="typeBadgeStyle(notice)"
                  >
                    {{ typeBadgeText(notice) }}
                  </span>
                </template>
              </VEntityField>
              <VEntityField>
                <template #description>
                  <span
                    class=":uno: text-xs font-medium"
                    :class="statusColorClass(notice)"
                  >
                    {{ NOTICE_STATUS_LABELS[notice.spec.status || 'draft'] }}
                  </span>
                </template>
              </VEntityField>
              <VEntityField v-if="(notice.spec.priority || 0) > 0" description="置顶" />
            </template>
            <template #dropdownItems>
              <VDropdownItem @click="handleOpenEditingModal(notice)">
                编辑
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleDelete(notice)">
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
  </div>

  <ImagePreviewModal
    v-model:visible="previewVisible"
    :images="previewUrl ? [previewUrl] : []"
    title="公告封面"
  />
</template>

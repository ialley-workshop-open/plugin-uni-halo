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
  VStatusDot,
  VDropdownItem,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref, watch } from "vue";
import LoveDailyItemEditingModal from "@/components/love-manage/daily/LoveDailyItemEditingModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import { loveDailyApi } from "@/api";
import {
  LOVE_DAILY_TIME_SORT_OPTIONS,
  LOVE_DAILY_VIEW_MODES,
  LOVE_LIST_SORT_OPTIONS,
  LOVE_STATUS_OPTIONS,
  LOVE_STATUS_LABELS,
  type LoveDailyItem,
  type LoveDailyViewMode,
} from "@/types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const filterStatus = ref<string | undefined>(undefined);
const total = ref(0);
/** 展示方式：列表 / 时间轴 */
const viewMode = ref<LoveDailyViewMode>("list");
/** 列表模式排序：最新创建 / 最早创建 */
const listSortValue = ref("created_desc");
/** 时间轴模式排序：按计划时间 / 完成时间 */
const timelineSortValue = ref("plan_desc");

const editingModal = ref(false);
const selectedItem = ref<LoveDailyItem>();

const checkAll = ref(false);
const selectedItemNames = ref<string[]>([]);

const { data: items, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:love-daily", page, size, filterStatus, keyword],
  queryFn: async () => {
    const result = await loveDailyApi.list({
      page: page.value,
      size: size.value,
      status: filterStatus.value || undefined,
      keyword: keyword.value || undefined,
    });
    total.value = result.total;
    return result;
  },
});

watch(
  () => [filterStatus.value, keyword.value],
  () => {
    page.value = 1;
  }
);

// 时间轴排序锚点：按完成时间时用 completeDate，否则用 planDate；无时间回退创建时间
const timelineTime = (item: LoveDailyItem) => {
  const spec = item.spec;
  if (timelineSortValue.value.startsWith("complete_")) {
    return spec.completeDate || item.metadata.creationTimestamp || "";
  }
  return spec.planDate || item.metadata.creationTimestamp || "";
};

// 列表模式按创建时间排序，时间轴模式按时间轴锚点排序（无时间排最后）
const sortedItems = computed(() => {
  const list = [...(items.value?.items || [])];
  if (viewMode.value === "list") {
    const dir = listSortValue.value === "created_asc" ? 1 : -1;
    list.sort((a, b) => {
      const ta = a.metadata.creationTimestamp || "";
      const tb = b.metadata.creationTimestamp || "";
      return ta.localeCompare(tb) * dir;
    });
    return list;
  }
  const dir = timelineSortValue.value.endsWith("_asc") ? 1 : -1;
  list.sort((a, b) => {
    const ta = timelineTime(a);
    const tb = timelineTime(b);
    if (!ta && !tb) {
      return 0;
    }
    if (!ta) {
      return 1;
    }
    if (!tb) {
      return -1;
    }
    return ta.localeCompare(tb) * dir;
  });
  return list;
});

watch(
  () => selectedItemNames.value,
  (newValue) => {
    checkAll.value = newValue.length === items.value?.items.length;
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedItemNames.value = items.value?.items.map((item) => item.metadata.name) || [];
  } else {
    selectedItemNames.value = [];
  }
};

const checkSelection = (item: LoveDailyItem) => {
  return selectedItemNames.value.includes(item.metadata.name);
};

const handleOpenEditingModal = (item?: LoveDailyItem) => {
  selectedItem.value = item;
  editingModal.value = true;
};

const handleDelete = (item: LoveDailyItem) => {
  Dialog.warning({
    title: "确定要删除该清单项吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await loveDailyApi.delete(item.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:love-daily"] });
      }
    },
  });
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的清单项吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await Promise.all(
          selectedItemNames.value.map((name) => loveDailyApi.delete(name))
        );
        selectedItemNames.value = [];
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:love-daily"] });
      }
    },
  });
};

const onModalClose = () => {
  editingModal.value = false;
  selectedItem.value = undefined;
  refetch();
};

const statusDotState = (status?: string) => {
  if (status === "complete") {
    return "success" as const;
  }
  if (status === "doing") {
    return "warning" as const;
  }
  return "default" as const;
};

// 时间轴圆点颜色（wait 灰 / doing 黄 / complete 粉）
const timelineDotClass = (status?: string) => {
  if (status === "complete") {
    return "bg-pink-400 ring-pink-100";
  }
  if (status === "doing") {
    return "bg-yellow-400 ring-yellow-100";
  }
  return "bg-gray-300 ring-gray-100";
};
</script>

<template>
  <LoveDailyItemEditingModal
    v-if="editingModal"
    :item="selectedItem"
    @close="onModalClose"
  />

  <VPageHeader title="UniHalo-恋爱清单">
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="refetch">
          <template #icon>
            <IconRefreshLine :class="{ 'animate-spin text-gray-900': isFetching }" />
          </template>
          刷新
        </VButton>
        <VButton v-permission="['plugin:uni-halo:love:manage']" type="primary" @click="handleOpenEditingModal()">
          <template #icon>
            <IconAddCircle />
          </template>
          新建清单项
        </VButton>
        <VButton
          v-for="mode in LOVE_DAILY_VIEW_MODES"
          :key="mode.value"
          :type="viewMode === mode.value ? 'primary' : 'secondary'"
          @click="viewMode = mode.value"
        >
          {{ mode.label }}
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
              <template v-if="!selectedItemNames.length">
                <SearchInput v-model="keyword" placeholder="标题/内容（回车搜索）" />
              </template>
              <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                删除
              </VButton>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <FilterDropdown
                v-model="filterStatus"
                label="状态"
                :items="[{ label: '全部状态' }, ...LOVE_STATUS_OPTIONS]"
                @update:model-value="() => refetch()"
              />
              <FilterDropdown
                v-if="viewMode === 'list'"
                v-model="listSortValue"
                label="排序"
                :items="LOVE_LIST_SORT_OPTIONS"
                @update:model-value="() => refetch()"
              />
              <FilterDropdown
                v-else
                v-model="timelineSortValue"
                label="排序"
                :items="LOVE_DAILY_TIME_SORT_OPTIONS"
                @update:model-value="() => refetch()"
              />
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <!-- 列表模式 -->
      <template v-else-if="viewMode === 'list'">
        <Transition v-if="!sortedItems.length" appear name="fade">
          <VEmpty message="写下第一件想一起做的事吧" title="清单还是空的">
            <template #actions>
              <VButton v-permission="['plugin:uni-halo:love:manage']" type="secondary" @click="handleOpenEditingModal()">
                <template #icon>
                  <IconAddCircle />
                </template>
                新建清单项
              </VButton>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <VEntityContainer>
            <VEntity
              v-for="item in sortedItems"
              :key="item.metadata.name"
              :is-selected="checkSelection(item)"
            >
              <template #checkbox>
                <input
                  v-model="selectedItemNames"
                  :value="item.metadata.name"
                  name="love-daily-checkbox"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField :title="item.spec.title || item.metadata.name" width="15rem">
                  <template #description>
                    <span v-if="item.spec.content" class=":uno: truncate text-xs text-gray-500">
                      {{ item.spec.content }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField>
                  <template #description>
                    <VStatusDot
                      :state="statusDotState(item.spec.status)"
                      :text="LOVE_STATUS_LABELS[item.spec.status || 'wait'] || item.spec.status"
                    />
                  </template>
                </VEntityField>
                <VEntityField v-if="item.spec.planDate" :description="`计划 ${item.spec.planDate}`" />
                <VEntityField v-if="item.spec.completeDate" :description="`完成于 ${item.spec.completeDate}`" />
                <VEntityField v-if="item.spec.images?.length" :description="`${item.spec.images.length} 张图片`" />
              </template>
              <template #dropdownItems>
                <VDropdownItem @click="handleOpenEditingModal(item)">
                  编辑
                </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(item)">
                  删除
                </VDropdownItem>
              </template>
            </VEntity>
          </VEntityContainer>
        </Transition>
      </template>

      <!-- 时间轴模式 -->
      <div v-else class=":uno: p-4">
        <div v-if="!sortedItems.length" class=":uno: py-10">
          <VEmpty message="写下第一件想一起做的事吧" title="清单还是空的" />
        </div>
        <div v-else class=":uno: relative">
          <div class=":uno: absolute bottom-2 left-0 top-2 w-0.5 bg-pink-200" />
          <div class=":uno: space-y-6 pl-8">
            <div v-for="item in sortedItems" :key="item.metadata.name" class=":uno: relative">
              <div
                class=":uno: absolute -left-9 top-1.5 h-3 w-3 rounded-full ring-4"
                :class="timelineDotClass(item.spec.status)"
              />
              <VCard :body-class="[':uno: !p-4']">
                <div class=":uno: flex items-center justify-between gap-2">
                  <div class=":uno: flex min-w-0 items-center gap-2">
                    <span class=":uno: truncate text-sm font-semibold text-gray-800">
                      {{ item.spec.title || item.metadata.name }}
                    </span>
                    <VStatusDot
                      :state="statusDotState(item.spec.status)"
                      :text="LOVE_STATUS_LABELS[item.spec.status || 'wait'] || item.spec.status"
                    />
                  </div>
                  <VSpace spacing="sm">
                    <VButton size="sm" type="secondary" @click="handleOpenEditingModal(item)">
                      编辑
                    </VButton>
                    <VButton size="sm" type="danger" plain @click="handleDelete(item)">
                      删除
                    </VButton>
                  </VSpace>
                </div>
                <div class=":uno: mt-1 flex items-center gap-3 text-xs text-gray-400">
                  <span v-if="item.spec.planDate">计划 {{ item.spec.planDate }}</span>
                  <span v-if="item.spec.completeDate">完成于 {{ item.spec.completeDate }}</span>
                </div>
                <div
                  v-if="item.spec.content"
                  class=":uno: mt-2 text-sm leading-relaxed text-gray-600"
                >
                  {{ item.spec.content }}
                </div>
                <div
                  v-if="item.spec.completeRemark"
                  class=":uno: mt-2 rounded bg-pink-50 px-3 py-2 text-sm text-pink-700"
                >
                  💬 {{ item.spec.completeRemark }}
                </div>
                <div
                  v-if="item.spec.images?.length"
                  class=":uno: mt-3 grid grid-cols-3 gap-2 sm:grid-cols-4"
                >
                  <img
                    v-for="(url, index) in item.spec.images"
                    :key="index"
                    :src="url"
                    :alt="item.spec.title || ''"
                    loading="lazy"
                    class=":uno: h-20 w-full rounded object-cover"
                  />
                </div>
              </VCard>
            </div>
          </div>
        </div>
      </div>

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
</template>

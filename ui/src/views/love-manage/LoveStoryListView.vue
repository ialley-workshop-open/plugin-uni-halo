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
import LoveStoryEditingModal from "@/components/love-manage/story/LoveStoryEditingModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import ImagePreviewModal from "@/components/common/ImagePreviewModal.vue";
import { loveStoryApi } from "@/api";
import {
  LOVE_STORY_SORT_OPTIONS,
  LOVE_STORY_VIEW_MODES,
  type LoveStory,
  type LoveStoryViewMode,
} from "@/types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const total = ref(0);
/** 展示方式：列表 / 时间轴 */
const viewMode = ref<LoveStoryViewMode>("list");
/** 日期排序：date_desc 最新在前 / date_asc 最早在前 */
const sortValue = ref("date_desc");

const editingModal = ref(false);
const selectedStory = ref<LoveStory>();

// 故事图片大图预览（多图，从点击的那张开始）
const previewVisible = ref(false);
const previewImages = ref<string[]>([]);
const previewIndex = ref(0);

const checkAll = ref(false);
const selectedStoryNames = ref<string[]>([]);

const { data: stories, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:love-stories", page, size, keyword],
  queryFn: async () => {
    const result = await loveStoryApi.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
    });
    total.value = result.total;
    return result;
  },
});

watch(
  () => keyword.value,
  () => {
    page.value = 1;
  }
);

watch(
  () => selectedStoryNames.value,
  (newValue) => {
    checkAll.value = newValue.length === (stories.value?.items.length || 0);
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedStoryNames.value =
      stories.value?.items.map((story) => story.metadata.name) || [];
  } else {
    selectedStoryNames.value = [];
  }
};

const checkSelection = (story: LoveStory) => {
  return selectedStoryNames.value.includes(story.metadata.name);
};

// 按日期排序（列表与时间轴共用；无日期排最后）
const sortedStories = computed(() => {
  const list = [...(stories.value?.items || [])];
  const dir = sortValue.value === "date_asc" ? 1 : -1;
  list.sort((a, b) => {
    const da = a.spec.date || "";
    const db = b.spec.date || "";
    if (!da && !db) {
      return 0;
    }
    if (!da) {
      return 1;
    }
    if (!db) {
      return -1;
    }
    return da.localeCompare(db) * dir;
  });
  return list;
});

const handleOpenEditingModal = (story?: LoveStory) => {
  selectedStory.value = story;
  editingModal.value = true;
};

const handleDelete = (story: LoveStory) => {
  Dialog.warning({
    title: "确定要删除这段故事吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await loveStoryApi.delete(story.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:love-stories"] });
      }
    },
  });
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的故事吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await Promise.all(
          selectedStoryNames.value.map((name) => loveStoryApi.delete(name))
        );
        selectedStoryNames.value = [];
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:love-stories"] });
      }
    },
  });
};

const onModalClose = () => {
  editingModal.value = false;
  selectedStory.value = undefined;
  refetch();
};
</script>

<template>
  <LoveStoryEditingModal
    v-if="editingModal"
    :story="selectedStory"
    @close="onModalClose"
  />

  <VPageHeader title="UniHalo-恋爱故事">
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
          新建故事
        </VButton>
        <VButton
          v-for="mode in LOVE_STORY_VIEW_MODES"
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
              <template v-if="!selectedStoryNames.length">
                <SearchInput v-model="keyword" placeholder="故事标题/内容（回车搜索）" />
              </template>
              <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                删除
              </VButton>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <FilterDropdown
                v-model="sortValue"
                label="排序"
                :items="LOVE_STORY_SORT_OPTIONS"
                @update:model-value="() => refetch()"
              />
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <!-- 列表模式 -->
      <template v-else-if="viewMode === 'list'">
        <Transition v-if="!sortedStories.length" appear name="fade">
          <VEmpty message="记录下你们的故事，成为珍贵的回忆" title="还没有故事">
            <template #actions>
              <VButton v-permission="['plugin:uni-halo:love:manage']" type="secondary" @click="handleOpenEditingModal()">
                <template #icon>
                  <IconAddCircle />
                </template>
                新建故事
              </VButton>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <VEntityContainer>
            <VEntity
              v-for="story in sortedStories"
              :key="story.metadata.name"
              :is-selected="checkSelection(story)"
            >
              <template #checkbox>
                <input
                  v-model="selectedStoryNames"
                  :value="story.metadata.name"
                  name="love-story-checkbox"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField :title="story.spec.title || story.metadata.name" width="15rem">
                  <template #description>
                    <span v-if="story.spec.date" class=":uno: text-xs text-gray-500">
                      {{ story.spec.date }}
                    </span>
                    <span v-if="story.spec.location" class=":uno: ml-2 text-xs text-gray-500">
                      📍 {{ story.spec.location }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField>
                  <template #description>
                    <span class=":uno: truncate text-xs text-gray-500">
                      {{ story.spec.content || "暂无内容" }}
                    </span>
                  </template>
                </VEntityField>
                <VEntityField v-if="story.spec.images?.length" :description="`${story.spec.images.length} 张图片`" />
              </template>
              <template #dropdownItems>
                <VDropdownItem @click="handleOpenEditingModal(story)">
                  编辑
                </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(story)">
                  删除
                </VDropdownItem>
              </template>
            </VEntity>
          </VEntityContainer>
        </Transition>
      </template>

      <!-- 时间轴模式 -->
      <div v-else class=":uno: p-4">
        <div v-if="!sortedStories.length" class=":uno: py-10">
          <VEmpty message="记录下你们的故事，成为珍贵的回忆" title="还没有故事" />
        </div>
        <div v-else class=":uno: relative">
          <div class=":uno: absolute bottom-2 left-0 top-2 w-0.5 bg-pink-200" />
          <div class=":uno: space-y-6 pl-8">
            <div v-for="story in sortedStories" :key="story.metadata.name" class=":uno: relative">
              <div class=":uno: absolute -left-9 top-1.5 h-3 w-3 rounded-full bg-pink-400 ring-4 ring-pink-100" />
              <VCard :body-class="[':uno: !p-4']">
                <div class=":uno: flex items-center justify-between gap-2">
                  <div class=":uno: truncate text-sm font-semibold text-gray-800">
                    {{ story.spec.title || story.metadata.name }}
                  </div>
                  <VSpace spacing="sm">
                    <VButton size="sm" type="secondary" @click="handleOpenEditingModal(story)">
                      编辑
                    </VButton>
                    <VButton size="sm" type="danger" plain @click="handleDelete(story)">
                      删除
                    </VButton>
                  </VSpace>
                </div>
                <div class=":uno: mt-1 flex items-center gap-3 text-xs text-gray-400">
                  <span v-if="story.spec.date">{{ story.spec.date }}</span>
                  <span v-if="story.spec.location">📍 {{ story.spec.location }}</span>
                </div>
                <div
                  v-if="story.spec.content"
                  class=":uno: mt-2 text-sm leading-relaxed text-gray-600"
                  v-html="story.spec.content"
                />
                <div
                  v-if="story.spec.images?.length"
                  class=":uno: mt-3 grid grid-cols-3 gap-2 sm:grid-cols-4"
                >
                  <img
                    v-for="(url, index) in story.spec.images"
                    :key="index"
                    :src="url"
                    :alt="story.spec.title || ''"
                    loading="lazy"
                    class=":uno: h-20 w-full cursor-pointer rounded object-cover hover:opacity-80"
                    @click="() => { previewImages = story.spec.images || []; previewIndex = index; previewVisible = true; }"
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

  <ImagePreviewModal
    v-model:visible="previewVisible"
    :images="previewImages"
    :initial-index="previewIndex"
    title="故事图片"
  />
</template>

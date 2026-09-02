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
import { VueDraggable } from "vue-draggable-plus";
import { ref, watch } from "vue";
import BannerEditingModal from "@/components/banner-manage/BannerEditingModal.vue";
import BannerCandidatesModal from "@/components/banner-manage/BannerCandidatesModal.vue";
import ImagePreviewModal from "@/components/common/ImagePreviewModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import RiDragMove2Line from "~icons/ri/drag-move-2-line";
import RiImageLine from "~icons/ri/image-line";
import RiRefreshLine from "~icons/ri/refresh-line";
import { bannerApi } from "@/api";
import {
  BANNER_SOURCE_LABELS,
  BANNER_SOURCE_OPTIONS,
  BANNER_SORT_OPTIONS,
  type Banner,
  type BannerCandidate,
} from "@/types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const source = ref<string>();
const sort = ref("manual");
const total = ref(0);

// 多选（批量删除/同步）
const checkAll = ref(false);
const selectedBannerNames = ref<string[]>([]);

// 新建-从文章 / 更换文章 共用候选弹窗
const candidatesVisible = ref(false);
const candidatesMode = ref<"create" | "change">("create");
const changeTargetName = ref("");

// 自定义编辑弹窗
const editingModal = ref(false);
const selectedBanner = ref<Banner>();

// 封面预览
const previewVisible = ref(false);
const previewUrl = ref("");

const { data: banners, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:banners", page, size, source, sort, keyword],
  queryFn: async () => {
    const result = await bannerApi.list({
      page: page.value,
      size: size.value,
      source: source.value,
      sort: sort.value,
      keyword: keyword.value || undefined,
    });
    total.value = result.total;
    return result;
  },
});

// 拖拽排序用可变数组（VueDraggable 需要；非手动排序时手柄不渲染，列表不可拖）
const bannerList = ref<Banner[]>([]);
watch(
  () => banners.value,
  (val) => {
    bannerList.value = val ? [...val.items] : [];
  },
  { immediate: true }
);

watch(
  () => selectedBannerNames.value,
  (newValue) => {
    checkAll.value = newValue.length === (banners.value?.items.length || 0);
  }
);

watch(
  () => [source.value, sort.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedBannerNames.value =
      banners.value?.items.map((b) => b.metadata.name) || [];
  } else {
    selectedBannerNames.value = [];
  }
};

const checkSelection = (banner: Banner) => {
  return selectedBannerNames.value.includes(banner.metadata.name);
};

// ===== 新建 =====

/** 新建（从文章）：打开多选候选弹窗，确认后批量创建 */
const handleCreateFromPost = () => {
  candidatesMode.value = "create";
  candidatesVisible.value = true;
};

/** 新建（自定义）：打开自定义编辑弹窗 */
const handleCreateCustom = () => {
  selectedBanner.value = undefined;
  editingModal.value = true;
};

const handleCandidateConfirm = async (selected: BannerCandidate[]) => {
  if (!selected.length) {
    return;
  }
  try {
    if (candidatesMode.value === "create") {
      // 批量创建：仅提交 postId，快照由服务端拉取填充
      await Promise.all(
        selected.map((candidate) =>
          bannerApi.create({
            metadata: { name: "" },
            spec: { postId: candidate.name },
          })
        )
      );
      Toast.success(`已添加 ${selected.length} 条`);
    } else {
      // 更换文章：更新 postId（服务端重新快照）
      const candidate = selected[0];
      if (!candidate) {
        return;
      }
      const banner = bannerList.value.find(
        (b) => b.metadata.name === changeTargetName.value
      );
      if (banner) {
        const spec = { ...banner.spec, postId: candidate.name };
        await bannerApi.update(changeTargetName.value, {
          metadata: { name: changeTargetName.value },
          spec,
        });
        Toast.success("已更换文章并同步快照");
      }
    }
    await refetch();
  } catch (error) {
    Toast.error((error as Error).message);
  }
};

// ===== 编辑 / 删除 / 同步（单条） =====

const handleEdit = (banner: Banner) => {
  selectedBanner.value = banner;
  editingModal.value = true;
};

const handleSync = async (banner: Banner) => {
  try {
    await bannerApi.sync(banner.metadata.name);
    Toast.success("快照已同步");
    await refetch();
  } catch (error) {
    Toast.error((error as Error).message);
  }
};

const handleChangePost = (banner: Banner) => {
  changeTargetName.value = banner.metadata.name;
  candidatesMode.value = "change";
  candidatesVisible.value = true;
};

const handleDelete = (banner: Banner) => {
  Dialog.warning({
    title: "确定要删除这张轮播图吗？",
    description: "删除后 app 端首页立即不可见，该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await bannerApi.delete(banner.metadata.name);
        Toast.success("删除成功");
        // 删除当前页最后一条后回退页码，避免停留在空页；否则显式刷新列表
        if (page.value > 1 && banners.value?.items.length === 1) {
          page.value -= 1;
        } else {
          refetch();
        }
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:banners"] });
      }
    },
  });
};

// ===== 批量操作 =====

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的轮播图吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      const names = [...selectedBannerNames.value];
      try {
        await Promise.all(names.map((name) => bannerApi.delete(name)));
        selectedBannerNames.value = [];
        Toast.success("删除成功");
        // 删除覆盖当前页全部条目时回退页码，否则显式刷新列表
        if (page.value > 1 && banners.value?.items.length === names.length) {
          page.value -= 1;
        } else {
          refetch();
        }
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:banners"] });
      }
    },
  });
};

/** 批量同步：仅同步选中的文章来源条目，自定义来源跳过 */
const handleSyncInBatch = async () => {
  const postNames = bannerList.value
    .filter(
      (b) =>
        selectedBannerNames.value.includes(b.metadata.name) &&
        b.spec.source === "post"
    )
    .map((b) => b.metadata.name);
  if (!postNames.length) {
    Toast.warning("请选择文章来源的轮播图");
    return;
  }
  try {
    await Promise.all(postNames.map((name) => bannerApi.sync(name)));
    Toast.success(`已同步 ${postNames.length} 条快照`);
    await refetch();
  } catch (error) {
    Toast.error((error as Error).message);
  }
};

// ===== 拖拽排序 =====

const handleDragEnd = async () => {
  const names = bannerList.value.map((b) => b.metadata.name);
  try {
    await bannerApi.sortOrder(names);
    Toast.success("排序已保存");
    await refetch();
  } catch (error) {
    Toast.error((error as Error).message);
    await refetch();
  }
};

const onModalClose = () => {
  editingModal.value = false;
  selectedBanner.value = undefined;
  refetch();
};

const formatTime = (value?: string | null) => {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

/** 来源标签：文章=蓝 / 自定义=绿 */
const sourceBadgeClass = (source?: string) =>
  source === "post"
    ? ":uno: rounded px-1.5 py-0.5 text-xs text-white bg-blue-500"
    : ":uno: rounded px-1.5 py-0.5 text-xs text-white bg-emerald-500";
</script>

<template>
  <BannerEditingModal
    v-if="editingModal"
    :item="selectedBanner"
    @close="onModalClose"
  />

  <!-- 文章候选弹窗：新建-从文章（多选）/ 更换文章（单选） -->
  <BannerCandidatesModal
    v-if="candidatesVisible"
    :multi-select="candidatesMode === 'create'"
    @update:visible="candidatesVisible = $event"
    @confirm="handleCandidateConfirm"
  />

  <VPageHeader title="UniHalo-轮播管理">
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="refetch">
          <template #icon>
            <IconRefreshLine :class="{ 'animate-spin text-gray-900': isFetching }" />
          </template>
          刷新
        </VButton>
        <VButton
          v-permission="['plugin:uni-halo:banner:manage']"
          type="secondary"
          @click="handleCreateFromPost"
        >
          <template #icon>
            <IconAddCircle />
          </template>
          新建（从文章）
        </VButton>
        <VButton
          v-permission="['plugin:uni-halo:banner:manage']"
          type="primary"
          @click="handleCreateCustom"
        >
          <template #icon>
            <IconAddCircle />
          </template>
          新建（自定义）
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 flex flex-col gap-4 p-4">
    <VCard :body-class="[':uno: !p-0']">
      <template #header>
        <div class=":uno: block w-full bg-gray-50 px-4 py-3">
          <div class=":uno: flex flex-wrap items-center gap-3">
            <div class=":uno: flex items-center">
              <input
                v-model="checkAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class=":uno: flex min-w-0 flex-1 items-center">
              <template v-if="!selectedBannerNames.length">
                <SearchInput v-model="keyword" placeholder="标题/备注（回车搜索）" />
              </template>
              <VSpace v-else spacing="sm">
                <VButton size="sm" type="secondary" @click="handleSyncInBatch">
                  <template #icon>
                    <RiRefreshLine class=":uno: h-4 w-4" />
                  </template>
                  同步快照
                </VButton>
                <VButton size="sm" type="danger" @click="handleDeleteInBatch">
                  删除
                </VButton>
              </VSpace>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <FilterDropdown
                v-model="source"
                label="来源"
                :items="BANNER_SOURCE_OPTIONS"
              />
              <FilterDropdown
                v-model="sort"
                label="排序"
                :items="BANNER_SORT_OPTIONS"
              />
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!banners?.items.length" appear name="fade">
        <VEmpty message="创建轮播图，让 app 端首页展示精彩内容" title="还没有轮播图">
          <template #actions>
            <VSpace>
              <VButton
                v-permission="['plugin:uni-halo:banner:manage']"
                type="secondary"
                @click="handleCreateFromPost"
              >
                <template #icon>
                  <IconAddCircle />
                </template>
                新建（从文章）
              </VButton>
              <VButton
                v-permission="['plugin:uni-halo:banner:manage']"
                type="primary"
                @click="handleCreateCustom"
              >
                <template #icon>
                  <IconAddCircle />
                </template>
                新建（自定义）
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <!-- 非手动排序模式手柄不渲染，handle 无匹配时列表不可拖 -->
        <VueDraggable
          v-model="bannerList"
          handle=".banner-drag-handle"
          @end="handleDragEnd"
        >
          <VEntityContainer>
            <VEntity v-for="banner in bannerList" :key="banner.metadata.name">
              <template #checkbox>
                <input
                  v-model="selectedBannerNames"
                  :value="banner.metadata.name"
                  name="banner-checkbox"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField v-if="sort === 'manual'" width="2rem">
                  <template #description>
                    <span
                      class=":uno: banner-drag-handle cursor-move text-gray-300 hover:text-gray-500"
                      title="拖拽排序"
                    >
                      <RiDragMove2Line class=":uno: h-4 w-4" />
                    </span>
                  </template>
                </VEntityField>
                <VEntityField v-if="banner.spec.cover" width="6rem">
                  <template #description>
                    <img
                      :src="banner.spec.cover"
                      :alt="banner.spec.title || '封面'"
                      loading="lazy"
                      class=":uno: h-10 w-16 cursor-pointer rounded object-cover hover:opacity-80"
                      @click="
                        () => {
                          previewUrl = banner.spec.cover || '';
                          previewVisible = true;
                        }
                      "
                    />
                  </template>
                </VEntityField>
                <VEntityField v-else width="6rem">
                  <template #description>
                    <div class=":uno: flex h-10 w-16 items-center justify-center rounded bg-gray-100">
                      <RiImageLine class=":uno: h-4 w-4 text-gray-300" />
                    </div>
                  </template>
                </VEntityField>
                <VEntityField :title="banner.spec.title || banner.metadata.name" width="16rem">
                  <template #description>
                    <span class=":uno: inline-flex items-center gap-1.5 text-xs text-gray-500">
                      <img
                        v-if="banner.spec.authorAvatar"
                        :src="banner.spec.authorAvatar"
                        class=":uno: h-4 w-4 rounded-full object-cover"
                        alt=""
                      />
                      {{ banner.spec.authorName || "—" }}
                      <template v-if="banner.spec.date">
                        {{ formatTime(banner.spec.date) }}
                      </template>
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField v-if="banner.spec.remark">
                  <template #description>
                    <span class=":uno: truncate text-xs text-gray-400" style="max-width: 12rem">
                      {{ banner.spec.remark }}
                    </span>
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span :class="sourceBadgeClass(banner.spec.source)">
                      {{ BANNER_SOURCE_LABELS[banner.spec.source || "custom"] }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template #dropdownItems>
                <template v-if="banner.spec.source === 'post'">
                  <VDropdownItem @click="handleSync(banner)">
                    同步快照
                  </VDropdownItem>
                  <VDropdownItem @click="handleChangePost(banner)">
                    更换文章
                  </VDropdownItem>
                </template>
                <VDropdownItem v-else @click="handleEdit(banner)">
                  编辑
                </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(banner)">
                  删除
                </VDropdownItem>
              </template>
            </VEntity>
          </VEntityContainer>
        </VueDraggable>
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
    title="轮播图封面"
  />
</template>

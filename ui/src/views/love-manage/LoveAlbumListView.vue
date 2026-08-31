<script setup lang="ts">
import {
  Dialog,
  IconAddCircle,
  IconRefreshLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref, watch } from "vue";
import LoveAlbumEditingModal from "@/components/love-manage/album/LoveAlbumEditingModal.vue";
import PhotoManageModal from "@/components/love-manage/PhotoManageModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import { loveAlbumsApi } from "@/api";
import { LOVE_LIST_SORT_OPTIONS, type LoveAlbum } from "@/types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const total = ref(0);

const editingModal = ref(false);
const photoModal = ref(false);
const selectedAlbum = ref<LoveAlbum>();

const checkAll = ref(false);
const selectedAlbumNames = ref<string[]>([]);

const { data: albums, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:love-albums", page, size, keyword],
  queryFn: async () => {
    const result = await loveAlbumsApi.list({
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
  () => selectedAlbumNames.value,
  (newValue) => {
    checkAll.value = newValue.length === (albums.value?.items.length || 0);
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedAlbumNames.value =
      albums.value?.items.map((album) => album.metadata.name) || [];
  } else {
    selectedAlbumNames.value = [];
  }
};

const checkSelection = (album: LoveAlbum) => {
  return selectedAlbumNames.value.includes(album.metadata.name);
};

const sortValue = ref("created_desc");

// 按创建时间排序（前端排序，无时间戳排最后）
const sortedAlbums = computed(() => {
  const list = [...(albums.value?.items || [])];
  const dir = sortValue.value === "created_asc" ? 1 : -1;
  list.sort((a, b) => {
    const ta = a.metadata.creationTimestamp || "";
    const tb = b.metadata.creationTimestamp || "";
    return ta.localeCompare(tb) * dir;
  });
  return list;
});

const handleOpenEditingModal = (album?: LoveAlbum) => {
  selectedAlbum.value = album;
  editingModal.value = true;
};

const handleOpenPhotoModal = (album: LoveAlbum) => {
  selectedAlbum.value = album;
  photoModal.value = true;
};

const handleDelete = (album: LoveAlbum) => {
  Dialog.warning({
    title: "确定要删除该相册吗？",
    description: "相册内的照片将一并删除（附件文件保留），该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await loveAlbumsApi.delete(album.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:love-albums"] });
      }
    },
  });
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的相册吗？",
    description: "相册内的照片将一并删除（附件文件保留），该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await Promise.all(
          selectedAlbumNames.value.map((name) => loveAlbumsApi.delete(name))
        );
        selectedAlbumNames.value = [];
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:love-albums"] });
      }
    },
  });
};

const onModalClose = () => {
  editingModal.value = false;
  photoModal.value = false;
  selectedAlbum.value = undefined;
  refetch();
};
</script>

<template>
  <LoveAlbumEditingModal
    v-if="editingModal"
    :album="selectedAlbum"
    @close="onModalClose"
  />
  <PhotoManageModal
    v-if="photoModal && selectedAlbum"
    :album="selectedAlbum"
    @close="onModalClose"
  />

  <VPageHeader title="UniHalo-恋爱相册">
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
          新建相册
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
              <template v-if="!selectedAlbumNames.length">
                <SearchInput v-model="keyword" placeholder="相册名称/描述（回车搜索）" />
              </template>
              <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                删除
              </VButton>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <FilterDropdown
                v-model="sortValue"
                label="排序"
                :items="LOVE_LIST_SORT_OPTIONS"
                @update:model-value="() => refetch()"
              />
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />
      <div v-else-if="!albums?.items.length" class=":uno: py-10">
        <VEmpty message="点击右上角新建你的第一个恋爱相册" title="当前没有相册">
          <template #actions>
            <VButton v-permission="['plugin:uni-halo:love:manage']" type="secondary" @click="handleOpenEditingModal()">
              <template #icon>
                <IconAddCircle />
              </template>
              新建相册
            </VButton>
          </template>
        </VEmpty>
      </div>
      <div v-else class=":uno: grid grid-cols-1 gap-4 p-4 sm:grid-cols-2 lg:grid-cols-3">
        <VCard
          v-for="album in sortedAlbums"
          :key="album.metadata.name"
          :body-class="[':uno: !p-0']"
          class=":uno: overflow-hidden"
          :class="checkSelection(album) ? ':uno: border-pink-400 ring-2 ring-pink-200' : ''"
        >
          <div class=":uno: relative aspect-[16/9] w-full overflow-hidden bg-gray-100">
            <input
              v-model="selectedAlbumNames"
              :value="album.metadata.name"
              name="love-album-checkbox"
              type="checkbox"
              class=":uno: absolute left-2 top-2 z-10 h-4 w-4 cursor-pointer rounded border-gray-300"
            />
            <img
              v-if="album.spec.cover"
              :src="album.spec.cover"
              alt=""
              loading="lazy"
              class=":uno: h-full w-full object-cover"
            />
            <div v-else class=":uno: flex h-full w-full items-center justify-center text-gray-300">
              <span class=":uno: text-sm">暂无封面</span>
            </div>
            <span
              v-if="album.spec.passwordEnabled"
              class=":uno: absolute right-2 top-2 rounded bg-gray-900/70 px-1.5 py-0.5 text-xs text-white"
            >
              🔒 已加密
            </span>
          </div>
          <div class=":uno: p-4">
            <div class=":uno: truncate text-sm font-semibold text-gray-800">
              {{ album.spec.displayName || "未命名相册" }}
            </div>
            <div class=":uno: mt-1 line-clamp-1 text-xs text-gray-500">
              {{ album.spec.description || "暂无描述" }}
            </div>
            <div class=":uno: mt-1 text-xs text-gray-400">
              {{ album.status?.photoCount ?? 0 }} 张照片
            </div>
            <VSpace class=":uno: mt-3 flex-wrap">
              <VButton size="sm" type="secondary" @click="handleOpenPhotoModal(album)">
                照片
              </VButton>
              <VButton size="sm" type="secondary" @click="handleOpenEditingModal(album)">
                编辑
              </VButton>
              <VButton size="sm" type="danger" plain @click="handleDelete(album)">
                删除
              </VButton>
            </VSpace>
          </div>
        </VCard>
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

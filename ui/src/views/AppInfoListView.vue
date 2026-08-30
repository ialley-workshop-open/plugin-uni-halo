<script lang="ts" setup>
import {
  Dialog,
  IconAddCircle,
  IconRefreshLine,
  Toast,
  VButton,
  VCard,
  VEmpty,
  VEntityContainer,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { ref, watch } from "vue";
import AppInfoEditingModal from "../components/AppInfoEditingModal.vue";
import AppInfoListItem from "../components/AppInfoListItem.vue";
import { appsApi } from "../api";
import type { AppInfo } from "../types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const total = ref(0);

const editingModal = ref(false);
const selectedApp = ref<AppInfo>();

const checkAll = ref(false);
const selectedAppNames = ref<string[]>([]);

const { data: apps, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:apps", page, size, keyword],
  queryFn: async () => {
    const result = await appsApi.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
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

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedAppNames.value = apps.value?.items.map((app) => app.metadata.name) || [];
  } else {
    selectedAppNames.value = [];
  }
};

const checkSelection = (app: AppInfo) => {
  return selectedAppNames.value.includes(app.metadata.name);
};

watch(
  () => selectedAppNames.value,
  (newValue) => {
    checkAll.value = newValue.length === apps.value?.items.length;
  }
);

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的应用吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await Promise.all(
          selectedAppNames.value.map((name) => appsApi.delete(name))
        );
        selectedAppNames.value = [];
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:apps"] });
      }
    },
  });
};

const handleOpenEditingModal = (app?: AppInfo) => {
  selectedApp.value = app;
  editingModal.value = true;
};

const handleDelete = (app: AppInfo) => {
  Dialog.warning({
    title: "确定要删除该应用吗？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await appsApi.delete(app.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:apps"] });
      }
    },
  });
};

const onEditingModalClose = () => {
  selectedApp.value = undefined;
  editingModal.value = false;
  refetch();
};
</script>

<template>
  <AppInfoEditingModal
    v-if="editingModal"
    :app-info="selectedApp"
    @close="onEditingModalClose"
  />

  <VPageHeader title="应用管理">
    <template #actions>
      <VButton type="primary" @click="editingModal = true">
        <template #icon>
          <IconAddCircle />
        </template>
        新建应用
      </VButton>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 md:m-4">
    <VCard :body-class="[':uno: !p-0']">
      <template #header>
        <div class=":uno: block w-full bg-gray-50 px-4 py-3">
          <div
            class=":uno: relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center"
          >
            <div class=":uno: hidden items-center sm:flex">
              <input
                v-model="checkAll"
                type="checkbox"
                @change="handleCheckAllChange"
              />
            </div>
            <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
              <template v-if="!selectedAppNames.length">
                <input
                  v-model="keyword"
                  class=":uno: w-64 rounded border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-primary"
                  placeholder="搜索 AppID / 应用名称（回车搜索）"
                  @keyup.enter="() => refetch()"
                />
              </template>
              <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                删除
              </VButton>
            </div>
            <VSpace spacing="lg" class=":uno: flex-wrap">
              <div class=":uno: flex flex-row gap-2">
                <div
                  class=":uno: group cursor-pointer rounded p-1 hover:bg-gray-200"
                  @click="() => refetch()"
                >
                  <IconRefreshLine
                    v-tooltip="'刷新'"
                    :class="{ 'animate-spin text-gray-900': isFetching }"
                    class=":uno: h-4 w-4 text-gray-600 group-hover:text-gray-900"
                  />
                </div>
              </div>
            </VSpace>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />
      <Transition v-else-if="!apps?.items.length" appear name="fade">
        <VEmpty message="你可以尝试刷新或者新建应用" title="当前没有应用">
          <template #actions>
            <VSpace>
              <VButton @click="refetch">刷新</VButton>
              <VButton type="secondary" @click="editingModal = true">
                <template #icon>
                  <IconAddCircle />
                </template>
                新建应用
              </VButton>
            </VSpace>
          </template>
        </VEmpty>
      </Transition>
      <Transition v-else appear name="fade">
        <VEntityContainer>
          <AppInfoListItem
            v-for="app in apps?.items"
            :key="app.metadata.name"
            :app="app"
            :is-selected="checkSelection(app)"
            @editing="handleOpenEditingModal"
            @delete="handleDelete"
          >
            <template #checkbox>
              <input
                v-model="selectedAppNames"
                :value="app.metadata.name"
                name="app-checkbox"
                type="checkbox"
              />
            </template>
          </AppInfoListItem>
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
</template>

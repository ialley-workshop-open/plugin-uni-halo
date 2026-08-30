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
import FilterDropdown from "../components/FilterDropdown.vue";
import { appsApi } from "../api";
import type { AppInfo } from "../types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const filterAppType = ref<string>();
const total = ref(0);

const editingModal = ref(false);
const selectedApp = ref<AppInfo>();

const { data: apps, isLoading, refetch } = useQuery({
  queryKey: ["uni-halo:apps", page, size, keyword, filterAppType],
  queryFn: async () => {
    const result = await appsApi.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      appType: filterAppType.value ? Number(filterAppType.value) : undefined,
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
        console.error("Failed to delete app", error);
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
      <VButton type="secondary" @click="refetch">
        <template #icon>
          <IconRefreshLine />
        </template>
        刷新
      </VButton>
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
          <div class=":uno: relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center">
            <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
              <FilterDropdown
                v-model="filterAppType"
                label="类型"
                :items="[
                  { label: '全部类型' },
                  { label: 'uni-app', value: '0' },
                  { label: 'uni-app x', value: '1' },
                ]"
                @update:model-value="() => refetch()"
              />
              <input
                v-model="keyword"
                class=":uno: w-64 rounded border border-gray-300 px-3 py-1.5 text-sm outline-none focus:border-primary"
                placeholder="搜索 AppID / 应用名称"
                @keyup.enter="() => refetch()"
              />
              <VButton size="sm" type="secondary" @click="() => refetch()">搜索</VButton>
            </div>
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
            @editing="handleOpenEditingModal"
            @delete="handleDelete"
          />
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

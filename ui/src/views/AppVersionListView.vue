<script lang="ts" setup>
import {
  Dialog,
  IconAddCircle,
  IconGrid,
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
import { useRoute } from "vue-router";
import AppVersionEditingModal from "../components/AppVersionEditingModal.vue";
import AppVersionListItem from "../components/AppVersionListItem.vue";
import FilterDropdown from "../components/FilterDropdown.vue";
import { appVersionsApi, appsApi } from "../api";
import type { AppInfo, AppVersion } from "../types";

const queryClient = useQueryClient();
const route = useRoute();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
// 支持从应用管理跳转时携带 appid 默认筛选（无则全部）
const filterAppid = ref(typeof route.query.appid === "string" ? route.query.appid : "");
const filterPlatform = ref("");
const filterType = ref("");
const total = ref(0);

const editingModal = ref(false);
const selectedVersion = ref<AppVersion>();
// 发布新版时预选的所属应用
const publishAppid = ref("");

const checkAll = ref(false);
const selectedVersionNames = ref<string[]>([]);

// 所属应用下拉数据
const { data: apps } = useQuery({
  queryKey: ["uni-halo:apps:all"],
  queryFn: () => appsApi.list({ page: 1, size: 100 }),
  staleTime: 60 * 1000,
});

const { data: versions, isLoading, isFetching, refetch } = useQuery({
  queryKey: [
    "uni-halo:app-versions",
    page,
    size,
    filterAppid,
    filterPlatform,
    filterType,
    keyword,
  ],
  queryFn: async () => {
    const result = await appVersionsApi.list({
      page: page.value,
      size: size.value,
      appid: filterAppid.value || undefined,
      platform: filterPlatform.value || undefined,
      type: filterType.value || undefined,
      keyword: keyword.value || undefined,
    });
    total.value = result.total;
    return result;
  },
});

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  if (checked) {
    selectedVersionNames.value =
      versions.value?.items.map((version) => version.metadata.name) || [];
  } else {
    selectedVersionNames.value = [];
  }
};

const checkSelection = (version: AppVersion) => {
  return selectedVersionNames.value.includes(version.metadata.name);
};

watch(
  () => selectedVersionNames.value,
  (newValue) => {
    checkAll.value = newValue.length === versions.value?.items.length;
  }
);

watch(
  () => [filterAppid.value, filterPlatform.value, filterType.value, keyword.value],
  () => {
    page.value = 1;
  }
);

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "确定要删除选中的版本吗？",
    description: "该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await Promise.all(
          selectedVersionNames.value.map((name) => appVersionsApi.delete(name))
        );
        selectedVersionNames.value = [];
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:app-versions"] });
      }
    },
  });
};

const handleOpenEditingModal = (version?: AppVersion) => {
  selectedVersion.value = version;
  editingModal.value = true;
};

const handlePublish = (appid: string) => {
  selectedVersion.value = undefined;
  publishAppid.value = appid;
  editingModal.value = true;
};

const handleToggle = async (version: AppVersion) => {
  const next = JSON.parse(JSON.stringify(version)) as AppVersion;
  next.spec.stablePublish = !next.spec.stablePublish;
  try {
    await appVersionsApi.update(version.metadata.name, next);
    Toast.success(next.spec.stablePublish ? "已上线" : "已下线");
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    queryClient.invalidateQueries({ queryKey: ["uni-halo:app-versions"] });
  }
};

const handleDelete = (version: AppVersion) => {
  Dialog.warning({
    title: "确定要删除该版本吗？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await appVersionsApi.delete(version.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:app-versions"] });
      }
    },
  });
};

const onEditingModalClose = () => {
  selectedVersion.value = undefined;
  publishAppid.value = "";
  editingModal.value = false;
  refetch();
};
</script>

<template>
  <AppVersionEditingModal v-if="editingModal" :app-version="selectedVersion" :initial-appid="publishAppid"
    :apps="(apps?.items as AppInfo[]) || []" @close="onEditingModalClose" />
  <VPageHeader title="UniHalo-版本管理">
    <template #actions>
      <VButton type="secondary" @click="refetch">
        <template #icon>
          <IconRefreshLine />
        </template>
        刷新
      </VButton>
      <VButton v-permission="['plugin:uni-halo:version:manage']" type="primary" @click="editingModal = true">
        <template #icon>
          <IconAddCircle />
        </template>
        发布新版
      </VButton>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 flex flex-col gap-4 md:m-4 lg:flex-row">
    <div class=":uno: w-full flex-shrink-0 lg:w-64">
      <VCard :body-class="[':uno: !p-0']">
        <div class=":uno: border-b border-gray-100 px-4 py-3 text-sm font-semibold text-gray-700">
          应用列表
        </div>
        <div class=":uno: flex cursor-pointer items-center gap-3 px-4 py-3 text-sm"
          :class="filterAppid === '' ? ':uno: bg-gray-50 font-medium text-gray-900' : ':uno: text-gray-700 hover:bg-gray-50'"
          @click="filterAppid = ''">
          <div class=":uno: flex h-8 w-8 flex-shrink-0 items-center justify-center rounded bg-gray-200">
            <IconGrid class=":uno: h-4 w-4 text-gray-500" />
          </div>
          全部应用
        </div>
        <div v-for="app in apps?.items" :key="app.metadata.name"
          class=":uno: flex cursor-pointer items-center gap-3 px-4 py-3"
          :class="filterAppid === (app.spec.appid || '') ? ':uno: bg-gray-50' : ':uno: hover:bg-gray-50'"
          @click="filterAppid = app.spec.appid || ''">
          <img v-if="app.spec.iconUrl" :src="app.spec.iconUrl" class=":uno: h-8 w-8 flex-shrink-0 rounded object-cover"
            alt="" />
          <div v-else class=":uno: h-8 w-8 flex-shrink-0 rounded bg-gray-100" />
          <div class=":uno: min-w-0">
            <div class=":uno: truncate text-sm text-gray-800">
              {{ app.spec.name || app.spec.appid }}
            </div>
            <div v-if="app.spec.appid" class=":uno: truncate text-xs text-gray-500">
              {{ app.spec.appid }}
            </div>
          </div>
        </div>
      </VCard>
    </div>
    <div class=":uno: min-w-0 flex-1">
      <VCard :body-class="[':uno: !p-0']">
        <template #header>
          <div class=":uno: block w-full bg-gray-50 px-4 py-3">
            <div class=":uno: relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center">
              <div class=":uno: hidden items-center sm:flex">
                <input v-model="checkAll" type="checkbox" @change="handleCheckAllChange" />
              </div>
              <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
                <template v-if="!selectedVersionNames.length">
                  <SearchInput v-model="keyword" placeholder="标题/版本号（回车搜索）"/>
                </template>
                <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                  删除
                </VButton>
              </div>
              <VSpace spacing="lg" class=":uno: flex-wrap">
                <FilterDropdown v-model="filterAppid" label="应用" :items="[
                  { label: '全部应用' },
                  ...((apps?.items as AppInfo[]) || []).map((app) => ({
                    label: app.spec.name || app.spec.appid || '',
                    value: app.spec.appid,
                  })),
                ]" @update:model-value="() => refetch()" />
                <FilterDropdown v-model="filterPlatform" label="平台" :items="[
                  { label: '全部平台' },
                  { label: 'Android', value: 'Android' },
                  { label: 'iOS', value: 'iOS' },
                  { label: 'Harmony', value: 'Harmony' },
                ]" @update:model-value="() => refetch()" />
                <FilterDropdown v-model="filterType" label="类型" :items="[
                  { label: '全部类型' },
                  { label: '整包', value: 'native_app' },
                  { label: 'wgt', value: 'wgt' },
                ]" @update:model-value="() => refetch()" />
                <div class=":uno: flex flex-row gap-2">
                  <div class=":uno: group cursor-pointer rounded p-1 hover:bg-gray-200" @click="() => refetch()">
                    <IconRefreshLine v-tooltip="'刷新'" :class="{ 'animate-spin text-gray-900': isFetching }"
                      class=":uno: h-4 w-4 text-gray-600 group-hover:text-gray-900" />
                  </div>
                </div>
              </VSpace>
            </div>
          </div>
        </template>

        <VLoading v-if="isLoading" />
        <Transition v-else-if="!versions?.items.length" appear name="fade">
          <VEmpty message="你可以尝试刷新或者发布新版" title="当前没有版本">
            <template #actions>
              <VSpace>
                <VButton @click="refetch">刷新</VButton>
                <VButton type="secondary" @click="editingModal = true">
                  <template #icon>
                    <IconAddCircle />
                  </template>
                  发布新版
                </VButton>
              </VSpace>
            </template>
          </VEmpty>
        </Transition>
        <Transition v-else appear name="fade">
          <VEntityContainer>
            <AppVersionListItem v-for="version in versions?.items" :key="version.metadata.name" :version="version"
              :is-selected="checkSelection(version)" @publish="handlePublish" @editing="handleOpenEditingModal"
              @toggle="handleToggle" @delete="handleDelete">
              <template #checkbox>
                <input v-model="selectedVersionNames" :value="version.metadata.name" name="version-checkbox"
                  type="checkbox" />
              </template>
            </AppVersionListItem>
          </VEntityContainer>
        </Transition>
        <template #footer>
          <VPagination v-model:page="page" v-model:size="size" page-label="页" size-label="条 / 页"
            :total-label="`共 ${total} 项数据`" :total="total" :size-options="[20, 30, 50, 100]" />
        </template>
      </VCard>
    </div>
  </div>
</template>

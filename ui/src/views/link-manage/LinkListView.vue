<script setup lang="ts">
import {
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
  VStatusDot,
  VDropdownItem,
} from "@halo-dev/components";
import RiEditLine from "~icons/ri/edit-line";
import RiDeleteBinLine from "~icons/ri/delete-bin-line";
import RiFolderLine from "~icons/ri/folder-line";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useDeletionFlow } from "@/composables/useDeletionFlow";
import { deletingRefetchIntervalForList } from "@/utils/query";
import { computed, ref, watch } from "vue";
import LinkEditingModal from "@/components/link-manage/LinkEditingModal.vue";
import LinkGroupEditingModal from "@/components/link-manage/LinkGroupEditingModal.vue";
import FilterDropdown from "@/components/common/FilterDropdown.vue";
import ImagePreviewModal from "@/components/common/ImagePreviewModal.vue";
import { miniProgramLinksApi, miniProgramLinkGroupsApi } from "@/api";
import { LINK_SOURCE_LABELS, LINK_SOURCE_OPTIONS } from "@/types";
import type { MiniProgramLink, MiniProgramLinkGroup } from "@/types";

/** 未分组项的固定 id（groupName 为空/undefined） */
const UNGROUPED_TAB = "__ungrouped__";

/** 时间排序选项 */
const SORT_OPTIONS = [
  { label: "默认排序", value: "default" },
  { label: "最新优先", value: "newest" },
  { label: "最早优先", value: "oldest" },
  { label: "来源", value: "source" },
];

/** 可见性筛选选项（value 为 boolean，与 spec.visible 直接比较；「全部」无 value=清除筛选） */
const VISIBLE_FILTER_OPTIONS = [
  { label: "全部" },
  { label: "显示", value: true },
  { label: "隐藏", value: false },
];

const queryClient = useQueryClient();
const { confirmDelete: confirmDeleteLink } = useDeletionFlow(["uni-halo:mini-program-links"]);
const { confirmDelete: confirmDeleteGroup } = useDeletionFlow([
  "uni-halo:mini-program-link-groups-filter",
]);

const activeGroup = ref<string>();
const keyword = ref("");
const visibleFilter = ref<boolean>();
const sourceFilter = ref<string>();
const sortBy = ref("default");
const page = ref(1);
const size = ref(20);
const checkAll = ref(false);
const selectedNames = ref<string[]>([]);

const editingModal = ref(false);
const selectedLink = ref<MiniProgramLink>();
const groupEditingModalVisible = ref(false);
const editingGroup = ref<MiniProgramLinkGroup>();

// 太阳码预览
const previewVisible = ref(false);
const previewUrl = ref("");

const { data: links, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:mini-program-links"],
  queryFn: async () => {
    const result = await miniProgramLinksApi.list({ page: 1, size: 500 });
    return result.items;
  },
  // 删除中对象存在时每 1s 自动重取，直到对象消失（删除语义统一）
  refetchInterval: (data) => deletingRefetchIntervalForList(data),
});

// 分组（左侧分组栏 + 列表分组名展示）
const { data: groups } = useQuery({
  queryKey: ["uni-halo:mini-program-link-groups-filter"],
  queryFn: async () => {
    const result = await miniProgramLinkGroupsApi.list({ page: 1, size: 100 });
    return result.items;
  },
  // 删除中对象存在时每 1s 自动重取，直到对象消失（删除语义统一）
  refetchInterval: (data) => deletingRefetchIntervalForList(data),
});

/** 分组名映射（列表展示用） */
const groupInfoMap = computed(() => {
  const map: Record<string, string> = {};
  groups.value?.forEach((g) => {
    map[g.metadata.name] = g.spec.displayName || g.metadata.name;
  });
  return map;
});

const groupNameOf = (link: MiniProgramLink) => link.spec.groupName || "";

const groupLabelOf = (link: MiniProgramLink) => {
  const name = groupNameOf(link);
  return name ? groupInfoMap.value[name] || name : "";
};

/** 各分组的链接数量（左侧分组栏展示） */
const groupCountMap = computed(() => {
  const map: Record<string, number> = {};
  let ungrouped = 0;
  links.value?.forEach((link) => {
    const name = groupNameOf(link);
    if (name) {
      map[name] = (map[name] || 0) + 1;
    } else {
      ungrouped += 1;
    }
  });
  map[UNGROUPED_TAB] = ungrouped;
  return map;
});

const filteredLinks = computed(() => {
  const list =
    links.value?.filter((link) => {
      const groupName = groupNameOf(link);
      if (activeGroup.value === UNGROUPED_TAB && groupName !== "") {
        return false;
      }
      if (activeGroup.value && activeGroup.value !== UNGROUPED_TAB
          && groupName !== activeGroup.value) {
        return false;
      }
      if (visibleFilter.value !== undefined
          && link.spec.visible !== visibleFilter.value) {
        return false;
      }
      if (sourceFilter.value
          && (link.spec.source || "manual") !== sourceFilter.value) {
        return false;
      }
      if (keyword.value) {
        const text = [
          link.spec.displayName,
          link.spec.description,
          link.spec.authorName,
          groupName,
        ].filter(Boolean).join(" ").toLowerCase();
        if (!text.includes(keyword.value.toLowerCase())) {
          return false;
        }
      }
      return true;
    }) || [];
  // 时间排序（默认：priority 倒序 + 创建时间倒序，服务端已保证；此处仅处理显式排序）
  if (sortBy.value === "newest") {
    return [...list].sort((a, b) => compareTime(b, a));
  }
  if (sortBy.value === "oldest") {
    return [...list].sort((a, b) => compareTime(a, b));
  }
  if (sortBy.value === "source") {
    // 按来源排序：手动在前、申请在后（D26）
    return [...list].sort((a, b) =>
      (a.spec.source || "manual").localeCompare(b.spec.source || "manual")
    );
  }
  return list;
});

/** 来源展示文案（缺失按手动处理） */
const sourceLabelOf = (link: MiniProgramLink) =>
  LINK_SOURCE_LABELS[link.spec.source || "manual"] || "手动";

/** 当前页数据（前端分页，与其他功能模块的分页统计一致） */
const pagedLinks = computed(() => {
  const from = Math.min((page.value - 1) * size.value, filteredLinks.value.length);
  const to = Math.min(from + size.value, filteredLinks.value.length);
  return filteredLinks.value.slice(from, to);
});

const compareTime = (a: MiniProgramLink, b: MiniProgramLink) => {
  const ta = new Date(a.metadata.creationTimestamp || 0).getTime();
  const tb = new Date(b.metadata.creationTimestamp || 0).getTime();
  return ta - tb;
};

watch(
  () => [activeGroup.value, keyword.value, visibleFilter.value, sourceFilter.value, sortBy.value],
  () => {
    page.value = 1;
    selectedNames.value = [];
    checkAll.value = false;
  }
);

watch(
  () => selectedNames.value,
  (value) => {
    checkAll.value = value.length === filteredLinks.value.length
        && filteredLinks.value.length > 0;
  }
);

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  selectedNames.value = checked
    ? filteredLinks.value.map((link) => link.metadata.name)
    : [];
};

const checkSelection = (link: MiniProgramLink) =>
  selectedNames.value.includes(link.metadata.name);

const handleOpenEditingModal = (link?: MiniProgramLink) => {
  selectedLink.value = link;
  editingModal.value = true;
};

const handleDelete = (link: MiniProgramLink) => {
  confirmDeleteLink({
    title: "确定要删除这个链接吗？",
    description: "删除后 app 端将不可见，该操作不可恢复。",
    names: [link.metadata.name],
    doDelete: (name) => miniProgramLinksApi.delete(name),
  });
};

const handleDeleteInBatch = () => {
  const names = [...selectedNames.value];
  confirmDeleteLink({
    title: "确定要删除选中的链接吗？",
    names,
    doDelete: (name) => miniProgramLinksApi.delete(name),
    onSuccess: () => {
      selectedNames.value = [];
    },
  });
};

const handleToggleVisible = async (link: MiniProgramLink) => {
  try {
    await miniProgramLinksApi.update(link.metadata.name, {
      metadata: link.metadata,
      spec: { ...link.spec, visible: !link.spec.visible },
    });
    queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-links"] });
  } catch (error) {
    Toast.error((error as Error).message);
  }
};

// ===== 分组内联管理（参考公告类型管理，无独立弹窗） =====

const handleOpenGroupCreate = () => {
  editingGroup.value = undefined;
  groupEditingModalVisible.value = true;
};

const handleOpenGroupEdit = (group: MiniProgramLinkGroup) => {
  editingGroup.value = group;
  groupEditingModalVisible.value = true;
};

const handleDeleteGroup = (group: MiniProgramLinkGroup) => {
  confirmDeleteGroup({
    title: "确定要删除这个分组吗？",
    description: "删除后已关联的链接将按未分组展示，该操作不可恢复。",
    names: [group.metadata.name],
    doDelete: (name) => miniProgramLinkGroupsApi.delete(name),
    onSuccess: () => {
      if (activeGroup.value === group.metadata.name) {
        activeGroup.value = undefined;
      }
      // 分组删除后刷新分组列表与编辑弹窗选项（链接列表不受影响）
      queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-options"] });
    },
  });
};

const handleGroupSaved = () => {
  groupEditingModalVisible.value = false;
  editingGroup.value = undefined;
  refreshGroups();
};

/** 分组保存/删除成功后刷新分组列表与编辑弹窗的选项 */
const refreshGroups = () => {
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-filter"] });
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-options"] });
};

const onModalClose = () => {
  editingModal.value = false;
  selectedLink.value = undefined;
  refetch();
};
</script>

<template>
  <LinkEditingModal
    v-if="editingModal"
    :item="selectedLink"
    @close="onModalClose"
  />

  <!-- 分组编辑弹窗（新建/编辑共用，与编辑链接弹窗平级挂载） -->
  <LinkGroupEditingModal
    v-if="groupEditingModalVisible"
    :item="editingGroup"
    @close="groupEditingModalVisible = false"
    @saved="handleGroupSaved"
  />

  <VPageHeader title="UniHalo-链接管理">
    <template #actions>
      <VSpace>
        <VButton type="secondary" @click="refetch">
          <template #icon>
            <IconRefreshLine :class="{ 'animate-spin text-gray-900': isFetching }" />
          </template>
          刷新
        </VButton>
        <VButton
          v-permission="['plugin:uni-halo:link:manage']"
          type="primary"
          @click="handleOpenEditingModal()"
        >
          <template #icon>
            <IconAddCircle />
          </template>
          新建链接
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <!-- 左侧分组栏（内联管理）+ 右侧链接列表（参考应用管理-版本管理布局） -->
  <div class=":uno: m-0 flex flex-col gap-4 md:m-4 lg:flex-row">
    <div class=":uno: w-full flex-shrink-0 lg:w-64">
      <VCard :body-class="[':uno: !p-0']">
        <div class=":uno: flex items-center justify-between border-b border-gray-100 px-4 py-3">
          <span class=":uno: text-sm font-semibold text-gray-700">分组列表</span>
          <VButton size="sm" type="secondary" @click="handleOpenGroupCreate">
            新建分组
          </VButton>
        </div>
        <!-- 全部 -->
        <div
          class=":uno: flex cursor-pointer items-center justify-between gap-3 px-4 py-3 text-sm"
          :class="activeGroup === undefined
            ? ':uno: bg-gray-50 font-bold text-gray-900'
            : ':uno: text-gray-700 hover:bg-gray-50'"
          @click="activeGroup = undefined"
        >
          <div class=":uno: flex items-center gap-3">
            <div class=":uno: flex h-8 w-8 flex-shrink-0 items-center justify-center rounded bg-gray-200">
              <IconGrid class=":uno: h-4 w-4 text-gray-500" />
            </div>
            全部
          </div>
          <span class=":uno: text-xs text-gray-400">{{ links?.length || 0 }}</span>
        </div>
        <!-- 未分组 -->
        <div
          class=":uno: flex cursor-pointer items-center justify-between gap-3 px-4 py-3 text-sm"
          :class="activeGroup === UNGROUPED_TAB
            ? ':uno: bg-gray-50 font-bold text-gray-900'
            : ':uno: text-gray-700 hover:bg-gray-50'"
          @click="activeGroup = UNGROUPED_TAB"
        >
          <div class=":uno: flex items-center gap-3">
            <div class=":uno: flex h-8 w-8 flex-shrink-0 items-center justify-center rounded bg-gray-100">
              <IconGrid class=":uno: h-4 w-4 text-gray-400" />
            </div>
            未分组
          </div>
          <span class=":uno: text-xs text-gray-400">{{ groupCountMap[UNGROUPED_TAB] || 0 }}</span>
        </div>
        <!-- 各分组：hover 显示编辑/删除（内联管理） -->
        <div
          v-for="group in groups || []"
          :key="group.metadata.name"
          class=":uno: group flex cursor-pointer items-center justify-between gap-3 px-4 py-3 text-sm"
          :class="activeGroup === group.metadata.name
            ? ':uno: bg-gray-50 font-bold text-gray-900'
            : ':uno: text-gray-700 hover:bg-gray-50'"
          @click="activeGroup = group.metadata.name"
        >
          <div class=":uno: flex min-w-0 items-center gap-3">
            <div class=":uno: flex h-8 w-8 flex-shrink-0 items-center justify-center rounded bg-gray-100">
              <RiFolderLine class=":uno: h-4 w-4 text-gray-400" />
            </div>
            <span class=":uno: truncate">
              {{ group.spec.displayName || group.metadata.name }}
            </span>
          </div>
          <div class=":uno: flex items-center gap-1">
            <span class=":uno: text-xs text-gray-400">
              {{ groupCountMap[group.metadata.name] || 0 }}
            </span>
            <div class=":uno: hidden items-center gap-1 group-hover:flex" @click.stop>
              <button
                class=":uno: rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                title="编辑分组"
                @click="handleOpenGroupEdit(group)"
              >
                <RiEditLine class=":uno: h-3.5 w-3.5" />
              </button>
              <button
                class=":uno: rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
                title="删除分组"
                @click="handleDeleteGroup(group)"
              >
                <RiDeleteBinLine class=":uno: h-3.5 w-3.5" />
              </button>
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
                <input
                  v-model="checkAll"
                  type="checkbox"
                  @change="handleCheckAllChange"
                />
              </div>
              <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
                <template v-if="!selectedNames.length">
                  <SearchInput v-model="keyword" placeholder="名称/描述/作者/分组（回车搜索）" />
                </template>
                <VButton v-else size="sm" type="danger" @click="handleDeleteInBatch">
                  删除
                </VButton>
              </div>
              <VSpace spacing="lg" class=":uno: flex-wrap">
                <FilterDropdown
                  v-model="sortBy"
                  label="排序"
                  :items="SORT_OPTIONS"
                />
                <FilterDropdown
                  v-model="sourceFilter"
                  label="来源"
                  :items="LINK_SOURCE_OPTIONS"
                />
                <FilterDropdown
                  v-model="visibleFilter"
                  label="可见性"
                  :items="VISIBLE_FILTER_OPTIONS"
                />
              </VSpace>
            </div>
          </div>
        </template>

        <VLoading v-if="isLoading" />

        <Transition v-else-if="!filteredLinks.length" appear name="fade">
          <VEmpty message="添加小程序链接，让 app 端用户可以扫码直达" title="还没有链接">
            <template #actions>
              <VButton
                v-permission="['plugin:uni-halo:link:manage']"
                type="secondary"
                @click="handleOpenEditingModal()"
              >
                <template #icon>
                  <IconAddCircle />
                </template>
                新建链接
              </VButton>
            </template>
          </VEmpty>
        </Transition>

        <Transition v-else appear name="fade">
          <VEntityContainer>
            <VEntity
              v-for="link in pagedLinks"
              :key="link.metadata.name"
              :is-selected="checkSelection(link)"
            >
              <template #checkbox>
                <input
                  v-model="selectedNames"
                  :value="link.metadata.name"
                  name="link-checkbox"
                  type="checkbox"
                />
              </template>
              <template #start>
                <VEntityField v-if="link.spec.miniProgramCode" width="6rem">
                  <template #description>
                    <img
                      :src="link.spec.miniProgramCode"
                      :alt="link.spec.displayName || '太阳码'"
                      loading="lazy"
                      class=":uno: h-10 w-10 cursor-pointer rounded object-cover hover:opacity-80"
                      @click="() => { previewUrl = link.spec.miniProgramCode || ''; previewVisible = true; }"
                    />
                  </template>
                </VEntityField>
                <VEntityField :title="link.spec.displayName || link.metadata.name" width="14rem">
                  <template #description>
                    <span class=":uno: text-xs text-gray-500">
                      {{ link.spec.authorName || "未填写作者" }}
                    </span>
                  </template>
                </VEntityField>
              </template>
              <template #end>
                <VEntityField v-if="link.metadata.deletionTimestamp">
                  <template #description>
                    <VStatusDot v-tooltip="'删除中'" state="warning" text="删除中" />
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class=":uno: truncate text-xs text-gray-500" style="max-width: 16rem">
                      {{ link.spec.description || "暂无描述" }}
                    </span>
                  </template>
                </VEntityField>
                <VEntityField v-if="groupNameOf(link)">
                  <template #description>
                    <span class=":uno: rounded bg-gray-100 px-1.5 py-0.5 text-xs text-gray-700">
                      {{ groupLabelOf(link) }}
                    </span>
                  </template>
                </VEntityField>
                <VEntityField>
                  <template #description>
                    <span class=":uno: rounded bg-gray-100 px-1.5 py-0.5 text-xs text-gray-700">
                      来源：{{ sourceLabelOf(link) }}
                    </span>
                  </template>
                </VEntityField>
                <VEntityField v-if="(link.spec.priority || 0) > 0" description="置顶" />
                <VEntityField>
                  <template #description>
                    <button
                      class=":uno: text-xs font-medium"
                      :class="link.spec.visible === false ? 'text-gray-400' : 'text-green-600'"
                      @click="handleToggleVisible(link)"
                    >
                      {{ link.spec.visible === false ? "已隐藏" : "已显示" }}
                    </button>
                  </template>
                </VEntityField>
              </template>
              <template #dropdownItems>
                <VDropdownItem @click="handleOpenEditingModal(link)">
                  编辑
                </VDropdownItem>
                <VDropdownItem type="danger" @click="handleDelete(link)">
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
            :total-label="`共 ${filteredLinks.length} 项数据`"
            :total="filteredLinks.length"
            :size-options="[20, 30, 50, 100]"
          />
        </template>
      </VCard>
    </div>
  </div>

  <ImagePreviewModal
    v-model:visible="previewVisible"
    :images="previewUrl ? [previewUrl] : []"
    title="太阳码预览"
  />
</template>

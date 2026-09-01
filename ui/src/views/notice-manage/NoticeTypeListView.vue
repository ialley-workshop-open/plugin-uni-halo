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
import { ref, watch } from "vue";
import NoticeTypeEditingModal from "@/components/notice-manage/NoticeTypeEditingModal.vue";
import { noticeTypeApi } from "@/api";
import type { NoticeType } from "@/types";

const queryClient = useQueryClient();

const page = ref(1);
const size = ref(20);
const keyword = ref("");
const total = ref(0);

const editingModal = ref(false);
const selectedType = ref<NoticeType>();

const { data: types, isLoading, isFetching, refetch } = useQuery({
  queryKey: ["uni-halo:notice-types", page, size, keyword],
  queryFn: async () => {
    const result = await noticeTypeApi.list({
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

const handleOpenEditingModal = (type?: NoticeType) => {
  selectedType.value = type;
  editingModal.value = true;
};

const handleDelete = (type: NoticeType) => {
  Dialog.warning({
    title: "确定要删除这个公告类型吗？",
    description: "删除后已关联的公告将不再显示类型标签，该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await noticeTypeApi.delete(type.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["uni-halo:notice-types"] });
      }
    },
  });
};

const onModalClose = () => {
  editingModal.value = false;
  selectedType.value = undefined;
  refetch();
};

const isValidColor = (color?: string) =>
  /^#[0-9a-fA-F]{3,8}$/.test(color || "");
</script>

<template>
  <NoticeTypeEditingModal
    v-if="editingModal"
    :type="selectedType"
    @close="onModalClose"
  />

  <VPageHeader title="UniHalo-公告类型">
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
          新建类型
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>

  <div class=":uno: m-0 flex flex-col gap-4 md:m-4">
    <VCard :body-class="[':uno: !p-0']">
      <template #header>
        <div class=":uno: block w-full bg-gray-50 px-4 py-3">
          <div class=":uno: relative flex flex-col flex-wrap items-start gap-4 sm:flex-row sm:items-center">
            <div class=":uno: flex w-full flex-1 items-center sm:w-auto">
              <SearchInput v-model="keyword" placeholder="类型名称（回车搜索）" />
            </div>
          </div>
        </div>
      </template>

      <VLoading v-if="isLoading" />

      <Transition v-else-if="!types?.items.length" appear name="fade">
        <VEmpty message="为公告添加类型标签，便于 app 端分类展示" title="还没有公告类型">
          <template #actions>
            <VButton
              v-permission="['plugin:uni-halo:notice:manage']"
              type="secondary"
              @click="handleOpenEditingModal()"
            >
              <template #icon>
                <IconAddCircle />
              </template>
              新建类型
            </VButton>
          </template>
        </VEmpty>
      </Transition>

      <Transition v-else appear name="fade">
        <VEntityContainer>
          <VEntity
            v-for="type in types?.items"
            :key="type.metadata.name"
          >
            <template #start>
              <VEntityField width="4rem">
                <template #description>
                  <span
                    class=":uno: inline-block h-5 w-5 rounded-full border border-gray-200 align-middle"
                    :style="{
                      backgroundColor: isValidColor(type.spec.color)
                        ? type.spec.color
                        : '#cccccc',
                    }"
                  />
                </template>
              </VEntityField>
              <VEntityField :title="type.spec.displayName || type.metadata.name" width="12rem" />
            </template>
            <template #end>
              <VEntityField v-if="(type.spec.priority || 0) > 0" description="优先展示" />
            </template>
            <template #dropdownItems>
              <VDropdownItem @click="handleOpenEditingModal(type)">
                编辑
              </VDropdownItem>
              <VDropdownItem type="danger" @click="handleDelete(type)">
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
</template>

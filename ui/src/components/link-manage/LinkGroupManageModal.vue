<script setup lang="ts">
import { Dialog, Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { ref } from "vue";
import LinkGroupEditingModal from "@/components/link-manage/LinkGroupEditingModal.vue";
import { miniProgramLinkGroupsApi } from "@/api";
import type { MiniProgramLinkGroup } from "@/types";

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const editingModalVisible = ref(false);
const editingGroup = ref<MiniProgramLinkGroup>();

const queryClient = useQueryClient();

const { data: groups } = useQuery({
  queryKey: ["uni-halo:mini-program-link-groups-manage"],
  queryFn: async () => {
    const result = await miniProgramLinkGroupsApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

const handleOpenCreate = () => {
  editingGroup.value = undefined;
  editingModalVisible.value = true;
};

const handleOpenEdit = (group: MiniProgramLinkGroup) => {
  editingGroup.value = group;
  editingModalVisible.value = true;
};

const handleDelete = (group: MiniProgramLinkGroup) => {
  Dialog.warning({
    title: "确定要删除这个分组吗？",
    description: "删除后已关联的链接将按未分组展示，该操作不可恢复。",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await miniProgramLinkGroupsApi.delete(group.metadata.name);
        Toast.success("删除成功");
      } catch (error) {
        Toast.error((error as Error).message);
      } finally {
        refreshGroups();
      }
    },
  });
};

/** 保存/删除成功后刷新管理弹窗列表与列表页分组标签条 */
const refreshGroups = () => {
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-manage"] });
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-filter"] });
};

const handleSaved = () => {
  editingModalVisible.value = false;
  editingGroup.value = undefined;
  refreshGroups();
};
</script>

<template>
  <VModal
    ref="modal"
    title="分组管理"
    :width="560"
    @close="emit('close')"
  >
    <div class=":uno: mb-3 flex justify-end">
      <VButton size="sm" type="secondary" @click="handleOpenCreate">
        新建分组
      </VButton>
    </div>

    <div
      v-if="!groups?.length"
      class=":uno: py-10 text-center text-sm text-gray-400"
    >
      还没有分组，点击右上角「新建分组」
    </div>
    <div v-else class=":uno: flex flex-col gap-2">
      <div
        v-for="group in groups"
        :key="group.metadata.name"
        class=":uno: flex items-center justify-between rounded-md border border-gray-100 px-3 py-2"
      >
        <div class=":uno: flex items-center gap-2">
          <span class=":uno: text-sm">
            {{ group.spec.displayName || group.metadata.name }}
          </span>
          <span v-if="(group.spec.priority || 0) > 0" class=":uno: text-xs text-gray-400">
            优先展示
          </span>
        </div>
        <VSpace spacing="sm">
          <VButton size="sm" type="secondary" @click="handleOpenEdit(group)">
            编辑
          </VButton>
          <VButton size="sm" type="danger" plain @click="handleDelete(group)">
            删除
          </VButton>
        </VSpace>
      </div>
    </div>
  </VModal>

  <!-- 编辑弹窗与主弹窗平级（不嵌套），避免 VModal(Teleport) 嵌套触发 Vue insertBefore 错误 -->
  <LinkGroupEditingModal
    v-if="editingModalVisible"
    :item="editingGroup"
    @close="editingModalVisible = false"
    @saved="handleSaved"
  />
</template>

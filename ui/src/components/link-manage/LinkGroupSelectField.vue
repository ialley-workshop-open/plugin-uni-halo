<script setup lang="ts">
import { IconAddCircle, VButton } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, ref } from "vue";
import LinkGroupEditingModal from "@/components/link-manage/LinkGroupEditingModal.vue";
import { miniProgramLinkGroupsApi } from "@/api";
import type { MiniProgramLinkGroup } from "@/types";

const props = withDefaults(
  defineProps<{
    /** 当前选中分组 name（空字符串=未分组） */
    modelValue?: string;
    /** 下拉帮助文案 */
    help?: string;
  }>(),
  {
    modelValue: "",
    help: "选择一个分组",
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

const queryClient = useQueryClient();

// 分组选项（审核时可调整分组；「未分组」value 为空字符串）
const { data: groupsData } = useQuery({
  queryKey: ["uni-halo:mini-program-link-groups-options"],
  queryFn: async () => {
    const result = await miniProgramLinkGroupsApi.list({ page: 1, size: 100 });
    return result.items;
  },
});

const groupOptions = computed(() => {
  const options: { label: string; value: string }[] = [{ label: "未分组", value: "" }];
  groupsData.value?.forEach((group) => {
    options.push({
      label: group.spec.displayName || group.metadata.name,
      value: group.metadata.name,
    });
  });
  return options;
});

// 新建分组弹窗（与链接列表共用 LinkGroupEditingModal）
const createVisible = ref(false);

/** 新建分组保存成功：刷新选项并自动选中新分组 */
const handleGroupSaved = (group: MiniProgramLinkGroup) => {
  createVisible.value = false;
  queryClient.invalidateQueries({ queryKey: ["uni-halo:mini-program-link-groups-options"] });
  emit("update:modelValue", group.metadata.name);
};
</script>

<template>
  <div>
    <FormKit
      :model-value="modelValue"
      name="groupName"
      label="分组"
      type="select"
      :options="groupOptions"
      :help="help"
      @update:model-value="emit('update:modelValue', $event as string)"
    />
    <div class=":uno: mb-4 flex -mt-2">
      <VButton size="sm" type="secondary" @click="createVisible = true">
        <template #icon>
          <IconAddCircle />
        </template>
        新建分组
      </VButton>
    </div>
    <LinkGroupEditingModal
      v-if="createVisible"
      @close="createVisible = false"
      @saved="handleGroupSaved"
    />
  </div>
</template>

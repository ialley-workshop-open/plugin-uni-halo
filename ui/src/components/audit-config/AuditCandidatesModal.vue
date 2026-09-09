<script setup lang="ts">
import { Toast, VButton, VEmpty, VLoading, VModal, VPagination, VSpace } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { computed, onMounted, ref, watch } from "vue";
import RiImageLine from "~icons/ri/image-line";
import { auditDataApi } from "@/api";
import {
  AUDIT_CANDIDATE_PLUGIN_HINTS,
  AUDIT_CANDIDATE_TYPE_LABELS,
  type AuditCandidateType,
  type AuditDataRef,
} from "@/types";

const props = withDefaults(
  defineProps<{
    type: AuditCandidateType;
    selected?: AuditDataRef[];
    /** 最大可选数量（默认不限；首页分类栏固定 3 个时传 3） */
    max?: number;
  }>(),
  { selected: () => [], max: Infinity }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "confirm", selected: AuditDataRef[]): void;
}>();

const PAGE_SIZE = 8;
const keyword = ref("");
const page = ref(1);
const localSelected = ref<AuditDataRef[]>([]);

// 父组件 v-if 挂载，挂载即打开：重置搜索与已选回显
onMounted(() => {
  keyword.value = "";
  page.value = 1;
  localSelected.value = [...props.selected];
});

// 关键字变化（FormKit 输入）重置到第一页（查询由 queryKey 含 keyword 自动触发）
watch(keyword, () => {
  page.value = 1;
});

const { data, isLoading } = useQuery({
  queryKey: ["uni-halo:audit-candidates", props.type, keyword, page],
  queryFn: async () => {
    return auditDataApi.candidates(props.type, {
      keyword: keyword.value || undefined,
      page: page.value,
      size: PAGE_SIZE,
    });
  },
});

const label = computed(() => AUDIT_CANDIDATE_TYPE_LABELS[props.type]);

const isChecked = (candidate: AuditDataRef) =>
  localSelected.value.some((item) => item.name === candidate.name);

const toggleItem = (candidate: AuditDataRef) => {
  const index = localSelected.value.findIndex((item) => item.name === candidate.name);
  if (index >= 0) {
    localSelected.value.splice(index, 1);
    return;
  }
  if (props.max > 0 && localSelected.value.length >= props.max) {
    Toast.warning(`最多选择 ${props.max} 个`);
    return;
  }
  localSelected.value.push({ ...candidate });
};

const handleConfirm = () => {
  emit("confirm", [...localSelected.value]);
  emit("update:visible", false);
};

const handleClose = () => {
  emit("update:visible", false);
};
</script>

<template>
  <VModal
    :title="`选择${label}`"
    :width="720"
    @close="handleClose"
  >
    <div class=":uno: flex flex-col gap-3">
      <!-- 搜索 + 已选计数（FormKit 输入，无 label；关键字变化经 watch 重置页码） -->
      <div class=":uno: flex items-center gap-3">
        <FormKit
          v-model="keyword"
          type="text"
          :placeholder="`输入关键字搜索${label}…`"
          outer-class=":uno: !pt-0"
          class=":uno: w-full flex-1"
        />
        <span class=":uno: shrink-0 text-sm text-gray-500">
          已选 <b class=":uno: text-primary">{{ localSelected.length }}</b> 条
        </span>
      </div>

      <VLoading v-if="isLoading" />

      <!-- 数据源插件未安装 -->
      <VEmpty
        v-else-if="data?.pluginMissing"
        :title="`未检测到${label}数据源`"
        :message="AUDIT_CANDIDATE_PLUGIN_HINTS[type] || '请确认对应插件已安装并启用'"
      />

      <template v-else>
        <!-- 候选列表 -->
        <div class=":uno: max-h-96 overflow-y-auto rounded-md border border-gray-100">
          <div
            v-for="candidate in data?.items || []"
            :key="candidate.name"
            class=":uno: flex cursor-pointer items-center gap-3 border-b border-gray-50 px-3 py-2.5 last:border-b-0"
            :class="isChecked(candidate) ? ':uno: bg-blue-50' : ':uno: hover:bg-gray-50'"
            @click="toggleItem(candidate)"
          >
            <span
              class=":uno: flex h-4 w-4 shrink-0 items-center justify-center rounded border text-[11px] text-white"
              :class="
                isChecked(candidate)
                  ? ':uno: border-primary bg-primary'
                  : ':uno: border-gray-300'
              "
            >
              {{ isChecked(candidate) ? "✓" : "" }}
            </span>
            <div
              class=":uno: flex h-11 w-11 shrink-0 items-center justify-center overflow-hidden rounded-md bg-gray-100 text-lg"
            >
              <img
                v-if="candidate.cover"
                :src="candidate.cover"
                class=":uno: h-full w-full object-cover"
                alt=""
              />
              <RiImageLine v-else class=":uno: h-5 w-5 text-gray-300" />
            </div>
            <div class=":uno: min-w-0 flex-1">
              <!-- 瞬间内容为富文本 HTML，v-html 渲染保留格式；其余类型纯文本插值 -->
              <div
                v-if="type === 'moment'"
                class=":uno: truncate text-sm font-medium text-gray-800 [&_p]:inline [&_p]:m-0"
                v-html="candidate.title"
              />
              <div
                v-else
                class=":uno: truncate text-sm font-medium text-gray-800"
              >
                {{ candidate.title || candidate.name }}
              </div>
              <div class=":uno: mt-0.5 truncate text-xs text-gray-400">
                {{ [candidate.subTitle, candidate.extra].filter(Boolean).join(" · ") }}
              </div>
            </div>
          </div>
          <div v-if="!data?.items?.length" class=":uno: py-8">
            <VEmpty title="没有匹配的数据" message="换个关键字试试" />
          </div>
        </div>

        <!-- 分页 -->
        <div class=":uno: flex justify-end">
          <VPagination
            :page="data?.page || 1"
            :size="data?.size || PAGE_SIZE"
            :total="data?.total || 0"
            :page-visible="5"
            @update:page="page = $event"
          />
        </div>
      </template>
    </div>

    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="emit('update:visible', false)">取消</VButton>
        <VButton type="primary" @click="handleConfirm">
          确认选择（{{ localSelected.length }}）
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

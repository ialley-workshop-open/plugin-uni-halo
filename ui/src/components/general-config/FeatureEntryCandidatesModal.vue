<script setup lang="ts">
import { VButton, VEmpty, VModal, VSpace } from "@halo-dev/components";
import { computed, onMounted, ref } from "vue";
import {
  FEATURE_ENTRY_REGISTRY,
  type FeatureEntry,
} from "@/constant/feature-entries";

/**
 * 功能入口候选弹窗（2026-09-10 新增，设计见 .docs/feature-entry-unified-design.md）。
 *
 * <p>数据源为前端静态注册表 {@link FEATURE_ENTRY_REGISTRY}（统一清单，**不按 group 分组**
 * 展示全部条目；与审核配置候选的后端跨插件查询范式不同）。语义为「追加选择」：
 * 已配置条目（selectedKeys）置灰禁选，确认后返回新选中条目，由父组件追加到目标列表。</p>
 */
const props = withDefaults(
  defineProps<{
    /** 已配置条目的 key 列表（置灰禁用，不可再选） */
    selectedKeys?: string[];
  }>(),
  { selectedKeys: () => [] }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "confirm", selected: FeatureEntry[]): void;
}>();

const keyword = ref("");
const localSelected = ref<FeatureEntry[]>([]);

// 父组件 v-if 挂载，挂载即打开：重置搜索与已选
onMounted(() => {
  keyword.value = "";
  localSelected.value = [];
});

/** 注册表统一清单 + 关键字过滤（key/title/subTitle 命中；不按 group 分组） */
const candidates = computed<FeatureEntry[]>(() => {
  const kw = keyword.value.trim().toLowerCase();
  return FEATURE_ENTRY_REGISTRY.filter(
    (entry) =>
      !kw ||
      entry.title?.toLowerCase().includes(kw) ||
      entry.subTitle?.toLowerCase().includes(kw) ||
      entry.key?.toLowerCase().includes(kw)
  );
});

/** 已在目标列表中的条目（置灰禁选） */
const isTaken = (candidate: FeatureEntry) =>
  props.selectedKeys.includes(candidate.key || "");

const isChecked = (candidate: FeatureEntry) =>
  localSelected.value.some((item) => item.key === candidate.key);

const toggleItem = (candidate: FeatureEntry) => {
  if (isTaken(candidate)) {
    return;
  }
  const index = localSelected.value.findIndex((item) => item.key === candidate.key);
  if (index >= 0) {
    localSelected.value.splice(index, 1);
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
  <VModal title="选择功能入口" :width="720" @close="handleClose">
    <div class=":uno: flex flex-col gap-3">
      <!-- 搜索 + 已选计数 -->
      <div class=":uno: flex items-center gap-3">
        <FormKit
          v-model="keyword"
          type="text"
          placeholder="输入关键字搜索功能入口…"
          outer-class=":uno: !pt-0"
          class=":uno: w-full flex-1"
        />
        <span class=":uno: shrink-0 text-sm text-gray-500">
          已选 <b class=":uno: text-primary">{{ localSelected.length }}</b> 条
        </span>
      </div>

      <!-- 候选列表（静态注册表；已配置条目置灰禁选） -->
      <div class=":uno: max-h-96 overflow-y-auto rounded-md border border-gray-100">
        <div
          v-for="candidate in candidates"
          :key="candidate.key"
          class=":uno: flex cursor-pointer items-center gap-3 border-b border-gray-50 px-3 py-2.5 last:border-b-0"
          :class="[
            isTaken(candidate)
              ? ':uno: cursor-not-allowed bg-gray-50 opacity-50'
              : isChecked(candidate)
                ? ':uno: bg-blue-50'
                : ':uno: hover:bg-gray-50',
          ]"
          @click="toggleItem(candidate)"
        >
          <span
            class=":uno: flex h-4 w-4 shrink-0 items-center justify-center rounded border text-[11px] text-white"
            :class="
              isChecked(candidate) ? ':uno: border-primary bg-primary' : ':uno: border-gray-300'
            "
          >
            {{ isChecked(candidate) ? "✓" : "" }}
          </span>
          <span
            class=":uno: flex h-8 w-8 shrink-0 items-center justify-center rounded-md text-sm font-medium text-white"
            :style="{ backgroundColor: candidate.bgColor, color: candidate.color }"
          >
            {{ candidate.icon || "•" }}
          </span>
          <div class=":uno: min-w-0 flex-1">
            <div class=":uno: flex items-baseline gap-2">
              <span class=":uno: truncate text-sm font-medium text-gray-800">
                {{ candidate.title }}
              </span>
              <span v-if="candidate.subTitle" class=":uno: truncate text-xs text-gray-400">
                {{ candidate.subTitle }}
              </span>
            </div>
            <div class=":uno: mt-0.5 truncate text-xs text-gray-400">
              {{ candidate.path || "about 页内入口" }}
            </div>
          </div>
          <span v-if="isTaken(candidate)" class=":uno: shrink-0 text-xs text-gray-400">
            已配置
          </span>
        </div>
        <div v-if="!candidates.length" class=":uno: py-8">
          <VEmpty title="没有匹配的功能入口" message="换个关键字试试" />
        </div>
      </div>
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

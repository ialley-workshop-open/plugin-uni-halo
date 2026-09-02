<script setup lang="ts">
import { VButton, VEmpty, VLoading, VModal, VPagination, VSpace } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import { onMounted, ref } from "vue";
import RiImageLine from "~icons/ri/image-line";
import { bannerApi } from "@/api";
import type { BannerCandidate } from "@/types";

const props = withDefaults(
  defineProps<{
    /** 多选模式（新建-从文章批量添加）；false 为单选（更换文章） */
    multiSelect?: boolean;
    selected?: BannerCandidate[];
  }>(),
  {
    multiSelect: true,
    selected: () => [],
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "confirm", selected: BannerCandidate[]): void;
}>();

const PAGE_SIZE = 8;
const keyword = ref("");
const page = ref(1);
const localSelected = ref<BannerCandidate[]>([]);

// 父组件 v-if 挂载，挂载即打开：重置搜索与已选回显
onMounted(() => {
  keyword.value = "";
  page.value = 1;
  localSelected.value = [...props.selected];
});

const { data, isLoading } = useQuery({
  queryKey: ["uni-halo:banner-candidates", keyword, page],
  queryFn: async () => {
    return bannerApi.candidates({
      keyword: keyword.value || undefined,
      page: page.value,
      size: PAGE_SIZE,
    });
  },
});

const isChecked = (candidate: BannerCandidate) =>
  localSelected.value.some((item) => item.name === candidate.name);

const toggleItem = (candidate: BannerCandidate) => {
  if (!props.multiSelect) {
    // 单选模式：点击即选中，再次点击取消
    localSelected.value = isChecked(candidate) ? [] : [candidate];
    return;
  }
  const index = localSelected.value.findIndex((item) => item.name === candidate.name);
  if (index >= 0) {
    localSelected.value.splice(index, 1);
  } else {
    localSelected.value.push({ ...candidate });
  }
};

const handleConfirm = () => {
  emit("confirm", [...localSelected.value]);
  emit("update:visible", false);
};

const handleClose = () => {
  emit("update:visible", false);
};

const formatTime = (value?: string) => {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};
</script>

<template>
  <VModal :title="`选择文章`" :width="720" @close="handleClose">
    <div class=":uno: flex flex-col gap-3">
      <!-- 搜索（Halo SearchInput 全局组件） + 已选计数 -->
      <div class=":uno: flex items-center gap-3">
        <SearchInput v-model="keyword" class=":uno: flex-1" placeholder="输入关键字搜索文章…" />
        <span class=":uno: shrink-0 text-sm text-gray-500">
          已选 <b class=":uno: text-primary">{{ localSelected.length }}</b> 条
        </span>
      </div>

      <VLoading v-if="isLoading" />

      <template v-else>
        <!-- 候选列表（多选/单选） -->
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
              <div class=":uno: truncate text-sm font-medium text-gray-800">
                {{ candidate.title || candidate.name }}
              </div>
              <div class=":uno: mt-0.5 truncate text-xs text-gray-400">
                {{
                  [formatTime(candidate.publishTime), candidate.categories?.[0]]
                    .filter(Boolean)
                    .join(" · ")
                }}
              </div>
            </div>
          </div>
          <div v-if="!data?.items?.length" class=":uno: py-8">
            <VEmpty title="没有匹配的文章" message="换个关键字试试" />
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

      <!-- 快照语义提示 -->
      <div class=":uno: rounded-md bg-blue-50 px-3 py-2 text-xs text-blue-600">
        选择后将保存文章当前快照（标题/封面/日期/作者），文章后续修改不影响轮播图展示
      </div>
    </div>

    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="handleClose">取消</VButton>
        <VButton type="primary" :disabled="!localSelected.length" @click="handleConfirm">
          确认选择（{{ localSelected.length }}）
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>

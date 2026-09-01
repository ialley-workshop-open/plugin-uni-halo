<script setup lang="ts">
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { useWindowSize } from "@vueuse/core";
import { computed, ref, watch } from "vue";
import RiArrowLeftLine from "~icons/ri/arrow-left-line";
import RiArrowRightLine from "~icons/ri/arrow-right-line";

const props = withDefaults(
  defineProps<{
    visible?: boolean;
    /** 图片地址列表（单图时传一个元素即可） */
    images?: string[];
    /** 打开时初始显示的图片索引（多图时从点击的那张开始） */
    initialIndex?: number;
    title?: string;
  }>(),
  {
    visible: false,
    images: () => [],
    initialIndex: 0,
    title: "图片预览",
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
}>();

// 预览弹窗：宽度 80vw（VModal width 为 px 数值，动态计算）、高度 80vh
const { width: windowWidth } = useWindowSize();
const previewWidth = computed(() => Math.round(windowWidth.value * 0.8));

const currentIndex = ref(0);

// 每次打开时重置到初始索引（点击的那张图）
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      currentIndex.value = Math.min(
        Math.max(props.initialIndex, 0),
        Math.max(props.images.length - 1, 0)
      );
    }
  }
);

const currentImage = computed(() => props.images[currentIndex.value]);

const handlePrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value -= 1;
  }
};

const handleNext = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value += 1;
  }
};
</script>

<template>
  <VModal
    :visible="visible"
    :title="title"
    :width="previewWidth"
    height="80vh"
    :body-class="[':uno: !p-4']"
    mount-to-body
    @update:visible="emit('update:visible', $event)"
    @close="emit('update:visible', false)"
  >
    <div class=":uno: relative flex h-full w-full items-center justify-center">
      <img
        v-if="currentImage"
        :src="currentImage"
        class=":uno: max-h-full max-w-full object-contain"
        alt=""
      />
      <div v-else class=":uno: text-sm text-gray-400">暂无图片</div>

      <template v-if="images.length > 1">
        <VButton
          class=":uno: absolute left-2 top-1/2 -translate-y-1/2"
          type="secondary"
          circle
          :disabled="currentIndex === 0"
          @click="handlePrev"
        >
          <template #icon>
            <RiArrowLeftLine />
          </template>
        </VButton>
        <VButton
          class=":uno: absolute right-2 top-1/2 -translate-y-1/2"
          type="secondary"
          circle
          :disabled="currentIndex === images.length - 1"
          @click="handleNext"
        >
          <template #icon>
            <RiArrowRightLine />
          </template>
        </VButton>
        <div class=":uno: absolute bottom-2 left-1/2 -translate-x-1/2">
          <VSpace spacing="sm">
            <span class=":uno: rounded bg-gray-900/70 px-2 py-0.5 text-xs text-white">
              {{ currentIndex + 1 }} / {{ images.length }}
            </span>
          </VSpace>
        </div>
      </template>
    </div>
  </VModal>
</template>

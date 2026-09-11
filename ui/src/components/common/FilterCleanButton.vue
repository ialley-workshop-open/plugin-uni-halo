<script setup lang="ts">
import { IconCloseCircle } from "@halo-dev/components";

const emit = defineEmits<{
  (event: "click"): void;
}>();
</script>

<template>
  <div
    class=":uno: flex cursor-pointer select-none items-center gap-1 text-sm text-gray-400 hover:text-red-500"
    @click="emit('click')"
  >
    <IconCloseCircle class=":uno: h-4 w-4" />
    清除
  </div>
</template>

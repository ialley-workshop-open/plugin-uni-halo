<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import { useMagicKeys } from "@vueuse/core";
import { useAttrs, watchEffect } from "vue";

const props = withDefaults(
  defineProps<{
    text?: string;
  }>(),
  {
    text: "提交",
  }
);

const emit = defineEmits<{
  (event: "submit"): void;
}>();

const attrs = useAttrs();

const { Command_Enter, Ctrl_Enter } = useMagicKeys();

watchEffect(() => {
  if (Command_Enter?.value || Ctrl_Enter?.value) {
    emit("submit");
  }
});
</script>

<template>
  <VButton v-bind="attrs" @click="emit('submit')">
    {{ text }}
  </VButton>
</template>

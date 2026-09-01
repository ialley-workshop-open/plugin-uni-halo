<script setup lang="ts">
import { ExtensionsKit, RichTextEditor, VueEditor } from "@halo-dev/richtext-editor";
// 注意：不引入 @halo-dev/richtext-editor/dist/style.css —— bundler-kit 禁止 shared
// 依赖的子路径导入（Unsupported shared dependency subpath），且该包为 Halo 宿主
// 内置依赖，样式已由控制台全局加载，插件端无需（也不允许）重复引入。
import { nextTick, onBeforeUnmount, onMounted, shallowRef, watch } from "vue";

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
  }>(),
  {
    modelValue: "",
    placeholder: "输入 / 以选择输入类型，支持图文混排、表格",
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string): void;
}>();

// 官方 README 推荐用法：shallowRef 持有实例，onMounted 中 new VueEditor 创建；
// 扩展直接用预置 ExtensionsKit（可通过 configure 覆盖各扩展选项）。
// 注：控制台的 "[tiptap warn]: Duplicate extension names found: ['textStyleKit',
// 'textStyle', ...]" 为包自身问题（官方 README 同样触发），实测无害，可忽略。
const editor = shallowRef<VueEditor>();

onMounted(() => {
  editor.value = new VueEditor({
    content: props.modelValue,
    extensions: [
      ExtensionsKit.configure({
        placeholder: {
          placeholder: props.placeholder,
        },
      }),
    ],
    parseOptions: {
      preserveWhitespace: true,
    },
    onUpdate: () => {
      emit("update:modelValue", editor.value?.getHTML() || "");
    },
  });
  // 回显兜底：极端时序下初始 content 可能未进编辑器（编辑器为空），
  // 此时显式 setContent 一次确保回显；已有内容则不动，避免重置光标。
  nextTick(() => {
    if (editor.value && props.modelValue && editor.value.isEmpty) {
      editor.value.commands.setContent(props.modelValue);
    }
  });
});

// 外部内容变化（如编辑回显）时同步进编辑器，避免光标跳动
watch(
  () => props.modelValue,
  (value) => {
    if (editor.value && value !== editor.value.getHTML()) {
      editor.value.commands.setContent(value || "");
    }
  }
);

onBeforeUnmount(() => {
  editor.value?.destroy();
});
</script>

<template>
  <div class=":uno: h-full overflow-hidden rounded-md border border-gray-200">
    <RichTextEditor v-if="editor" :editor="editor" />
  </div>
</template>

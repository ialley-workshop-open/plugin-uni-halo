<script setup lang="ts">
import { Toast, VButton, VModal, VSpace } from "@halo-dev/components";
import { submitForm } from "@formkit/core";
import { cloneDeep } from "lodash-es";
import { computed, ref, watch } from "vue";
import SubmitButton from "../../button/SubmitButton.vue";
import { loveAlbumsApi, type LoveAlbumWriteRequest } from "@/api";
import type { LoveAlbum } from "@/types";

const props = withDefaults(
  defineProps<{
    album?: LoveAlbum;
  }>(),
  {
    album: undefined,
  }
);

const emit = defineEmits<{
  (event: "close"): void;
}>();

const modal = ref<InstanceType<typeof VModal> | null>(null);
const saving = ref(false);

const passwordInput = ref("");
const passwordConfirm = ref("");
const clearPassword = ref(false);

const isUpdateMode = computed(() => !!props.album);
const hasPassword = computed(() => props.album?.spec.passwordEnabled === true);

const formState = ref<LoveAlbum>({
  metadata: { name: "" },
  spec: { displayName: "", description: "", cover: "", priority: 0, photos: [] },
});

watch(
  () => props.album,
  (album) => {
    if (album) {
      formState.value = cloneDeep(album);
      if (!formState.value.spec.photos) {
        formState.value.spec.photos = [];
      }
    }
  },
  {
    immediate: true,
  }
);

const handleSubmit = () => {
  submitForm("love-album-form");
};

const handleSave = async () => {
  if (passwordInput.value !== passwordConfirm.value) {
    Toast.error("两次输入的密码不一致");
    return;
  }
  const body: LoveAlbumWriteRequest = { album: formState.value };
  if (clearPassword.value) {
    body.passwordRemoved = true;
  } else if (passwordInput.value) {
    body.password = passwordInput.value;
  }
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await loveAlbumsApi.update(formState.value.metadata.name, body);
    } else {
      await loveAlbumsApi.create(body);
    }
    modal.value?.close();
    Toast.success("保存成功");
  } catch (error) {
    Toast.error((error as Error).message);
  } finally {
    saving.value = false;
  }
};
</script>

<template>
  <VModal ref="modal" :title="isUpdateMode ? '编辑相册' : '新建相册'" :width="640" @close="emit('close')">
    <FormKit
      id="love-album-form"
      type="form"
      name="love-album-form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleSave"
    >
      <FormKit
        v-model="formState.spec.displayName"
        name="displayName"
        label="相册名称"
        type="text"
        validation="required"
        :validation-messages="{ required: '相册名称不能为空' }"
        placeholder="例如：我们的恋爱相册"
      />
      <FormKit
        v-model="formState.spec.description"
        name="description"
        label="相册描述"
        type="textarea"
        rows="3"
        placeholder="简要描述这个相册"
      />
      <FormKit
        v-model="formState.spec.cover"
        name="cover"
        label="封面图"
        type="attachment"
        :accepts="['image/*']"
        help="列表页展示的封面，可从附件库选择或输入图片地址"
      />
      <FormKit
        v-model="formState.spec.priority"
        name="priority"
        label="排序"
        type="number"
        help="数值越大越靠前"
        placeholder="数值越大越靠前"
      />

      <!-- 查看密码 -->
      <div class=":uno: rounded-md border border-gray-200 p-4">
        <div class=":uno: mb-2 flex items-center text-sm font-semibold text-gray-700">
          查看密码
          <span v-if="hasPassword" class=":uno: ml-2 text-xs font-normal text-emerald-600">
            已加密
          </span>
          <span v-else class=":uno: ml-2 text-xs font-normal text-gray-400">未设置</span>
        </div>
        <template v-if="!clearPassword">
          <FormKit
            v-model="passwordInput"
            name="password"
            label="新密码"
            type="password"
            :help="isUpdateMode ? '留空表示保持原密码不变' : '设置后查看相册需要输入密码'"
            placeholder="输入相册查看密码"
          />
          <FormKit
            v-if="passwordInput"
            v-model="passwordConfirm"
            name="passwordConfirm"
            label="确认密码"
            type="password"
            validation="required"
            placeholder="再次输入确认密码"
          />
        </template>
        <div v-if="clearPassword" class=":uno: text-sm text-gray-500">
          保存后将清除该相册的查看密码。
        </div>
        <VButton
          v-if="hasPassword && !clearPassword"
          size="sm"
          type="danger"
          plain
          class=":uno: mt-2"
          @click="clearPassword = true"
        >
          清除密码
        </VButton>
      </div>
    </FormKit>

    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="saving"
          :disabled="saving"
          type="secondary"
          text="提交"
          @submit="handleSubmit"
        />
        <VButton @click="modal?.close()">关闭</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

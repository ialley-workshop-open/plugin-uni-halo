<script setup lang="ts">
import {Toast, VCard, VPageHeader, VSpace} from "@halo-dev/components";
import {useQuery, useQueryClient} from "@tanstack/vue-query";
import {submitForm} from "@formkit/core";
import {cloneDeep} from "lodash-es";
import {ref, watch} from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import {loveConfigApi} from "@/api";
import type {LoveConfig} from "@/types";

const queryClient = useQueryClient();

const {data: config, isLoading} = useQuery({
  queryKey: ["uni-halo:love-config"],
  queryFn: () => loveConfigApi.get(),
});

const formState = ref<LoveConfig>(defaultForm());

function defaultForm(): LoveConfig {
  return {
    metadata: {name: ""},
    spec: {
      loveDateTitle: "这是我们一起走过的",
      loveDate: "",
      loveInfo: {boyNickname: "", boyAvatar: "", girlNickname: "", girlAvatar: ""},
    },
  };
}

watch(
        () => config.value,
        (value) => {
          if (!value) {
            return;
          }
          const loaded = cloneDeep(value);
          const spec = loaded.spec || {};
          spec.loveInfo = {
            boyNickname: "",
            boyAvatar: "",
            girlNickname: "",
            girlAvatar: "",
            ...spec.loveInfo,
          };
          spec.loveDateTitle = spec.loveDateTitle || "这是我们一起走过的";
          loaded.spec = spec;
          formState.value = loaded;
        },
        {immediate: true}
);

const handleSubmit = () => {
  submitForm("love-config-form");
};

const handleSave = async () => {
  try {
    await loveConfigApi.save(formState.value);
    Toast.success("保存成功");
    queryClient.invalidateQueries({queryKey: ["uni-halo:love-config"]});
  } catch (error) {
    Toast.error((error as Error).message);
  }
};
</script>

<template>
  <VPageHeader title="UniHalo-恋爱配置"/>
  <div class=":uno: m-0 flex flex-col gap-4 md:m-4">
    <VCard :loading="isLoading">
      <FormKit
              id="love-config-form"
              type="form"
              name="love-config-form"
              :config="{ validationVisibility: 'submit' }"
              @submit="handleSave"
      >
        <!-- 纪念日 -->
        <div class=":uno: mb-4">
          <div class=":uno: mb-2 text-sm font-semibold text-gray-700">纪念日</div>
          <FormKit
                  v-model="formState.spec.loveDateTitle"
                  name="loveDateTitle"
                  label="纪念日标题"
                  type="text"
                  validation="required"
                  :validation-messages="{ required: '纪念日标题不能为空' }"
                  placeholder="例如：我们在一起的那天"
          />
          <FormKit
                  v-model="formState.spec.loveDate"
                  name="loveDate"
                  label="恋爱纪念日"
                  type="date"
                  validation="required"
                  :validation-messages="{ required: '恋爱纪念日不能为空' }"
                  help="用于计算恋爱天数，同时这可是一个非常重要的节日呢，可不能忘记哦~"
                  placeholder="选择纪念日日期"
          />
        </div>

        <!-- 恋人信息 -->
        <div class=":uno: mb-4">
          <div class=":uno: mb-2 text-sm font-semibold text-gray-700">恋人信息</div>
          <div class=":uno: grid grid-cols-1 gap-4 md:grid-cols-2">
            <div class="flex flex-col">
              <FormKit
                      v-model="formState.spec.loveInfo!.boyAvatar"
                      name="boyAvatar"
                      label="男生头像"
                      type="attachment"
                      :accepts="['image/*']"
                      validation="required"
                      :validation-messages="{ required: '男生头像不能为空' }"
              />
              <FormKit
                      v-model="formState.spec.loveInfo!.boyNickname"
                      name="boyNickname"
                      label="男生昵称"
                      type="text"
                      validation="required"
                      :validation-messages="{ required: '男生昵称不能为空' }"
                  placeholder="男生的昵称"
              />
            </div>
            <div class="flex flex-col">
              <FormKit
                      v-model="formState.spec.loveInfo!.girlAvatar"
                      name="girlAvatar"
                      label="女生头像"
                      type="attachment"
                      :accepts="['image/*']"
                      validation="required"
                      :validation-messages="{ required: '女生头像不能为空' }"
              />
              <FormKit
                      v-model="formState.spec.loveInfo!.girlNickname"
                      name="girlNickname"
                      label="女生昵称"
                      type="text"
                      validation="required"
                      :validation-messages="{ required: '女生昵称不能为空' }"
                  placeholder="女生的昵称"
              />
            </div>
          </div>
        </div>
      </FormKit>

      <template #footer>
        <div class=":uno: flex justify-end">
          <VSpace>
            <SubmitButton type="secondary" text="保存" @submit="handleSubmit"/>
          </VSpace>
        </div>
      </template>
    </VCard>
  </div>
</template>

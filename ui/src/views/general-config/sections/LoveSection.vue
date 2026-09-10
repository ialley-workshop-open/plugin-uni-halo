<script setup lang="ts">
import {VButton, VSwitch} from "@halo-dev/components";
import { inject } from "vue";
import type { GeneralConfigLove } from "@/types";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 恋爱设置分区（2026-09-10 组件化拆分）：
 * 基本设置（背景图）/ 模块入口（开关 + 入口密码；2026-09-08 起无图标配置）。
 */
defineProps<{ subTab: string }>();

const { formState } = inject(GeneralConfigFormKey)!;

/** 恋爱模块入口（模块入口分区渲染数据源；password 语义见 GeneralConfigLoveModule 类型） */
const LOVE_MODULES: Array<{key: "ourStory" | "lovePhoto" | "loveDaily"; label: string; desc: string}> = [
  {key: "ourStory", label: "恋爱故事", desc: "恋爱页是否展示「恋爱故事」入口"},
  {key: "lovePhoto", label: "恋爱相册", desc: "恋爱页是否展示「恋爱相册」入口"},
  {key: "loveDaily", label: "恋爱清单", desc: "恋爱页是否展示「恋爱清单」入口"},
];

/** 清除某模块入口密码（清空新密码输入并标记 passwordRemoved，保存时后端清除） */
function clearModulePassword(key: "ourStory" | "lovePhoto" | "loveDaily") {
  const module = formState.value.spec.love[key];
  if (module) {
    module.password = "";
    module.passwordRemoved = true;
  }
}

/** 新密码输入时自动取消「清除密码」标记（重设优先级高于清除） */
function cancelRemovalOnTyping(module: GeneralConfigLove["ourStory"]) {
  if (module?.passwordRemoved) {
    module.passwordRemoved = false;
  }
}
</script>

<template>
  <!-- 恋爱 → 基本设置（恋爱页背景图；2026-09-10 起去掉「启用恋爱日记」总开关，
       入口展示由模块入口开关与 navList 统一管理） -->
  <template v-if="subTab === 'basic'">
    <p class=":uno: mb-3 text-xs text-gray-400">恋爱数据在「恋爱管理」菜单维护，此处配置恋爱页背景图；恋爱入口展示由「模块入口」开关统一管理。</p>
    <FormKit v-model="formState.spec.love.pageImages!.bgImageUrl" name="love_bg_image" label="背景图片" type="attachment" :accepts="['image/*']" />
  </template>

  <!-- 恋爱 → 模块入口（开关 + 入口密码；2026-09-08 起无图标配置） -->
  <template v-if="subTab === 'modules'">
    <p class=":uno: mb-3 text-xs text-gray-400">
      以下为恋爱页各模块入口的展示开关与入口密码；模块数据分别在「恋爱管理-恋爱故事 / 恋爱相册 / 恋爱清单」维护。
      设置密码后，小程序端进入该模块前需先验证密码。
    </p>
    <div v-for="item in LOVE_MODULES" :key="item.key" class=":uno: mb-4 rounded-lg bg-gray-50 p-4">
      <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
        <div>
          <div class=":uno: text-sm text-gray-700">{{ item.label }}</div>
          <div class=":uno: mt-0.5 text-xs text-gray-400">{{ item.desc }}</div>
        </div>
        <VSwitch v-model="formState.spec.love[item.key]!.enabled" />
      </div>
      <div class=":uno: mt-3">
        <div class=":uno: mb-2 flex items-center text-sm text-gray-700">
          入口密码
          <span
            v-if="formState.spec.love[item.key]!.passwordEnabled"
            class=":uno: ml-2 text-xs font-normal text-emerald-600"
          >已设置</span>
          <span v-else class=":uno: ml-2 text-xs font-normal text-gray-400">未设置</span>
        </div>
        <FormKit
          v-if="!formState.spec.love[item.key]!.passwordRemoved"
          v-model="formState.spec.love[item.key]!.password"
          :name="`love_${item.key}_password`"
          label="新密码"
          type="password"
          :help="formState.spec.love[item.key]!.passwordEnabled
            ? '留空表示保持原密码不变'
            : '设置后进入该模块前需先输入密码'"
          placeholder="输入入口密码"
          @input="cancelRemovalOnTyping(formState.spec.love[item.key])"
        />
        <p v-else class=":uno: text-sm text-gray-500">保存后将清除该入口密码。</p>
        <VButton
          v-if="formState.spec.love[item.key]!.passwordEnabled && !formState.spec.love[item.key]!.passwordRemoved"
          size="sm"
          type="danger"
          plain
          class=":uno: mt-2"
          @click="clearModulePassword(item.key)"
        >
          清除密码
        </VButton>
        <VButton
          v-if="formState.spec.love[item.key]!.passwordRemoved"
          size="sm"
          type="secondary"
          plain
          class=":uno: mt-2"
          @click="formState.spec.love[item.key]!.passwordRemoved = false"
        >
          取消清除
        </VButton>
      </div>
    </div>
  </template>
</template>

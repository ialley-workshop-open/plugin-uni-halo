<script setup lang="ts">
import {VButton, VStatusDot, VSwitch} from "@halo-dev/components";
import { computed, inject, onUnmounted, ref } from "vue";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import { GeneralConfigFormKey } from "../form-context";

/**
 * 维护设置分区（2026-09-10 组件化拆分，设计见 .docs/maintenance-config-design.md）：
 * 维护时间（四态状态卡 + 走秒倒计时 + 开启开关 + 时间窗口）/ 维护内容（标题/说明/详情）。
 * 前端展示态规则与后端 MaintenanceResolver 一致；状态判定权威在服务端输出端。
 */
defineProps<{ subTab: string }>();

const { formState, save } = inject(GeneralConfigFormKey)!;

type MaintenanceViewStatus = "none" | "scheduled" | "active" | "ended";

const nowMs = ref(Date.now());
const clock = window.setInterval(() => {
  nowMs.value = Date.now();
}, 1000);
onUnmounted(() => window.clearInterval(clock));

/** 计算当前维护状态（enabled=false / endTime 已到 → 不维护；startTime 未来 → 预告） */
function resolveMaintenanceStatus(): MaintenanceViewStatus {
  const m = formState.value.spec.maintenance;
  if (!m?.enabled) {
    return "none";
  }
  const now = nowMs.value;
  const start = m.startTime ? Date.parse(m.startTime) : Number.NaN;
  const end = m.endTime ? Date.parse(m.endTime) : Number.NaN;
  if (Number.isFinite(end) && now >= end) {
    return "ended";
  }
  if (Number.isFinite(start) && now < start) {
    return "scheduled";
  }
  return "active";
}

const STATUS_META: Record<
  MaintenanceViewStatus,
  {label: string; state: "default" | "success" | "warning" | "error"; hint: string}
> = {
  none: {
    label: "未维护",
    state: "default",
    hint: "当前未启用维护模式。可填写开始/结束时间安排维护，或直接开启开关立即进入维护。",
  },
  scheduled: {
    label: "维护预告中",
    state: "warning",
    hint: "已安排维护，到开始时间将自动进入维护中；可修改开始时间或关闭开关取消。",
  },
  active: {
    label: "维护中",
    state: "error",
    hint: "小程序端已展示维护页。已填预计恢复时间则到点自动结束；未填则持续到手动关闭。",
  },
  ended: {
    label: "已按计划结束",
    state: "success",
    hint: "本次维护已到点自动结束，小程序端已恢复正常。如需继续维护，请将结束时间顺延至未来后保存。",
  },
};

const maintenanceStatus = computed<MaintenanceViewStatus>(() => resolveMaintenanceStatus());

const statusMeta = computed(() => STATUS_META[maintenanceStatus.value]);

/** 倒计时（scheduled → 距开始 / active → 距恢复），无目标或已归零返回 null */
function maintenanceCountdown(): {prefix: string; text: string} | null {
  const m = formState.value.spec.maintenance;
  if (!m) {
    return null;
  }
  const status = maintenanceStatus.value;
  const target =
    status === "scheduled" ? m.startTime : status === "active" ? m.endTime : undefined;
  if (!target) {
    return null;
  }
  const remaining = Date.parse(target) - nowMs.value;
  if (!Number.isFinite(remaining) || remaining <= 0) {
    return null;
  }
  const totalSeconds = Math.floor(remaining / 1000);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const pad = (n: number) => String(n).padStart(2, "0");
  const text =
    days > 0
      ? `${days} 天 ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
      : `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
  return {prefix: status === "scheduled" ? "距开始维护还有" : "预计恢复还有", text};
}

const maintenanceCountdownText = computed(() => maintenanceCountdown());

/** RFC3339 UTC → 本地 datetime-local 输入值（yyyy-MM-ddTHH:mm），空/非法返回空串 */
function toLocalInput(iso?: string): string {
  if (!iso) {
    return "";
  }
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  const pad = (n: number) => String(n).padStart(2, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}

/** 本地 datetime-local 输入值 → RFC3339 UTC；空输入返回 undefined（清除字段） */
function fromLocalInput(value: string): string | undefined {
  if (!value) {
    return undefined;
  }
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? undefined : date.toISOString();
}

const startTimeLocal = computed({
  get: () => toLocalInput(formState.value.spec.maintenance?.startTime),
  set: (value: string) => {
    formState.value.spec.maintenance.startTime = fromLocalInput(value);
  },
});

const endTimeLocal = computed({
  get: () => toLocalInput(formState.value.spec.maintenance?.endTime),
  set: (value: string) => {
    formState.value.spec.maintenance.endTime = fromLocalInput(value);
  },
});

/** 提前结束维护（置 enabled=false 并触发父组件保存） */
const endMaintenanceNow = async () => {
  const m = formState.value.spec.maintenance;
  if (!m) {
    return;
  }
  m.enabled = false;
  await save();
};
</script>

<template>
  <!-- 维护 → 维护时间（VTabbar 由右侧卡片 header 统一渲染；状态卡 + 开启开关 + 时间窗口） -->
  <template v-if="subTab === 'time'">
    <!-- 状态摘要卡：四态 + 实时倒计时 + 快捷操作 -->
    <div class=":uno: rounded-lg bg-gray-50 p-4">
      <div class=":uno: flex flex-wrap items-center gap-x-4 gap-y-2">
        <VStatusDot :state="statusMeta.state" :text="statusMeta.label" />
        <span v-if="maintenanceCountdownText" class=":uno: text-sm text-gray-500">
          {{ maintenanceCountdownText.prefix }} {{ maintenanceCountdownText.text }}
        </span>
        <VButton
          v-if="maintenanceStatus === 'active'"
          class=":uno: !ml-auto"
          type="danger"
          size="sm"
          @click="endMaintenanceNow"
        >
          提前结束维护
        </VButton>
      </div>
      <p class=":uno: mt-2 text-xs text-gray-400">{{ statusMeta.hint }}</p>
    </div>

    <!-- 开启维护开关（时间窗口紧随其后） -->
    <div class=":uno: mt-4 flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
      <div>
        <div class=":uno: text-sm text-gray-700">开启维护</div>
        <div class=":uno: mt-0.5 text-xs text-gray-400">开启后小程序端将展示维护页；不填开始时间 = 开启即立即进入维护</div>
      </div>
      <VSwitch v-model="formState.spec.maintenance.enabled" />
    </div>

    <!-- 维护时间窗口（开始时间 / 预计恢复时间） -->
    <div class=":uno: mt-4 rounded-lg bg-gray-50 p-4">
      <div class=":uno: mb-3 text-sm font-medium text-gray-700">维护时间窗口</div>
      <div class=":uno: mb-4">
        <label class=":uno: mb-1 block text-sm text-gray-700" for="maintenance_start_time">开始时间</label>
        <input id="maintenance_start_time" v-model="startTimeLocal" type="datetime-local" class=":uno: h-9 w-full max-w-xs rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition hover:border-gray-400" />
        <p class=":uno: mt-1 text-xs text-gray-400">留空 = 开启后立即维护；填写未来时间则先向用户展示维护预告与倒计时</p>
      </div>
      <div>
        <label class=":uno: mb-1 block text-sm text-gray-700" for="maintenance_end_time">预计恢复时间</label>
        <input id="maintenance_end_time" v-model="endTimeLocal" type="datetime-local" class=":uno: h-9 w-full max-w-xs rounded-lg border border-gray-300 bg-white px-3 text-sm text-gray-700 outline-none transition hover:border-gray-400" />
        <p class=":uno: mt-1 text-xs text-gray-400">留空 = 持续维护直到手动关闭；填写后到点自动结束（小程序端恢复）</p>
      </div>
    </div>
  </template>

  <!-- 维护 → 维护内容（标题 / 说明 / 详情） -->
  <template v-if="subTab === 'content'">
    <!-- 维护标题 -->
    <div class=":uno: mt-4">
      <FormKit v-model="formState.spec.maintenance.title" name="maintenance_title" label="维护标题" type="text" help="维护页展示的大标题，如「系统升级维护」" />
    </div>

    <!-- 维护说明（页面展示，纯文本） -->
    <div class=":uno: mt-4">
      <FormKit v-model="formState.spec.maintenance.notice" name="maintenance_notice" label="维护说明" type="textarea" help="展示在维护页标题下方；留空则展示默认提示文案" />
    </div>

    <!-- 维护详情（小程序端弹窗展示，富文本） -->
    <div class=":uno: mt-4">
      <div class=":uno: mb-2 text-sm text-gray-700">维护详情</div>
      <RichTextEditorField v-model="formState.spec.maintenance.description" placeholder="维护详情，支持图文混排……小程序端「维护详情」弹窗展示，留空则不展示详情入口" />
    </div>
  </template>
</template>

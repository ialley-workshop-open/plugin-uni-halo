import type { InjectionKey, Ref } from "vue";
import type { GeneralConfig } from "@/types";

/**
 * 通用配置表单共享上下文（2026-09-10 组件化拆分后 provide/inject）：
 *
 * <p>formState 为单一数据源（父组件加载/保存），子组件（各 Section）直接修改其
 * 嵌套属性——Vue 3 深度响应式会触发父组件 {@code deep watch(formState)}，dirty
 * 自动置 true，保存按钮状态天然同步，无需子组件各自维护 local state 或事件上报。</p>
 *
 * <p>save 供子组件触发整表单保存（如维护分区「提前结束维护」先置 enabled=false
 * 再保存）。</p>
 */
export interface GeneralConfigFormContext {
  /** 表单单一数据源（ref 由父组件持有） */
  formState: Ref<GeneralConfig>;
  /** 保存当前表单（父组件 handleSave：预检 + PUT + dirty 复位 + 失效缓存） */
  save: () => Promise<void>;
}

/** 通用配置表单上下文注入 key（父组件 provide，Section 组件 inject） */
export const GeneralConfigFormKey: InjectionKey<GeneralConfigFormContext> =
  Symbol("general-config-form");

/**
 * 配置读取与全局常量。
 *
 * 配置由 FloatingWindowHeadProcessor 在页面 <head> 内联注入
 * window.__UNI_HALO_FLOAT_MINI_PROFILE__；未注入时 CONFIG 为 undefined，
 * 由 index.ts 判定不挂载（fail closed）。
 */
import type { FloatMiniProfileConfig } from "./types";

export const CONFIG: FloatMiniProfileConfig | undefined =
  window.__UNI_HALO_FLOAT_MINI_PROFILE__;

export const STORAGE_KEY = "uh-fmp-closed";
export const EDGE_TRIGGER = 80; // 距视口边缘小于该值视为贴边

// 公开接口（api.unihalo.ialley.cn 分组，匿名可访问；app 端同源接口）
export const API_BASE = "/apis/api.unihalo.ialley.cn/v1alpha1/plugins/plugin-uni-halo";
export const CAPTCHA_URL = API_BASE + "/captcha/generate";
export const LINK_LIST_URL = API_BASE + "/mini-program-links";
export const LINK_SUBMIT_URL = API_BASE + "/mini-program-links/submissions";
export const CONFIGS_URL = API_BASE + "/getConfigs";

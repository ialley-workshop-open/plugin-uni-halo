// API 客户端分组（对齐 plugin-vote 的组织方式：按资源分组导出 client）

import { http } from "./request";
import type { AppInfo, AppVersion, ListQuery, PageResult } from "@/types";

const CONSOLE_API_GROUP = "console.api.unihalo.ialley.cn/v1alpha1";
const PUBLIC_API_GROUP = "api.unihalo.ialley.cn/v1alpha1";
const PLUGIN_BASE = `/apis/${CONSOLE_API_GROUP}/plugins/plugin-uni-halo`;
const PUBLIC_BASE = `/apis/${PUBLIC_API_GROUP}/plugins/plugin-uni-halo`;

export const appsApi = {
  list: (query: ListQuery = {}) => http.get<PageResult<AppInfo>>(`${PLUGIN_BASE}/apps`, query),
  create: (data: AppInfo) => http.post<AppInfo>(`${PLUGIN_BASE}/apps`, data),
  update: (name: string, data: AppInfo) => http.put<AppInfo>(`${PLUGIN_BASE}/apps/${name}`, data),
  delete: (name: string) => http.delete<{ success: boolean }>(`${PLUGIN_BASE}/apps/${name}`),
};

export const appVersionsApi = {
  list: (query: ListQuery = {}) =>
    http.get<PageResult<AppVersion>>(`${PLUGIN_BASE}/app-versions`, query),
  create: (data: AppVersion) => http.post<AppVersion>(`${PLUGIN_BASE}/app-versions`, data),
  update: (name: string, data: AppVersion) =>
    http.put<AppVersion>(`${PLUGIN_BASE}/app-versions/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${PLUGIN_BASE}/app-versions/${name}`),
};

// 公开接口（app 端/小程序端调用，匿名可访问）
export const publicApi = {
  getConfigs: () => http.get<Record<string, unknown>>(`${PUBLIC_BASE}/getConfigs`),
  getConfigsByGroupName: (groupName: string) =>
    http.get<unknown>(`${PUBLIC_BASE}/getConfigs/${groupName}`),
};

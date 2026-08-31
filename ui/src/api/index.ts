// API 客户端分组（对齐 plugin-vote 的组织方式：按资源分组导出 client）

import { http } from "./request";
import type {
  AppInfo,
  AppVersion,
  ListQuery,
  PageResult,
  LoveAlbum,
  LoveAlbumPhoto,
  LoveConfig,
  LoveDailyItem,
  LoveStory,
} from "@/types";

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

// ===== 恋爱管理 =====

export const loveConfigApi = {
  get: () => http.get<LoveConfig>(`${PLUGIN_BASE}/love-config`),
  save: (data: LoveConfig) => http.put<LoveConfig>(`${PLUGIN_BASE}/love-config`, data),
};

export interface LoveAlbumWriteRequest {
  album: LoveAlbum;
  /** 空 = 保持原密码；明文 = 重设 */
  password?: string;
  /** true = 清除密码 */
  passwordRemoved?: boolean;
}

export const loveAlbumsApi = {
  list: (query: { page?: number; size?: number; keyword?: string } = {}) =>
    http.get<PageResult<LoveAlbum>>(`${PLUGIN_BASE}/love-albums`, query),
  get: (name: string) => http.get<LoveAlbum>(`${PLUGIN_BASE}/love-albums/${name}`),
  create: (data: LoveAlbumWriteRequest) =>
    http.post<LoveAlbum>(`${PLUGIN_BASE}/love-albums`, data),
  update: (name: string, data: LoveAlbumWriteRequest) =>
    http.put<LoveAlbum>(`${PLUGIN_BASE}/love-albums/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${PLUGIN_BASE}/love-albums/${name}`),
  addPhoto: (name: string, photo: LoveAlbumPhoto) =>
    http.post<LoveAlbum>(`${PLUGIN_BASE}/love-albums/${name}/photos`, photo),
  updatePhotos: (name: string, photos: LoveAlbumPhoto[]) =>
    http.put<LoveAlbum>(`${PLUGIN_BASE}/love-albums/${name}/photos`, { photos }),
  deletePhoto: (name: string, photoName: string) =>
    http.delete<LoveAlbum>(`${PLUGIN_BASE}/love-albums/${name}/photos/${photoName}`),
};

export const loveDailyApi = {
  list: (query: { page?: number; size?: number; status?: string; keyword?: string } = {}) =>
    http.get<PageResult<LoveDailyItem>>(`${PLUGIN_BASE}/love-daily-items`, query),
  create: (data: LoveDailyItem) =>
    http.post<LoveDailyItem>(`${PLUGIN_BASE}/love-daily-items`, data),
  update: (name: string, data: LoveDailyItem) =>
    http.put<LoveDailyItem>(`${PLUGIN_BASE}/love-daily-items/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${PLUGIN_BASE}/love-daily-items/${name}`),
};

export const loveStoryApi = {
  list: (query: { page?: number; size?: number; keyword?: string } = {}) =>
    http.get<PageResult<LoveStory>>(`${PLUGIN_BASE}/love-stories`, query),
  create: (data: LoveStory) => http.post<LoveStory>(`${PLUGIN_BASE}/love-stories`, data),
  update: (name: string, data: LoveStory) =>
    http.put<LoveStory>(`${PLUGIN_BASE}/love-stories/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${PLUGIN_BASE}/love-stories/${name}`),
};

// 公开接口（app 端/小程序端调用，匿名可访问）
export const publicApi = {
  getConfigs: () => http.get<Record<string, unknown>>(`${PUBLIC_BASE}/getConfigs`),
  getConfigsByGroupName: (groupName: string) =>
    http.get<unknown>(`${PUBLIC_BASE}/getConfigs/${groupName}`),
};

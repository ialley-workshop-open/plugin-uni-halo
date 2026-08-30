// 领域类型统一管理（对齐 plugin-links 的组织方式：types/ 单独目录）

import type { Metadata } from "@halo-dev/api-client";

export interface AppPlatformInfo {
  name?: string;
  url?: string;
}

export interface AppInfoSpec {
  appid?: string;
  name?: string;
  description?: string;
  intro?: string;
  iconUrl?: string;
  screenshot?: string[];
  appAndroid?: AppPlatformInfo;
  appIos?: AppPlatformInfo;
  appHarmony?: AppPlatformInfo;
}

export interface AppInfo {
  metadata: Metadata;
  spec: AppInfoSpec;
}

export interface AppVersionSpec {
  appid?: string;
  name?: string;
  title?: string;
  contents?: string;
  platform?: string[];
  type?: "native_app" | "wgt";
  version?: string;
  versionCode?: number;
  isDeleted?: boolean;
  minUniVersion?: string;
  url?: string;
  stablePublish?: boolean;
  isSilently?: boolean;
  isMandatory?: boolean;
}

export interface AppVersion {
  metadata: Metadata;
  spec: AppVersionSpec;
}

export interface PageResult<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export interface ListQuery {
  page?: number;
  size?: number;
  keyword?: string;
  appid?: string;
  platform?: string;
  type?: string;
  stablePublish?: string;
}

export const TYPE_LABELS: Record<string, string> = {
  native_app: "整包",
  wgt: "wgt 资源包",
};

export const PLATFORMS = ["Android", "iOS", "Harmony"];

// ===== 恋爱管理 =====

export interface LoveInfo {
  boyNickname?: string;
  boyAvatar?: string;
  girlNickname?: string;
  girlAvatar?: string;
}

// 决策 D4：pageImages（图片配置）与模块开关（loveAlbum/loveDaily/ourStory 的
// enabled/iconUrl）不进入模型，继续由 setting.yaml 的 loveConfig 组配置

export interface LoveConfigSpec {
  loveDateTitle?: string;
  loveDate?: string;
  loveInfo?: LoveInfo;
}

export interface LoveConfig {
  metadata: Metadata;
  spec: LoveConfigSpec;
}

export interface LoveAlbumPhoto {
  name?: string;
  url?: string;
  /** 照片标题 */
  title?: string;
  description?: string;
  takenDate?: string;
  /** 拍摄地点（决策 D6） */
  location?: string;
  priority?: number;
}

export interface LoveAlbumSpec {
  displayName?: string;
  description?: string;
  cover?: string;
  passwordEnabled?: boolean;
  priority?: number;
  photos?: LoveAlbumPhoto[];
}

export interface LoveAlbumStatus {
  photoCount?: number;
}

export interface LoveAlbum {
  metadata: Metadata;
  spec: LoveAlbumSpec;
  status?: LoveAlbumStatus;
}

export interface LoveDailyItemSpec {
  title?: string;
  content?: string;
  status?: "wait" | "doing" | "complete";
  /** 计划时间（可选，yyyy-MM-dd） */
  planDate?: string;
  completeDate?: string;
  /** 完成感想（可选） */
  completeRemark?: string;
  /** 图片列表（多图，Halo 附件 URL 列表） */
  images?: string[];
  priority?: number;
}

export interface LoveDailyItem {
  metadata: Metadata;
  spec: LoveDailyItemSpec;
}

export const LOVE_STATUS_LABELS: Record<string, string> = {
  wait: "未开始",
  doing: "进行中",
  complete: "已完成",
};

/** 恋爱清单状态选项（统一供下拉/筛选使用，数组形式） */
export const LOVE_STATUS_OPTIONS: { label: string; value: string }[] = [
  { label: "未开始", value: "wait" },
  { label: "进行中", value: "doing" },
  { label: "已完成", value: "complete" },
];

/** 恋爱故事展示方式 */
export type LoveStoryViewMode = "list" | "timeline";

/** 恋爱故事展示方式选项 */
export const LOVE_STORY_VIEW_MODES: { label: string; value: LoveStoryViewMode }[] = [
  { label: "列表", value: "list" },
  { label: "时间轴", value: "timeline" },
];

/** 恋爱故事日期排序选项 */
export const LOVE_STORY_SORT_OPTIONS: { label: string; value: string }[] = [
  { label: "按日期 · 最新在前", value: "date_desc" },
  { label: "按日期 · 最早在前", value: "date_asc" },
];

/** 列表创建时间排序选项（恋爱相册/恋爱清单通用） */
export const LOVE_LIST_SORT_OPTIONS: { label: string; value: string }[] = [
  { label: "最新创建", value: "created_desc" },
  { label: "最早创建", value: "created_asc" },
];

/** 恋爱清单展示方式 */
export type LoveDailyViewMode = "list" | "timeline";

/** 恋爱清单展示方式选项 */
export const LOVE_DAILY_VIEW_MODES: { label: string; value: LoveDailyViewMode }[] = [
  { label: "列表", value: "list" },
  { label: "时间轴", value: "timeline" },
];

/** 恋爱清单时间轴排序选项 */
export const LOVE_DAILY_TIME_SORT_OPTIONS: { label: string; value: string }[] = [
  { label: "计划时间 · 最新在前", value: "plan_desc" },
  { label: "计划时间 · 最早在前", value: "plan_asc" },
  { label: "完成时间 · 最新在前", value: "complete_desc" },
  { label: "完成时间 · 最早在前", value: "complete_asc" },
];

export interface LoveStorySpec {
  title?: string;
  content?: string;
  date?: string;
  /** 故事地点 */
  location?: string;
  images?: string[];
  priority?: number;
}

export interface LoveStory {
  metadata: Metadata;
  spec: LoveStorySpec;
}

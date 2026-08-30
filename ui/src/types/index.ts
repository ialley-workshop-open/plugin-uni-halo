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

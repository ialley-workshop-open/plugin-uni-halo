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
  MiniProgramLink,
  MiniProgramLinkGroup,
  MiniProgramLinkGroupVo,
  GroupOption,
  MiniProgramLinkSubmission,
  Notice,
  NoticeType,
  AuditDataConfig,
  AuditDataConfigDetail,
  AuditDataCandidateResult,
  AuditCandidateType,
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

// ===== 公告管理 =====

export const noticeApi = {
  list: (query: {
    page?: number;
    size?: number;
    status?: string;
    type?: string;
    keyword?: string;
    sort?: string;
  } = {}) => http.get<PageResult<Notice>>(`${PLUGIN_BASE}/notices`, query),
  create: (data: Notice) => http.post<Notice>(`${PLUGIN_BASE}/notices`, data),
  update: (name: string, data: Notice) =>
    http.put<Notice>(`${PLUGIN_BASE}/notices/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${PLUGIN_BASE}/notices/${name}`),
};

export const noticeTypeApi = {
  list: (query: { page?: number; size?: number; keyword?: string } = {}) =>
    http.get<PageResult<NoticeType>>(`${PLUGIN_BASE}/notice-types`, query),
  create: (data: NoticeType) => http.post<NoticeType>(`${PLUGIN_BASE}/notice-types`, data),
  update: (name: string, data: NoticeType) =>
    http.put<NoticeType>(`${PLUGIN_BASE}/notice-types/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${PLUGIN_BASE}/notice-types/${name}`),
  /** 拖拽排序保存：names 为按新顺序排列的类型 name 列表 */
  sortOrder: (names: string[]) =>
    http.put<{ success: boolean }>(`${PLUGIN_BASE}/notice-types/order`, { names }),
};

// ===== 友情链接（小程序链接） =====

const MINI_PROGRAM_LINK_BASE = `${PLUGIN_BASE}/mini-program-links`;
const SUBMISSION_BASE = `${PLUGIN_BASE}/mini-program-link-submissions`;

export const miniProgramLinksApi = {
  list: (query: {
    page?: number;
    size?: number;
    group?: string;
    visible?: boolean;
    keyword?: string;
  } = {}) => http.get<PageResult<MiniProgramLink>>(MINI_PROGRAM_LINK_BASE, query),
  create: (data: MiniProgramLink) =>
    http.post<MiniProgramLink>(MINI_PROGRAM_LINK_BASE, data),
  update: (name: string, data: MiniProgramLink) =>
    http.put<MiniProgramLink>(`${MINI_PROGRAM_LINK_BASE}/${name}`, data),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${MINI_PROGRAM_LINK_BASE}/${name}`),
  /** 公开分组选项（/types，仅可见链接引用的分组，供筛选/分组标题映射） */
  listGroupsPublic: () =>
    http.get<GroupOption[]>(`${PUBLIC_BASE}/mini-program-links/types`),
  /** 公开分组视图（小程序端将来使用，grouped=true） */
  listGroupedPublic: (query: { keyword?: string } = {}) =>
    http.get<MiniProgramLinkGroupVo[]>(`${PUBLIC_BASE}/mini-program-links`, {
      grouped: true,
      ...query,
    }),
  /** 公开提交申请（匿名） */
  submitPublic: (data: MiniProgramLinkSubmission) =>
    http.post<MiniProgramLinkSubmission>(`${PUBLIC_BASE}/mini-program-links/submissions`, data),
};

/** 分组管理（控制台 CRUD，对标 plugin-links LinkGroup） */
export const miniProgramLinkGroupsApi = {
  list: (query: { page?: number; size?: number; keyword?: string } = {}) =>
    http.get<PageResult<MiniProgramLinkGroup>>(
      `${PLUGIN_BASE}/mini-program-link-groups`,
      query
    ),
  create: (data: MiniProgramLinkGroup) =>
    http.post<MiniProgramLinkGroup>(`${PLUGIN_BASE}/mini-program-link-groups`, data),
  update: (name: string, data: MiniProgramLinkGroup) =>
    http.put<MiniProgramLinkGroup>(
      `${PLUGIN_BASE}/mini-program-link-groups/${name}`,
      data
    ),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(
      `${PLUGIN_BASE}/mini-program-link-groups/${name}`
    ),
};

export const miniProgramLinkSubmissionsApi = {
  list: (query: {
    page?: number;
    size?: number;
    status?: string;
    keyword?: string;
    /** submittedAt（默认）/ reviewedAt / status */
    sort?: string;
  } = {}) => http.get<PageResult<MiniProgramLinkSubmission>>(SUBMISSION_BASE, query),
  /** 控制台新增申请（测试用，提交后进入待审核） */
  create: (data: MiniProgramLinkSubmission) =>
    http.post<MiniProgramLinkSubmission>(SUBMISSION_BASE, data),
  /** 审核通过；groupName 非空时调整分组（空字符串=未分组） */
  approve: (name: string, reason?: string, groupName?: string) =>
    http.post<MiniProgramLinkSubmission>(`${SUBMISSION_BASE}/${name}/approve`, {
      reason,
      groupName,
    }),
  /** 审核拒绝；groupName 非空时调整分组（空字符串=未分组） */
  reject: (name: string, reason: string, groupName?: string) =>
    http.post<MiniProgramLinkSubmission>(`${SUBMISSION_BASE}/${name}/reject`, {
      reason,
      groupName,
    }),
  delete: (name: string) =>
    http.delete<{ success: boolean }>(`${SUBMISSION_BASE}/${name}`),
};

// ===== 审核配置 =====

const AUDIT_DATA_BASE = `${PLUGIN_BASE}/audit-data`;

export const auditDataApi = {
  /** 读取配置详情（原始配置 + 各类型已选条目详情） */
  get: () => http.get<AuditDataConfigDetail>(AUDIT_DATA_BASE),
  /** 整体保存（服务端校验并剔除失效引用） */
  save: (data: AuditDataConfig) => http.put<AuditDataConfig>(AUDIT_DATA_BASE, data),
  /** 候选数据查询（选择器数据源） */
  candidates: (
    type: AuditCandidateType,
    query: { keyword?: string; page?: number; size?: number } = {}
  ) => http.get<AuditDataCandidateResult>(`${AUDIT_DATA_BASE}/candidates`, { type, ...query }),
  /** 公开读取（审核模式联动开关，供小程序端二期接入） */
  getPublic: () =>
    http.get<{ enabled: boolean; spec?: AuditDataConfig["spec"] }>(`${PUBLIC_BASE}/audit-data`),
};

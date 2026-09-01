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

// ===== 通知公告 =====

export type NoticeStatus = "draft" | "published" | "offline";

export interface NoticeSpec {
  title?: string;
  /** 富文本正文（编辑器输出的 HTML） */
  content?: string;
  /** 摘要（手填可选，为空时服务端自动从正文剥离生成） */
  summary?: string;
  /** 封面图（Halo 附件 URL） */
  cover?: string;
  /** 外链地址（可选） */
  link?: string;
  /** 公告类型（关联 NoticeType.metadata.name，可为空=不分类） */
  typeName?: string;
  status?: NoticeStatus;
  /** 排序，越大越靠前 */
  priority?: number;
  /** 发布时间（状态为已发布时服务端自动记录） */
  publishTime?: string;
}

export interface Notice {
  metadata: Metadata;
  spec: NoticeSpec;
}

export interface NoticeTypeSpec {
  /** 类型名称（必填），如「活动」「维护」 */
  displayName?: string;
  /** 标签颜色（hex，如 #10B981） */
  color?: string;
  /** 排序，越大越靠前 */
  priority?: number;
}

export interface NoticeType {
  metadata: Metadata;
  spec: NoticeTypeSpec;
}

export const NOTICE_STATUS_LABELS: Record<string, string> = {
  draft: "草稿",
  published: "已发布",
  offline: "已下线",
};

/** 公告状态选项（统一供下拉/筛选使用） */
export const NOTICE_STATUS_OPTIONS: { label: string; value: string }[] = [
  { label: "草稿", value: "draft" },
  { label: "已发布", value: "published" },
  { label: "已下线", value: "offline" },
];

/** 公告列表排序选项（右侧列表 header 使用） */
export const NOTICE_SORT_OPTIONS: { label: string; value: string }[] = [
  { label: "日期 · 最新在前", value: "date_desc" },
  { label: "日期 · 最早在前", value: "date_asc" },
  { label: "按类型分组", value: "type" },
];

// ===== 友情链接（小程序链接） =====

export interface MiniProgramLinkSpec {
  /** 小程序名称（必填） */
  displayName?: string;
  /** 太阳码（小程序码图片 URL，必填） */
  miniProgramCode?: string;
  /** 小程序地址（非必填） */
  link?: string;
  /** 作者昵称 */
  authorName?: string;
  /** 作者头像（图片 URL） */
  avatar?: string;
  /** 作者网站（归属作者信息，非必填） */
  website?: string;
  /** 分组（引用 MiniProgramLinkGroup 的 metadata.name；为空=未分组） */
  groupName?: string;
  /** 描述 */
  description?: string;
  /** 预览图（多图） */
  screenshots?: string[];
  /** 可见性：true 公开显示（默认）/ false 隐藏 */
  visible?: boolean;
  /** 来源（服务端按操作自动设置）：manual 手动添加 / submitted 自助申请 */
  source?: "manual" | "submitted";
  /** 排序，越大越靠前 */
  priority?: number;
}

/** 链接来源标签映射 */
export const LINK_SOURCE_LABELS: Record<string, string> = {
  manual: "手动",
  submitted: "申请",
};

/** 链接来源筛选选项（「全部」无 value=清除筛选） */
export const LINK_SOURCE_OPTIONS: { label: string; value?: string }[] = [
  { label: "全部" },
  { label: "手动", value: "manual" },
  { label: "申请", value: "submitted" },
];

export interface MiniProgramLink {
  metadata: Metadata;
  spec: MiniProgramLinkSpec;
}

export interface MiniProgramLinkGroupSpec {
  /** 分组名称（必填），如「工具」「生活」 */
  displayName?: string;
  /** 排序，越大越靠前 */
  priority?: number;
}

/** 分组模型（对标 plugin-links LinkGroup） */
export interface MiniProgramLinkGroup {
  metadata: Metadata;
  spec: MiniProgramLinkGroupSpec;
}

/** 分组选项（公开 /types 接口返回，供筛选/分组标题映射） */
export interface GroupOption {
  name: string;
  displayName: string;
}

/** 分组视图（公开接口 grouped=true 返回） */
export interface MiniProgramLinkGroupVo {
  /** 分组 name（空字符串=未分组） */
  groupName: string;
  /** 分组显示名（分组不存在或未分组时为空） */
  displayName: string;
  links: MiniProgramLink[];
}

export type SubmissionStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface MiniProgramLinkSubmissionSpec {
  displayName?: string;
  miniProgramCode?: string;
  link?: string;
  authorName?: string;
  avatar?: string;
  website?: string;
  /** 分组（引用 MiniProgramLinkGroup 的 metadata.name；为空=未分组） */
  groupName?: string;
  description?: string;
  /** 申请说明（小程序端提交时填写） */
  applyRemark?: string;
  screenshots?: string[];
  /** 申请人邮箱（非必填；填写则审核结果邮件通知） */
  email?: string;
  status?: SubmissionStatus;
  /** 审核结果说明（拒绝时必填） */
  reason?: string;
  /** 提交时间（服务端自动记录） */
  submittedAt?: string;
  /** 审核时间（服务端自动记录） */
  reviewedAt?: string;
  /** 审核通过后生成的链接 name */
  linkName?: string;
}

export interface MiniProgramLinkSubmission {
  metadata: Metadata;
  spec: MiniProgramLinkSubmissionSpec;
}

export const SUBMISSION_STATUS_LABELS: Record<string, string> = {
  PENDING: "待审核",
  APPROVED: "已通过",
  REJECTED: "已拒绝",
};

/** 申请状态选项（统一供下拉/筛选使用；「全部」无 value=清除筛选） */
export const SUBMISSION_STATUS_OPTIONS: { label: string; value?: string }[] = [
  { label: "全部" },
  { label: "待审核", value: "PENDING" },
  { label: "已通过", value: "APPROVED" },
  { label: "已拒绝", value: "REJECTED" },
];

// ===== 审核配置 =====

/** 被选中引用的快照（name 为扩展 metadata.name，其余字段按类型选择性填充） */
export interface AuditDataRef {
  name: string;
  title?: string;
  cover?: string;
  subTitle?: string;
  extra?: string;
}

/** 审核模式模拟数据的选中引用（对象快照存储，数组顺序即展示顺序） */
export interface AuditDataConfigSpec {
  /** 选中的文章 Post 引用列表 */
  posts?: AuditDataRef[];
  /** 选中的分类 Category 引用列表 */
  categories?: AuditDataRef[];
  /** 选中的图库分组 PhotoGroup 引用列表（未分组照片不展示） */
  galleryGroups?: AuditDataRef[];
  /** 选中的瞬间 Moment 引用列表 */
  moments?: AuditDataRef[];
  /** 选中的链接分组 LinkGroup 引用列表 */
  linkGroups?: AuditDataRef[];
  /** 备注（如「微信审核用模拟数据」） */
  description?: string;
}

export interface AuditDataConfig {
  metadata: Metadata;
  spec: AuditDataConfigSpec;
}

/** 审核配置详情（管理端 GET /audit-data 返回）：原始配置 + 各类型已选条目的最新详情 */
export interface AuditDataConfigDetail {
  config: AuditDataConfig;
  selections: Record<AuditCandidateType, AuditDataRef[]>;
}

export type AuditCandidateType =
  | "post"
  | "category"
  | "galleryGroup"
  | "moment"
  | "linkGroup";

/** 候选数据分页结果（条目即 AuditDataRef 快照，可直接用于已选列表渲染） */
export interface AuditDataCandidateResult {
  items: AuditDataRef[];
  page: number;
  size: number;
  total: number;
  /** 数据源插件未安装/扩展未注册（UI 提示「请先安装 XX 插件」） */
  pluginMissing?: boolean;
}

/** 候选类型中文名（UI 展示/提示用） */
export const AUDIT_CANDIDATE_TYPE_LABELS: Record<AuditCandidateType, string> = {
  post: "文章",
  category: "分类",
  galleryGroup: "图库分组",
  moment: "瞬间",
  linkGroup: "链接分组",
};

/** 候选类型对应依赖插件提示（pluginMissing 时展示） */
export const AUDIT_CANDIDATE_PLUGIN_HINTS: Partial<Record<AuditCandidateType, string>> = {
  galleryGroup: "请先安装 plugin-photos（图库插件）",
  moment: "请先安装 plugin-moments（瞬间插件）",
  linkGroup: "请先安装 plugin-links（链接管理插件）",
};

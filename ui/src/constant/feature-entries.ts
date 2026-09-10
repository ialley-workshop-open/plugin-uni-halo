import type { GeneralConfigQuickNavigationItem } from "@/types";

/**
 * 功能入口注册表
 */
export interface FeatureEntry extends GeneralConfigQuickNavigationItem {
  /** 归属：我的页-常用 / 我的页-其他 */
  group: "common" | "other";
}

/**
 * 统一功能入口注册表
 */
export const FEATURE_ENTRY_REGISTRY: FeatureEntry[] = [
  // ===== 我的页面-常用功能（默认 7 项，顺序即展示顺序）=====
  {key: "contact-blogger", title: "联系博主", subTitle: "博主常用联系方式", color: "#FF9800", bgColor: "#FF980024", iconPrefix: "uhemoji2-icon", icon: "-wink", path: "/pages-blog/contact/contact", visible: true, group: "common"},
  {key: "favorites", title: "我的收藏", color: "#FFB300", bgColor: "#FFB30024", iconPrefix: "uhemoji2-icon", icon: "-smiling", path: "/pages-blog/favorites/favorites", visible: true, group: "common"},
  {key: "love", title: "恋爱日记", subTitle: "博主的恋爱日记", color: "#FF4C67", bgColor: "#FF4C6724", iconPrefix: "uhemoji2-icon", icon: "-in-love", path: "/pages-blog/love/love", visible: true, group: "common"},
  {key: "friend-links", title: "友情链接", subTitle: "看看博主朋友们吧", color: "#009688", bgColor: "#00968824", iconPrefix: "uhemoji2-icon", icon: "-cool", path: "/pages-blog/friend-links/friend-links", visible: true, group: "common"},
  {key: "archives", title: "文章归档", subTitle: "全部文章", color: "#03A9F4", bgColor: "#03A9F424", iconPrefix: "uhemoji2-icon", icon: "-mask", path: "/pages-blog/archives/archives", visible: true, group: "common"},
  {key: "vote", title: "投票中心", subTitle: "查看和进行投票", color: "#00BCD4", bgColor: "#00BCD424", iconPrefix: "uhemoji2-icon", icon: "-confused", path: "/pages-blog/votes/votes", visible: true, group: "common"},
  {key: "data-visual", title: "数据看板", subTitle: "站点数据可视化", color: "#663CC9", bgColor: "#663CC924", iconPrefix: "uhemoji2-icon", icon: "-surprised", path: "/pages-blog/data-visual/data-visual", visible: true, group: "common"},
  // ===== 我的页面-其他功能（默认 3 项，顺序即展示顺序）=====
  {key: "setting", title: "偏好设置", subTitle: "首页布局、卡片样式等本地偏好", color: "#7986CB", bgColor: "#7986CB24", iconPrefix: "uhemoji2-icon", icon: "-tired", path: "/pages-blog/setting/setting", visible: true, group: "other"},
  {key: "disclaimers", title: "免责声明", subTitle: "博客内容免责声明", color: "#795548", bgColor: "#79554824", iconPrefix: "uhemoji2-icon", icon: "-smirking", path: "/pages-blog/disclaimers/disclaimers", visible: true, group: "other"},
  {key: "about", title: "关于项目", subTitle: "小莫唐尼开源项目", color: "#607D8B", bgColor: "#607D8B24", iconPrefix: "uhemoji2-icon", icon: "-happy-", path: "/pages-blog/about/about", visible: true, group: "other"},
];

/**
 * 首页快捷导航默认 5 项
 */
export const DEFAULT_QUICK_NAV_KEYS = ["archives", "vote", "disclaimers", "love", "contact-blogger"];

/** 我的页面-常用功能默认 7 项 */
export const DEFAULT_MY_PAGE_COMMON_KEYS = [
  "contact-blogger", "favorites", "love", "friend-links", "archives", "vote", "data-visual",
];

/** 我的页面-其他功能默认 3 项 key */
export const DEFAULT_MY_PAGE_OTHER_KEYS = ["setting", "disclaimers", "about"];

/** 按归属组过滤注册表（候选弹窗统一清单不用；默认配置一律走显式 key 列表） */
export function featureEntriesByGroup(group: FeatureEntry["group"]): FeatureEntry[] {
  return FEATURE_ENTRY_REGISTRY.filter((entry) => entry.group === group);
}

/** 按 key 列表从注册表取条目（保持 key 列表顺序；未知 key 跳过） */
export function featureEntriesByKeys(keys: string[]): FeatureEntry[] {
  return keys
    .map((key) => FEATURE_ENTRY_REGISTRY.find((entry) => entry.key === key))
    .filter((entry): entry is FeatureEntry => !!entry);
}

/** 注册表条目 → 快捷导航项快照（去掉 group，写入配置） */
export function toQuickNavigationItem(entry: FeatureEntry): GeneralConfigQuickNavigationItem {
  const {group: _group, ...item} = entry;
  return {...item};
}

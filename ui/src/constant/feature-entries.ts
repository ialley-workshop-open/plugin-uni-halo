import type { GeneralConfigQuickNavigationItem } from "@/types";

/**
 * 功能入口注册表（2026-09-10 新增，设计见 .docs/feature-entry-unified-design.md）。
 *
 * <p>统一功能入口候选数据源：首页快捷导航与我的页面（about）功能入口共用，
 * 前端静态定义、不做后端表/接口维护（与审核配置候选的后端跨插件查询范式不同）。</p>
 *
 * <p>候选清单 = 快捷导航默认数据去重即全部（不含 tabbar 页面，避免入口重复）；
 * 字段对齐快捷导航项（key/title/subTitle/color/bgColor/iconPrefix/icon/path/visible），
 * 仅多一个可空 subTitle（对标 app 端 rightText）。group 标注默认归属：</p>
 * <ul>
 *   <li>home：首页快捷导航可选项（默认 5 项即此组）；</li>
 *   <li>common：我的页面-常用功能（原「博客功能」改名）可选项；</li>
 *   <li>other：我的页面-其他功能可选项。</li>
 * </ul>
 * 同一入口可同时出现在首页快捷导航与我的页面（互不排斥）。
 */
export interface FeatureEntry extends GeneralConfigQuickNavigationItem {
  /** 归属：首页候选 / 我的页-常用 / 我的页-其他 */
  group: "home" | "common" | "other";
}

/** 统一功能入口注册表（不含 tabbar 页面；path 以 app 端实际页面清单为准，实施时核对） */
export const FEATURE_ENTRY_REGISTRY: FeatureEntry[] = [
  {key: "archives", title: "文章归档", subTitle: "全部文章", color: "#03A9F4", bgColor: "rgba(3, 169, 244, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-mask", path: "/pages-blog/archives/archives", visible: true, group: "home"},
  {key: "vote", title: "投票中心", color: "#00BCD4", bgColor: "rgba(0, 188, 212, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-confused", path: "/pages-blog/votes/votes", visible: true, group: "home"},
  {key: "disclaimers", title: "友情链接", color: "#009688", bgColor: "rgba(0, 150, 136, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-wink", path: "/pages-blog/friend-links/friend-links", visible: true, group: "home"},
  {key: "love", title: "恋爱日记", color: "#FF4C67", bgColor: "rgba(255, 76, 103, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-in-love", path: "/pages-blog/love/love", visible: true, group: "home"},
  {key: "contact-blogger", title: "联系博主", color: "#FF9800", bgColor: "rgba(255, 152, 0, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-cool", path: "/pages-blog/contact/contact", visible: true, group: "home"},
  {key: "about-system", title: "关于项目", color: "#FF9800", bgColor: "rgba(255, 152, 0, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-information", path: "", visible: true, group: "other"},
  {key: "articles", title: "文章列表", color: "#03A9F4", bgColor: "rgba(3, 169, 244, 0.14)", iconPrefix: "uhemoji2-icon", icon: "-book", path: "/pagesA/articles", visible: true, group: "other"},
];

/** 按归属组过滤注册表 */
export function featureEntriesByGroup(group: FeatureEntry["group"]): FeatureEntry[] {
  return FEATURE_ENTRY_REGISTRY.filter((entry) => entry.group === group);
}

/** 注册表条目 → 快捷导航项快照（去掉 group，写入配置） */
export function toQuickNavigationItem(entry: FeatureEntry): GeneralConfigQuickNavigationItem {
  const {group: _group, ...item} = entry;
  return {...item};
}

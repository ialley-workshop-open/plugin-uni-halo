/**
 * 悬浮卡片类型定义。
 */

/** 悬浮卡片配置（FloatingWindowHeadProcessor 内联注入 window.__UNI_HALO_FLOAT_MINI_PROFILE__） */
export interface FloatMiniProfileConfig {
  enabled: boolean;
  pageScope: "all" | "only" | "except";
  pagePatterns: string;
  position: string;
  offsetX: number;
  offsetY: number;
  name: string;
  nameSize: number;
  nameColor: string;
  description: string;
  descSize: number;
  descColor: string;
  imageUrl: string;
  imageSize: number;
  dragEnabled: boolean;
  closeEnabled: boolean;
  edgeHideEnabled: boolean;
  edgeHideDistance: number;
  rememberClosed: boolean;
  miniProgramApply: boolean;
}

/** 验证码接口响应：GET .../captcha/generate → { id, imageBase64 } */
export interface CaptchaResponse {
  id: string;
  imageBase64: string;
}

/** 公开链接分组 VO：GET .../mini-program-links?grouped=true */
export interface MiniProgramLinkGroup {
  groupName: string;
  displayName: string;
  links: MiniProgramLinkItem[];
}

/** 公开链接条目（仅取展示所需字段） */
export interface MiniProgramLinkItem {
  spec?: {
    displayName?: string;
    link?: string;
    description?: string;
    authorName?: string;
  };
}

/** 小程序信息（getConfigs → pluginConfig.linkInfo.miniInfo，app 端「申请信息」同源） */
export interface MiniInfo {
  displayName?: string;
  miniProgramCode?: string;
  link?: string;
  description?: string;
  applyRemark?: string;
}

/** 博主信息（getConfigs → authorConfig.blogger，应用设置-博主资料） */
export interface BloggerInfo {
  nickname?: string;
  avatar?: string;
  website?: string;
  description?: string;
}

declare global {
  interface Window {
    __UNI_HALO_FLOAT_MINI_PROFILE__?: FloatMiniProfileConfig;
  }
}

/**
 * UniHalo 主题悬浮卡片（小程序太阳码）Lit 组件。
 *
 * 使用 shadow DOM 自定义元素：样式完全内聚（外部主题样式无法穿透，组件
 * 样式也不会泄漏）；弹窗（申请表单 / 友链信息）渲染在 shadow DOM 内部，
 * 样式选择器直接用类名（修复此前「弹窗内按钮无样式」问题）。
 *
 * 数据源：
 *  - 申请提交：POST .../mini-program-links/submissions（验证码 query 参数，403 自动刷新）
 *  - 友链信息：GET .../getConfigs → pluginConfig.linkInfo.miniInfo（小程序信息）
 *    + authorConfig.blogger（博主信息）
 */
import { LitElement, html } from "lit";
import {
  CAPTCHA_URL,
  CONFIG,
  CONFIGS_URL,
  EDGE_TRIGGER,
  LINK_SUBMIT_URL,
  STORAGE_KEY,
} from "./config";
import { matchPage } from "./page-match";
import { styles } from "./styles";
import type {
  BloggerInfo,
  CaptchaResponse,
  FloatMiniProfileConfig,
  MiniInfo,
} from "./types";

interface ApplyField {
  key: string;
  label: string;
  required: boolean;
  type?: "text" | "textarea" | "select";
  placeholder?: string;
}

/** 友链信息弹窗展示行（长文本用 textarea；copyable=false 不渲染复制按钮） */
interface LinkInfoRow {
  label: string;
  value?: string;
  textarea?: boolean;
  copyable?: boolean;
}

/** 最小化小图拖拽中间态 */
interface MiniDotDrag {
  startX: number;
  startY: number;
  left: number;
  top: number;
  moved: boolean;
}

/** 申请表单-基础信息字段（对齐管理端「链接管理-申请审核」新增申请表单） */
const BASIC_FIELDS: ApplyField[] = [
  { key: "displayName", label: "应用名称", required: true },
  { key: "miniProgramCode", label: "太阳码（图片地址）", required: true },
  { key: "link", label: "应用地址", required: false, placeholder: "#小程序://xxx" },
  { key: "description", label: "应用描述", required: false, type: "textarea" },
  { key: "applyRemark", label: "申请说明", required: false, type: "textarea" },
];

/** 申请表单-作者信息字段 */
const AUTHOR_FIELDS: ApplyField[] = [
  { key: "avatar", label: "作者头像（图片地址）", required: false },
  { key: "authorName", label: "作者昵称", required: false },
  { key: "website", label: "作者网站", required: false },
  { key: "email", label: "邮箱（选填，用于审核结果通知）", required: false },
];

interface DragState {
  startX: number;
  startY: number;
  left: number;
  top: number;
}

/** 申请表单草稿缓存 key：输入自动保存，提交成功后清空（验证码不缓存） */
const DRAFT_KEY = "uh-fmp-apply-draft";

/**
 * 图片地址规范化：http(s):// 或 // 协议相对或 data: 原样返回；
 * 其余相对/裸路径拼接当前站点域名（Halo 控制台附件/插件静态资源）。
 */
function normalizeImageUrl(url?: string): string {
  if (!url) {
    return "";
  }
  if (/^(https?:)?\/\//.test(url) || /^data:/i.test(url)) {
    return url;
  }
  return window.location.origin + (url.startsWith("/") ? url : "/" + url);
}

export class FloatMiniProfileElement extends LitElement {
  static styles = [styles];

  static properties = {
    applyOpen: { state: true },
    linksOpen: { state: true },
    applySubmitting: { state: true },
    captchaId: { state: true },
    captchaSrc: { state: true },
    linksLoading: { state: true },
    linksError: { state: true },
    miniInfo: { state: true },
    blogger: { state: true },
    groupOptions: { state: true },
    applyTab: { state: true },
    screenshotRows: { state: true },
    minimized: { state: true },
    edgeTrigger: { state: true },
    edgeSide: { state: true },
    edgeTriggerStyle: { state: true },
    miniDotStyle: { state: true },
  };

  declare applyOpen: boolean;
  declare linksOpen: boolean;
  declare applySubmitting: boolean;
  declare captchaId: string;
  declare captchaSrc: string;
  declare linksLoading: boolean;
  declare linksError: boolean;
  declare miniInfo: MiniInfo | null;
  declare blogger: BloggerInfo | null;
  declare groupOptions: Array<{ value: string; label: string }>;
  declare applyTab: "basic" | "author";
  declare screenshotRows: string[];
  declare minimized: boolean;
  declare edgeTrigger: boolean;
  declare edgeSide: string;
  declare edgeTriggerStyle: string;
  declare miniDotStyle: string;

  private config: FloatMiniProfileConfig;
  private dragState: DragState | null = null;
  private miniDotDrag: MiniDotDrag | null = null;
  private miniDotDragged = false;

  constructor() {
    super();
    // 入口（index.ts）已校验 CONFIG 非空
    this.config = CONFIG as FloatMiniProfileConfig;
    this.applyOpen = false;
    this.linksOpen = false;
    this.applySubmitting = false;
    this.captchaId = "";
    this.captchaSrc = "";
    this.linksLoading = false;
    this.linksError = false;
    this.miniInfo = null;
    this.blogger = null;
    this.groupOptions = [];
    this.applyTab = "basic";
    this.screenshotRows = [""];
    this.minimized = false;
    this.edgeTrigger = false;
    this.edgeSide = "";
    this.edgeTriggerStyle = "";
    this.miniDotStyle = "";
  }

  connectedCallback(): void {
    super.connectedCallback();
    // 页面范围不匹配或访客已关闭 → 零残留移除
    if (!matchPage() || this.isClosed()) {
      this.remove();
    }
  }

  protected firstUpdated(): void {
    this.applyPosition();
  }

  private isClosed(): boolean {
    if (this.config.rememberClosed === false) {
      return false;
    }
    try {
      return localStorage.getItem(STORAGE_KEY) === "1";
    } catch {
      return false;
    }
  }

  private get cardEl(): HTMLElement | null {
    return this.renderRoot.querySelector(".uh-fmp");
  }

  // ===== 定位：9 向锚点 + 偏移 =====
  private applyPosition(): void {
    const card = this.cardEl;
    if (!card) {
      return;
    }
    const c = this.config;
    const pos = c.position || "bottom-right";
    card.classList.add("uh-fmp-pos-" + pos);

    const x = Number(c.offsetX) || 0;
    const y = Number(c.offsetY) || 0;
    const tx =
      pos === "center" || pos === "top-center" || pos === "bottom-center"
        ? "calc(-50% + " + x + "px)"
        : x + "px";
    const ty =
      pos === "center" || pos === "left-center" || pos === "right-center"
        ? "calc(-50% + " + y + "px)"
        : y + "px";
    card.style.transform = "translate(" + tx + ", " + ty + ")";
  }

  // ===== 拖拽（Pointer Events）+ 贴边隐藏 =====
  private onPointerDown(e: PointerEvent): void {
    const target = e.target as HTMLElement | null;
    // 操作按钮（关闭/最小化/申请/友链/恢复/贴边把手）不触发拖拽：
    // 否则 setPointerCapture + preventDefault 会干扰 click 合成事件
    if (
      target &&
      (target.closest(".uh-fmp-close") ||
        target.closest(".uh-fmp-minimize") ||
        target.closest(".uh-fmp-actions") ||
        target.closest(".uh-fmp-overlay") ||
        target.closest(".uh-fmp-mini-dot") ||
        target.closest(".uh-fmp-edge-trigger"))
    ) {
      return;
    }
    const card = this.cardEl;
    if (!card) {
      return;
    }
    const rect = card.getBoundingClientRect();
    this.dragState = {
      startX: e.clientX,
      startY: e.clientY,
      left: rect.left,
      top: rect.top,
    };
    this.restoreFromEdge(); // 拖拽开始时清除贴边状态与把手
    card.classList.add("uh-fmp-dragging");
    card.style.touchAction = "none";
    card.setPointerCapture(e.pointerId);
    e.preventDefault();
  }

  private onPointerMove(e: PointerEvent): void {
    const dragState = this.dragState;
    const card = this.cardEl;
    if (!dragState || !card) {
      return;
    }
    let left = dragState.left + (e.clientX - dragState.startX);
    let top = dragState.top + (e.clientY - dragState.startY);
    left = Math.max(0, Math.min(left, window.innerWidth - card.offsetWidth));
    top = Math.max(0, Math.min(top, window.innerHeight - card.offsetHeight));
    this.setFree(left, top);
  }

  private onPointerEnd(): void {
    const card = this.cardEl;
    if (!this.dragState || !card) {
      return;
    }
    this.dragState = null;
    card.classList.remove("uh-fmp-dragging");
    card.style.touchAction = "";
    this.maybeEdgeHide();
  }

  private setFree(left: number, top: number): void {
    const card = this.cardEl;
    if (!card) {
      return;
    }
    card.style.left = left + "px";
    card.style.top = top + "px";
    card.style.right = "auto";
    card.style.bottom = "auto";
    card.style.transform = "translate(0, 0)";
    card.classList.remove("uh-fmp-edge");
  }

  private maybeEdgeHide(): void {
    if (!this.config.edgeHideEnabled) {
      return;
    }
    const card = this.cardEl;
    if (!card) {
      return;
    }
    const rect = card.getBoundingClientRect();
    const distances: Record<string, number> = {
      left: rect.left,
      right: window.innerWidth - rect.right,
      top: rect.top,
      bottom: window.innerHeight - rect.bottom,
    };
    let side: string | null = null;
    // 触发阈值：配置 edgeHideDistance（0 视为未设置，兜底最小有效值 4px 防贴边失效；未配置回退 80）
    const rawDistance = this.config.edgeHideDistance;
    const configured =
      rawDistance === undefined || rawDistance === null
        ? NaN
        : rawDistance === 0
          ? 4
          : Number(rawDistance);
    let min = Number.isFinite(configured) ? configured : EDGE_TRIGGER;
    (["left", "right", "top", "bottom"] as const).forEach((key) => {
      if (distances[key] < min) {
        min = distances[key];
        side = key;
      }
    });
    if (side) {
      // 完全隐藏：按当前视口位置精确位移（覆盖锚点 top/left/offset 残留，不留任何可见部分）
      if (side === "left") {
        card.style.transform = `translateX(${-(rect.left + rect.width)}px)`;
      } else if (side === "right") {
        card.style.transform = `translateX(${window.innerWidth - rect.left}px)`;
      } else if (side === "top") {
        card.style.transform = `translateY(${-(rect.top + rect.height)}px)`;
      } else {
        card.style.transform = `translateY(${window.innerHeight - rect.top}px)`;
      }
      card.classList.add("uh-fmp-edge", "uh-fmp-edge-" + side);
      // 边缘触发把手：fixed 定位在对应视口边缘中点
      this.edgeSide = side;
      this.edgeTriggerStyle = this.buildEdgeTriggerStyle(side, rect);
      this.edgeTrigger = true;
    }
  }

  /** 触发把手内联定位（贴左/右 → 竖把手，贴顶/底 → 横把手，对齐边缘中点） */
  private buildEdgeTriggerStyle(side: string, rect: DOMRect): string {
    const cx = rect.left + rect.width / 2;
    const cy = rect.top + rect.height / 2;
    const gap = 2;
    // 35px 长度的一半（17.5），把手中点对齐卡片边缘中点
    if (side === "left") {
      return `left:${gap}px;top:${Math.round(cy - 17.5)}px;`;
    }
    if (side === "right") {
      return `right:${gap}px;top:${Math.round(cy - 17.5)}px;`;
    }
    if (side === "top") {
      return `top:${gap}px;left:${Math.round(cx - 17.5)}px;`;
    }
    return `bottom:${gap}px;left:${Math.round(cx - 17.5)}px;`;
  }

  private onEdgeTriggerEnter(): void {
    const card = this.cardEl;
    if (card) {
      card.classList.add("uh-fmp-edge-hover");
    }
  }

  private onEdgeTriggerLeave(): void {
    const card = this.cardEl;
    if (card) {
      card.classList.remove("uh-fmp-edge-hover");
    }
  }

  /** 点击把手：完全恢复卡片（清贴边类与把手） */
  private restoreFromEdge(): void {
    const card = this.cardEl;
    this.edgeTrigger = false;
    this.edgeSide = "";
    this.edgeTriggerStyle = "";
    if (card) {
      card.style.transform = ""; // 清除贴边内联位移，回到锚点/拖拽位置
      card.classList.remove(
        "uh-fmp-edge",
        "uh-fmp-edge-hover",
        "uh-fmp-edge-left",
        "uh-fmp-edge-right",
        "uh-fmp-edge-top",
        "uh-fmp-edge-bottom",
      );
    }
  }

  // ===== 最小化 =====
  private onMinimizeClick(): void {
    const card = this.cardEl;
    if (!card) {
      return;
    }
    // 记录卡片当前视口位置：小图作为独立 fixed 元素定位到该处（不影响卡片样式）
    const rect = card.getBoundingClientRect();
    this.miniDotStyle = `left:${Math.round(rect.left)}px;top:${Math.round(rect.top)}px;`;
    this.minimized = true;
    this.edgeTrigger = false; // 最小化后隐藏贴边触发把手
    // 最小化后不再贴边（小图保持原位）
    card.style.transform = ""; // 清除贴边内联位移，恢复后卡片正常显示
    card.classList.remove(
      "uh-fmp-edge",
      "uh-fmp-edge-left",
      "uh-fmp-edge-right",
      "uh-fmp-edge-top",
      "uh-fmp-edge-bottom",
    );
  }

  private onRestoreClick(): void {
    if (this.miniDotDragged) {
      this.miniDotDragged = false;
      return; // 刚拖拽过，忽略本次 click（防拖拽松手误触发恢复）
    }
    this.minimized = false;
  }

  // ===== 最小化小图拖拽（独立 fixed 元素，不受 dragEnabled 约束，无条件可拖） =====
  private onMiniDotPointerDown(e: PointerEvent): void {
    const dot = e.currentTarget as HTMLElement;
    const rect = dot.getBoundingClientRect();
    this.miniDotDrag = {
      startX: e.clientX,
      startY: e.clientY,
      left: rect.left,
      top: rect.top,
      moved: false,
    };
    dot.setPointerCapture(e.pointerId);
    // 不 preventDefault：未移动时保留 click 恢复；移动后由 miniDotDragged 拦截恢复
  }

  private onMiniDotPointerMove(e: PointerEvent): void {
    const drag = this.miniDotDrag;
    if (!drag) {
      return;
    }
    const dx = e.clientX - drag.startX;
    const dy = e.clientY - drag.startY;
    if (!drag.moved && (Math.abs(dx) > 3 || Math.abs(dy) > 3)) {
      drag.moved = true; // 超过阈值视为拖拽
    }
    if (!drag.moved) {
      return;
    }
    const dot = e.currentTarget as HTMLElement;
    const left = Math.max(0, Math.min(drag.left + dx, window.innerWidth - dot.offsetWidth));
    const top = Math.max(0, Math.min(drag.top + dy, window.innerHeight - dot.offsetHeight));
    // 直接写内联样式，避免 state 重渲染干扰指针捕获
    dot.style.left = left + "px";
    dot.style.top = top + "px";
  }

  private onMiniDotPointerEnd(): void {
    const drag = this.miniDotDrag;
    if (drag?.moved) {
      this.miniDotDragged = true; // click 处理器据此忽略本次恢复
    }
    this.miniDotDrag = null;
  }

  // ===== 关闭 =====
  private onCloseClick(): void {
    if (this.config.rememberClosed !== false) {
      try {
        localStorage.setItem(STORAGE_KEY, "1");
      } catch {
        // localStorage 不可用时仅本次会话关闭
      }
    }
    const card = this.cardEl;
    if (card) {
      card.classList.add("uh-fmp-closing");
      setTimeout(() => this.remove(), 200);
    } else {
      this.remove();
    }
  }

  // ===== 弹窗通用 =====
  private onOverlayClick(e: Event): void {
    const target = e.target as HTMLElement;
    if (target.classList.contains("uh-fmp-overlay")) {
      this.closeModals();
    }
  }

  private closeModals(): void {
    this.applyOpen = false;
    this.linksOpen = false;
  }

  // ===== 申请弹窗 =====
  private openApply(): void {
    this.applyOpen = true;
    this.refreshCaptcha();
    // 弹窗渲染完成后回填本地草稿（验证码不缓存）
    this.updateComplete.then(() => this.restoreDraft());
  }

  private restoreDraft(): void {
    const form = this.renderRoot.querySelector(".uh-fmp-form") as HTMLFormElement | null;
    if (!form) {
      return;
    }
    const draft = this.loadDraft();
    [...BASIC_FIELDS, ...AUTHOR_FIELDS].forEach((field) => {
      const el = form.elements.namedItem(field.key) as HTMLInputElement | null;
      if (el && draft[field.key]) {
        el.value = String(draft[field.key]);
      }
    });
    // 预览图动态行回填（兼容旧草稿：字符串按行拆分）
    const shots = draft["screenshots"];
    if (Array.isArray(shots)) {
      this.screenshotRows = shots.length ? shots.slice() : [""];
    } else if (typeof shots === "string" && shots.trim()) {
      this.screenshotRows = shots
        .split("\n")
        .map((s) => s.trim())
        .filter(Boolean);
      if (!this.screenshotRows.length) {
        this.screenshotRows = [""];
      }
    }
  }

  private loadDraft(): Record<string, unknown> {
    try {
      const raw = localStorage.getItem(DRAFT_KEY);
      return raw ? (JSON.parse(raw) as Record<string, unknown>) : {};
    } catch {
      return {};
    }
  }

  /** 输入即自动保存草稿：刷新/关闭页面后重新打开仍显示已填内容 */
  private saveDraft(): void {
    const form = this.renderRoot.querySelector(".uh-fmp-form") as HTMLFormElement | null;
    if (!form) {
      return;
    }
    const values: Record<string, unknown> = {};
    [...BASIC_FIELDS, ...AUTHOR_FIELDS].forEach((field) => {
      const el = form.elements.namedItem(field.key) as HTMLInputElement | null;
      values[field.key] = el?.value || "";
    });
    // 预览图动态行存数组草稿
    values["screenshots"] = this.screenshotRows.filter((s) => s.trim());
    try {
      localStorage.setItem(DRAFT_KEY, JSON.stringify(values));
    } catch {
      // localStorage 不可用时仅本次会话
    }
  }

  private clearDraft(): void {
    try {
      localStorage.removeItem(DRAFT_KEY);
    } catch {
      // 忽略
    }
  }

  /** 重置：清空表单与本地草稿 */
  private resetApply(): void {
    const form = this.renderRoot.querySelector(".uh-fmp-form") as HTMLFormElement | null;
    if (form) {
      form.reset();
    }
    this.screenshotRows = [""]; // 预览图动态行重置为一行空
    this.applyTab = "basic"; // 回到基础信息面板
    this.clearDraft();
  }

  private onFormInput(): void {
    this.saveDraft();
  }

  private refreshCaptcha(): void {
    fetch(CAPTCHA_URL)
      .then((res) => res.json())
      .then((data: CaptchaResponse) => {
        if (data && data.imageBase64) {
          this.setCaptcha(data);
        }
      })
      .catch(() => {
        // 验证码加载失败静默处理
      });
  }

  private setCaptcha(captcha: CaptchaResponse): void {
    this.captchaId = captcha.id;
    // 后端 CaptchaVo.imageBase64 已是完整 data URI（data:image/png;base64,...），无需再拼前缀
    this.captchaSrc = captcha.imageBase64;
  }

  private async onApplySubmit(e: SubmitEvent): Promise<void> {
    e.preventDefault();
    const form = e.target as HTMLFormElement;
    // 手动校验必填字段（表单 novalidate：原生校验对隐藏面板的 required 不生效）
    const requiredChecks: Array<{ key: string; message: string; panel?: "basic" | "author" }> = [
      { key: "displayName", message: "请填写小程序名称", panel: "basic" },
      { key: "miniProgramCode", message: "请填写太阳码图片地址", panel: "basic" },
      { key: "captchaCode", message: "请输入验证码" },
    ];
    for (const check of requiredChecks) {
      const el = form.elements.namedItem(check.key) as HTMLInputElement | null;
      if (!el || !el.value.trim()) {
        if (check.panel) {
          this.applyTab = check.panel;
        }
        alert(check.message);
        return;
      }
    }
    const spec: Record<string, unknown> = {};
    [...BASIC_FIELDS, ...AUTHOR_FIELDS].forEach((field) => {
      const el = form.elements.namedItem(field.key) as HTMLInputElement | null;
      if (el && el.value.trim()) {
        spec[field.key] = el.value.trim();
      }
    });
    // 预览图动态行 → 数组（对齐 MiniProgramLinkSubmissionSpec.screenshots；URL 规范化防控制台裂图）
    const shots = this.screenshotRows
      .map((s) => s.trim())
      .filter(Boolean)
      .map(normalizeImageUrl);
    if (shots.length) {
      spec["screenshots"] = shots;
    }
    const captchaCodeEl = form.elements.namedItem("captchaCode") as HTMLInputElement | null;
    const captchaCode = (captchaCodeEl?.value || "").trim();
    const query = "?captchaId=" + encodeURIComponent(this.captchaId || "")
      + "&captchaCode=" + encodeURIComponent(captchaCode);

    this.applySubmitting = true;
    try {
      const res = await fetch(LINK_SUBMIT_URL + query, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ spec }),
      });
      const data = (await res.json().catch(() => ({}))) as {
        message?: string;
        captcha?: CaptchaResponse;
      };
      if (res.status === 200 || res.status === 201) {
        this.applyOpen = false;
        this.clearDraft(); // 提交成功后清空本地草稿
        form.reset(); // 清空表单字段（含验证码，下次打开为空）
        this.screenshotRows = [""]; // 预览图动态行重置
        this.applyTab = "basic"; // 回到基础信息面板
        alert("申请提交成功，请等待审核");
        return;
      }
      // 403：验证码错误/过期，附新验证码即时刷新
      if (res.status === 403 && data.captcha) {
        this.setCaptcha(data.captcha);
      }
      alert(data.message || "提交失败，请重试");
    } catch {
      alert("网络异常，请稍后重试");
    } finally {
      this.applySubmitting = false;
    }
  }

  // ===== 友链信息弹窗（小程序信息 + 博主信息） =====
  private openLinks(): void {
    this.linksOpen = true;
    this.linksLoading = true;
    this.linksError = false;
    this.miniInfo = null;
    this.blogger = null;
    fetch(CONFIGS_URL)
      .then((res) => res.json())
      .then((data: Record<string, unknown>) => {
        const pluginConfig = data?.pluginConfig as
          | { linkInfo?: { miniInfo?: MiniInfo } }
          | undefined;
        const authorConfig = data?.authorConfig as { blogger?: BloggerInfo } | undefined;
        this.miniInfo = pluginConfig?.linkInfo?.miniInfo || null;
        this.blogger = authorConfig?.blogger || null;
      })
      .catch(() => {
        this.linksError = true;
      })
      .finally(() => {
        this.linksLoading = false;
      });
  }

  // ===== 渲染 =====
  override render() {
    const c = this.config;
    const size = Number(c.imageSize) || 100;
    return html`
      ${this.applyOpen ? this.renderApplyModal() : ""}
      ${this.linksOpen ? this.renderLinksModal() : ""}
      <div
        class="uh-fmp ${this.minimized ? "uh-fmp-minimized" : ""}"
        style="width:${size}px"
        @pointerdown=${this.onPointerDown}
        @pointermove=${this.onPointerMove}
        @pointerup=${this.onPointerEnd}
        @pointercancel=${this.onPointerEnd}
        @mouseleave=${this.onEdgeTriggerLeave}
      >
        <div class="uh-fmp-main" ?hidden=${this.minimized}>
          <div class="uh-fmp-topbar">
            <button type="button" class="uh-fmp-minimize" aria-label="最小化" @click=${this.onMinimizeClick}>&minus;</button>
            ${c.closeEnabled !== false
              ? html`<button type="button" class="uh-fmp-close" aria-label="关闭悬浮窗" @click=${this.onCloseClick}>&times;</button>`
              : ""}
          </div>
          ${c.imageUrl
            ? html`<img class="uh-fmp-img" src=${normalizeImageUrl(c.imageUrl)} alt=${c.name || "小程序太阳码"} />`
            : ""}
          ${c.name
            ? html`<div class="uh-fmp-name" style="font-size:${Number(c.nameSize) || 14}px;color:${c.nameColor || "#333333"}">${c.name}</div>`
            : ""}
          ${c.description
            ? html`<div class="uh-fmp-desc" style="font-size:${Number(c.descSize) || 12}px;color:${c.descColor || "#999999"}">${c.description}</div>`
            : ""}
          ${c.miniProgramApply
            ? html`
                <div class="uh-fmp-actions">
                  <button type="button" class="uh-fmp-btn" @click=${this.openApply}>我要申请</button>
                  <button type="button" class="uh-fmp-btn" @click=${this.openLinks}>友链信息</button>
                  <div class="uh-fmp-hint">小程序申请和友链信息</div>
                </div>`
            : ""}
        </div>
      </div>
      ${this.minimized
        ? html`
            <button
              type="button"
              class="uh-fmp-mini-dot"
              style=${this.miniDotStyle}
              @click=${this.onRestoreClick}
              @pointerdown=${this.onMiniDotPointerDown}
              @pointermove=${this.onMiniDotPointerMove}
              @pointerup=${this.onMiniDotPointerEnd}
              @pointercancel=${this.onMiniDotPointerEnd}
              aria-label="恢复悬浮窗"
            >
              ${c.imageUrl ? html`<img src=${normalizeImageUrl(c.imageUrl)} alt="" />` : ""}
              <span class="uh-fmp-mini-plus">+</span>
            </button>`
        : ""}
      ${this.edgeTrigger
        ? html`
            <button
              type="button"
              class="uh-fmp-edge-trigger uh-fmp-edge-trigger-${this.edgeSide}"
              style=${this.edgeTriggerStyle}
              @mouseenter=${this.onEdgeTriggerEnter}
              @mouseleave=${this.onEdgeTriggerLeave}
              @click=${this.restoreFromEdge}
              aria-label="展开悬浮窗"
            ></button>`
        : ""}
    `;
  }

  /** 申请表单字段渲染：text（默认）/ textarea / select（分组下拉） */
  private renderApplyField(field: ApplyField) {
    const labelEl = html`<span>${field.label}${field.required ? " *" : ""}</span>`;
    if (field.type === "select") {
      return html`
        <label class="uh-fmp-field">
          ${labelEl}
          <select name=${field.key} class="uh-fmp-select">
            <option value="">未分组</option>
            ${this.groupOptions.map(
              (opt) => html`<option value=${opt.value}>${opt.label}</option>`,
            )}
          </select>
        </label>`;
    }
    if (field.type === "textarea") {
      return html`
        <label class="uh-fmp-field">
          ${labelEl}
          <textarea name=${field.key} rows="2" class="uh-fmp-textarea" placeholder=${field.placeholder ?? ""}></textarea>
        </label>`;
    }
    return html`
      <label class="uh-fmp-field">
        ${labelEl}
        <input type="text" name=${field.key} ?required=${field.required} placeholder=${field.placeholder ?? ""} />
      </label>`;
  }

  private renderApplyModal() {
    return html`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal uh-fmp-modal-apply">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">小程序申请</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-segmented">
            <button
              type="button"
              class="uh-fmp-seg-item ${this.applyTab === "basic" ? "uh-fmp-seg-active" : ""}"
              @click=${() => (this.applyTab = "basic")}
            >基础信息</button>
            <button
              type="button"
              class="uh-fmp-seg-item ${this.applyTab === "author" ? "uh-fmp-seg-active" : ""}"
              @click=${() => (this.applyTab = "author")}
            >作者信息</button>
          </div>
          <div class="uh-fmp-modal-body uh-fmp-apply-body">
            <!-- novalidate：隐藏面板的 required 不参与原生校验，由提交时手动校验 -->
            <form class="uh-fmp-form" novalidate @submit=${this.onApplySubmit} @input=${this.onFormInput}>
              <div class="uh-fmp-apply-panels">
                <div class="uh-fmp-apply-panel" ?hidden=${this.applyTab !== "basic"}>
                  ${BASIC_FIELDS.map((field) => this.renderApplyField(field))}
                  ${this.renderScreenshotRows()}
                </div>
                <div class="uh-fmp-apply-panel" ?hidden=${this.applyTab !== "author"}>
                  ${AUTHOR_FIELDS.map((field) => this.renderApplyField(field))}
                </div>
              </div>
              <div class="uh-fmp-apply-footer">
                <label class="uh-fmp-field uh-fmp-captcha-row">
                  <span>验证码 *</span>
                  <span class="uh-fmp-captcha-input">
                    <input type="text" name="captchaCode" required autocomplete="off" />
                    <img
                      class="uh-fmp-captcha-img"
                      alt="验证码"
                      title="看不清？点击刷新"
                      src=${this.captchaSrc}
                      @click=${this.refreshCaptcha}
                    />
                  </span>
                </label>
                <div class="uh-fmp-form-actions">
                  <button type="button" class="uh-fmp-btn" @click=${this.resetApply}>重置</button>
                  <button type="button" class="uh-fmp-btn" @click=${this.closeModals}>取消</button>
                  <button type="submit" class="uh-fmp-btn uh-fmp-btn-primary" ?disabled=${this.applySubmitting}>
                    ${this.applySubmitting ? "提交中…" : "提交申请"}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    `;
  }

  /** 预览图动态添加：URL 输入行 + 删除按钮 + 底部「添加一张」 */
  private renderScreenshotRows() {
    return html`
      <div class="uh-fmp-field">
        <span>预览截图(可选)</span>
        ${this.screenshotRows.map(
          (url, index) => html`
            <div class="uh-fmp-shot-row">
              <input
                class="uh-fmp-shot-input"
                type="text"
                name="screenshots"
                placeholder="https://…/image.png"
                value=${url}
                @input=${(e: Event) =>
                  this.updateScreenshotRow(index, (e.target as HTMLInputElement).value)}
              />
              <button
                type="button"
                class="uh-fmp-shot-remove"
                aria-label="删除该预览图"
                @click=${() => this.removeScreenshotRow(index)}
              >&times;</button>
            </div>`,
        )}
        <button type="button" class="uh-fmp-btn uh-fmp-shot-add" @click=${this.addScreenshotRow}>
          + 添加一张预览图
        </button>
      </div>`;
  }

  private updateScreenshotRow(index: number, value: string): void {
    const rows = this.screenshotRows.slice();
    rows[index] = value;
    this.screenshotRows = rows;
  }

  private addScreenshotRow(): void {
    this.screenshotRows = [...this.screenshotRows, ""];
    // 新行渲染后自动滚动到底部（面板内滚动容器）
    this.updateComplete.then(() => {
      const rows = this.renderRoot.querySelectorAll(".uh-fmp-shot-row");
      const last = rows[rows.length - 1];
      if (last) {
        last.scrollIntoView({ block: "nearest", behavior: "smooth" });
      }
    });
  }

  private removeScreenshotRow(index: number): void {
    const rows = this.screenshotRows.filter((_, i) => i !== index);
    // 至少保留一行空输入
    this.screenshotRows = rows.length ? rows : [""];
  }

  private renderLinksModal() {
    const miniRows: LinkInfoRow[] = [
      { label: "小程序名称", value: this.miniInfo?.displayName },
      { label: "太阳码地址", value: normalizeImageUrl(this.miniInfo?.miniProgramCode) },
      { label: "小程序地址", value: this.miniInfo?.link },
      { label: "小程序描述", value: this.miniInfo?.description, textarea: true },
      { label: "申请说明", value: this.miniInfo?.applyRemark, textarea: true, copyable: false },
    ];
    const blogRows: LinkInfoRow[] = [
      { label: "博主昵称", value: this.blogger?.nickname },
      { label: "博主头像", value: normalizeImageUrl(this.blogger?.avatar) },
      { label: "博主主页", value: this.blogger?.website },
      { label: "博主简介", value: this.blogger?.description },
    ];
    const hasContent =
      miniRows.some((row) => row.value) || blogRows.some((row) => row.value);
    return html`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">小程序友链信息</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-modal-body">
            ${this.linksLoading
              ? html`<div class="uh-fmp-loading">加载中…</div>`
              : this.linksError
                ? html`<div class="uh-fmp-empty">加载失败，请稍后重试</div>`
                : !hasContent
                  ? html`<div class="uh-fmp-empty">暂无友链信息</div>`
                  : html`
                      <div class="uh-fmp-info-card">
                        <div class="uh-fmp-info-card-title">小程序信息</div>
                        ${miniRows.map((row) => this.renderCopyRow(row.label, row.value, row))}
                      </div>
                      <div class="uh-fmp-info-card">
                        <div class="uh-fmp-info-card-title">博主信息</div>
                        ${blogRows.map((row) => this.renderCopyRow(row.label, row.value, row))}
                      </div>
                      <button
                        type="button"
                        class="uh-fmp-btn uh-fmp-copy-all"
                        @click=${(e: Event) =>
                          this.copyText(this.collectLinkText(), e.target as HTMLButtonElement)}
                      >复制全部</button>
                    `}
          </div>
        </div>
      </div>
    `;
  }

  /** 单条信息：只读输入框（长文本 textarea）+ 复制按钮（点击全选）；copyable=false 无复制按钮 */
  private renderCopyRow(label: string, value?: string, options?: Partial<LinkInfoRow>) {
    if (!value) {
      return "";
    }
    const textarea = !!options?.textarea;
    const copyable = options?.copyable ?? true;
    const input = textarea
      ? html`
          <textarea
            class="uh-fmp-copy-input uh-fmp-copy-textarea"
            readonly
            rows="2"
            @click=${(e: Event) => (e.target as HTMLTextAreaElement).select()}
          >${value}</textarea>`
      : html`
          <input
            class="uh-fmp-copy-input"
            type="text"
            readonly
            value=${value}
            @click=${(e: Event) => (e.target as HTMLInputElement).select()}
          />`;
    return html`
      <div class="uh-fmp-copy-row">
        <span class="uh-fmp-copy-label">${label}</span>
        ${input}
        ${copyable
          ? html`
              <button
                type="button"
                class="uh-fmp-btn uh-fmp-copy-btn"
                @click=${(e: Event) => this.copyText(value, e.target as HTMLButtonElement)}
              >复制</button>`
          : ""}
      </div>`;
  }

  /** 复制到剪贴板（navigator.clipboard，失败回退 execCommand）；成功后按钮短暂显示「已复制」 */
  private async copyText(text: string, btn?: HTMLButtonElement): Promise<void> {
    try {
      await navigator.clipboard.writeText(text);
    } catch {
      // 非安全上下文/权限拒绝时回退到 execCommand
      const ta = document.createElement("textarea");
      ta.value = text;
      ta.style.position = "fixed";
      ta.style.opacity = "0";
      document.body.appendChild(ta);
      ta.select();
      try {
        document.execCommand("copy");
      } catch {
        // 复制失败静默
      }
      ta.remove();
    }
    if (btn) {
      const original = btn.textContent;
      btn.textContent = "已复制";
      setTimeout(() => {
        if (btn.isConnected) {
          btn.textContent = original;
        }
      }, 800);
    }
  }

  /** 拼接全部信息文本（「复制全部」用，格式：标签：值，换行分隔） */
  private collectLinkText(): string {
    const parts: string[] = [];
    const push = (label: string, value?: string): void => {
      if (value) {
        parts.push(`${label}：${value}`);
      }
    };
    const mini = this.miniInfo;
    push("小程序名称", mini?.displayName);
    push("太阳码地址", normalizeImageUrl(mini?.miniProgramCode));
    push("小程序地址", mini?.link);
    push("描述", mini?.description);
    push("申请说明", mini?.applyRemark);
    const blog = this.blogger;
    push("博主昵称", blog?.nickname);
    push("博主头像", normalizeImageUrl(blog?.avatar));
    push("博主主页", blog?.website);
    push("博主简介", blog?.description);
    return parts.join("\n");
  }
}

customElements.define("uh-float-mini-profile", FloatMiniProfileElement);

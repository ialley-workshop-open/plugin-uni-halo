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

/** 申请表单字段（对齐 MiniProgramLinkSubmissionSpec；displayName/miniProgramCode 必填） */
const APPLY_FIELDS: Array<{ key: string; label: string; required: boolean }> = [
  { key: "displayName", label: "小程序名称", required: true },
  { key: "miniProgramCode", label: "太阳码图片地址", required: true },
  { key: "link", label: "小程序地址", required: false },
  { key: "authorName", label: "作者昵称", required: false },
  { key: "website", label: "作者网站", required: false },
  { key: "description", label: "描述", required: false },
  { key: "applyRemark", label: "申请说明", required: false },
  { key: "email", label: "邮箱（选填，用于审核结果通知）", required: false },
];

interface DragState {
  startX: number;
  startY: number;
  left: number;
  top: number;
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

  private config: FloatMiniProfileConfig;
  private dragState: DragState | null = null;

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
    card.style.setProperty("--uh-fmp-edge", (Number(c.edgeHideDistance) || 24) + "px");
  }

  // ===== 拖拽（Pointer Events）+ 贴边隐藏 =====
  private onPointerDown(e: PointerEvent): void {
    const target = e.target as HTMLElement | null;
    // 关闭按钮与底部操作按钮（申请/友链信息）不触发拖拽：
    // 否则 setPointerCapture + preventDefault 会干扰 click 合成事件
    if (
      target &&
      (target.closest(".uh-fmp-close") ||
        target.closest(".uh-fmp-actions") ||
        target.closest(".uh-fmp-overlay"))
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
    let min = EDGE_TRIGGER;
    (["left", "right", "top", "bottom"] as const).forEach((key) => {
      if (distances[key] < min) {
        min = distances[key];
        side = key;
      }
    });
    if (side) {
      card.classList.add("uh-fmp-edge", "uh-fmp-edge-" + side);
    }
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
    this.captchaSrc = "data:image/png;base64," + captcha.imageBase64;
  }

  private async onApplySubmit(e: SubmitEvent): Promise<void> {
    e.preventDefault();
    const form = e.target as HTMLFormElement;
    const spec: Record<string, string> = {};
    APPLY_FIELDS.forEach((field) => {
      const el = form.elements.namedItem(field.key) as HTMLInputElement | null;
      if (el && el.value.trim()) {
        spec[field.key] = el.value.trim();
      }
    });
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
        class="uh-fmp"
        @pointerdown=${this.onPointerDown}
        @pointermove=${this.onPointerMove}
        @pointerup=${this.onPointerEnd}
        @pointercancel=${this.onPointerEnd}
      >
        ${c.closeEnabled !== false
          ? html`<button type="button" class="uh-fmp-close" aria-label="关闭悬浮窗" @click=${this.onCloseClick}>&times;</button>`
          : ""}
        ${c.imageUrl
          ? html`<img class="uh-fmp-img" src=${c.imageUrl} alt=${c.name || "小程序太阳码"} style="width:${size}px;height:${size}px" />`
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
                <button type="button" class="uh-fmp-btn" @click=${this.openApply}>申请</button>
                <button type="button" class="uh-fmp-btn" @click=${this.openLinks}>友链信息</button>
                <div class="uh-fmp-hint">小程序申请和友链信息</div>
              </div>`
          : ""}
      </div>
    `;
  }

  private renderApplyModal() {
    return html`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">小程序申请</div>
            <button type="button" class="uh-fmp-modal-close" aria-label="关闭" @click=${this.closeModals}>&times;</button>
          </div>
          <div class="uh-fmp-modal-body">
            <form class="uh-fmp-form" @submit=${this.onApplySubmit}>
              ${APPLY_FIELDS.map(
                (field) => html`
                  <label class="uh-fmp-field">
                    <span>${field.label}${field.required ? " *" : ""}</span>
                    <input type="text" name=${field.key} ?required=${field.required} />
                  </label>`,
              )}
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
                <button type="submit" class="uh-fmp-btn uh-fmp-btn-primary" ?disabled=${this.applySubmitting}>
                  ${this.applySubmitting ? "提交中…" : "提交申请"}
                </button>
                <button type="button" class="uh-fmp-btn" @click=${this.closeModals}>取消</button>
              </div>
            </form>
          </div>
        </div>
      </div>
    `;
  }

  private renderLinksModal() {
    const miniInfo = this.miniInfo;
    const blogger = this.blogger;
    const hasContent = !!(miniInfo && Object.keys(miniInfo).length) ||
      !!(blogger && Object.keys(blogger).length);
    return html`
      <div class="uh-fmp-overlay" @click=${this.onOverlayClick}>
        <div class="uh-fmp-modal">
          <div class="uh-fmp-modal-header">
            <div class="uh-fmp-modal-title">友链信息</div>
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
                      ${miniInfo && Object.keys(miniInfo).length
                        ? html`
                            <div class="uh-fmp-info-card">
                              ${miniInfo.miniProgramCode
                                ? html`<img class="uh-fmp-info-card-img" src=${miniInfo.miniProgramCode} alt="小程序太阳码" />`
                                : ""}
                              ${miniInfo.displayName
                                ? html`<div class="uh-fmp-info-card-title">${miniInfo.displayName}</div>`
                                : ""}
                              ${miniInfo.description
                                ? html`<div class="uh-fmp-info-card-desc">${miniInfo.description}</div>`
                                : ""}
                              ${miniInfo.applyRemark
                                ? html`<div class="uh-fmp-info-card-desc">${miniInfo.applyRemark}</div>`
                                : ""}
                              ${miniInfo.link
                                ? html`<a class="uh-fmp-info-card-link" href=${miniInfo.link} target="_blank" rel="noopener noreferrer">${miniInfo.link}</a>`
                                : ""}
                            </div>`
                        : ""}
                      ${blogger && Object.keys(blogger).length
                        ? html`
                            <div class="uh-fmp-info-card">
                              ${blogger.avatar
                                ? html`<img class="uh-fmp-info-card-avatar" src=${blogger.avatar} alt="博主头像" />`
                                : ""}
                              ${blogger.nickname
                                ? html`<div class="uh-fmp-info-card-title">${blogger.nickname}</div>`
                                : ""}
                              ${blogger.description
                                ? html`<div class="uh-fmp-info-card-desc">${blogger.description}</div>`
                                : ""}
                              ${blogger.website
                                ? html`<a class="uh-fmp-info-card-link" href=${blogger.website} target="_blank" rel="noopener noreferrer">${blogger.website}</a>`
                                : ""}
                            </div>`
                        : ""}
                    `}
          </div>
        </div>
      </div>
    `;
  }
}

customElements.define("uh-float-mini-profile", FloatMiniProfileElement);

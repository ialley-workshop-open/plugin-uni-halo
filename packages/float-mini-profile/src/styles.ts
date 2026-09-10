/**
 * 悬浮卡片样式（Lit css 模板）。
 *
 * 组件使用 shadow DOM，样式完全内聚不泄漏、外部主题样式也无法穿透影响组件；
 * 弹窗（overlay/modal）渲染在 shadow DOM 内部，按钮等选择器直接用类名
 * （无需 .uh-fmp 前缀），修复了此前「弹窗内按钮无样式」的问题。
 * 设计参考 app 端 glass（uh-styles / tabbar：半透明背景 + backdrop blur + 白色细边框 + 柔和阴影）。
 */
import { css } from "lit";

export const styles = css`
  :host {
    display: block;
  }

  .uh-fmp {
    position: fixed;
    z-index: 9999;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 10px;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC",
      "Microsoft YaHei", sans-serif;
    background: rgba(255, 255, 255, 0.92);
    -webkit-backdrop-filter: blur(12px);
    backdrop-filter: blur(12px);
    border: 2px solid rgba(255, 255, 255, 0.65);
    border-radius: 14px;
    box-shadow: 0 16px 60px rgba(0, 0, 0, 0.06);
    user-select: none;
    -webkit-user-select: none;
    cursor: grab;
    max-width: 40vw;
    line-height: 1.4;
    transition: transform 0.3s ease;
  }

  /* ===== 9 向锚点（配合 JS inline transform 偏移） ===== */
  .uh-fmp.uh-fmp-pos-top-left { top: 8px; left: 8px; }
  .uh-fmp.uh-fmp-pos-top-center { top: 8px; left: 50%; }
  .uh-fmp.uh-fmp-pos-top-right { top: 8px; right: 8px; }
  .uh-fmp.uh-fmp-pos-right-center { top: 50%; right: 8px; }
  .uh-fmp.uh-fmp-pos-bottom-right { bottom: 8px; right: 8px; }
  .uh-fmp.uh-fmp-pos-bottom-center { bottom: 8px; left: 50%; }
  .uh-fmp.uh-fmp-pos-bottom-left { bottom: 8px; left: 8px; }
  .uh-fmp.uh-fmp-pos-left-center { top: 50%; left: 8px; }
  .uh-fmp.uh-fmp-pos-center { top: 50%; left: 50%; }

  /* ===== 内容 ===== */
  .uh-fmp-img {
    display: block;
    max-width: 100%;
    height: auto;
    border-radius: 8px;
  }
  .uh-fmp-name {
    font-weight: 600;
    text-align: center;
  }
  .uh-fmp-desc {
    text-align: center;
    word-break: break-all;
  }

  /* ===== 关闭按钮（右上角） ===== */
  .uh-fmp-close {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    border: none;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.45);
    color: #ffffff;
    font-size: 14px;
    line-height: 20px;
    text-align: center;
    cursor: pointer;
    padding: 0;
  }
  .uh-fmp-close:hover {
    background: rgba(0, 0, 0, 0.65);
  }

  /* ===== 拖拽 ===== */
  .uh-fmp.uh-fmp-dragging {
    transition: none !important;
    cursor: grabbing;
  }

  /* ===== 贴边隐藏（JS 按最近边缘加 uh-fmp-edge-*，露出 var(--uh-fmp-edge) 宽把手） ===== */
  .uh-fmp.uh-fmp-edge-left { transform: translateX(calc(-100% + var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge-right { transform: translateX(calc(100% - var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge-top { transform: translateY(calc(-100% + var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge-bottom { transform: translateY(calc(100% - var(--uh-fmp-edge, 24px))); }
  .uh-fmp.uh-fmp-edge:hover,
  .uh-fmp.uh-fmp-edge:focus-within {
    transform: translate(0, 0);
  }

  /* ===== 关闭动画 ===== */
  .uh-fmp.uh-fmp-closing {
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  /* ===== 底部操作按钮（小程序申请开关开启后显示） ===== */
  .uh-fmp-actions {
    display: flex;
    flex-wrap: wrap; /* 两个按钮同一行，提示文字（flex-basis:100%）换行独占一行 */
    gap: 8px;
    width: 100%;
    margin-top: 2px;
  }
  .uh-fmp-btn {
    flex: 1;
    box-sizing: border-box;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.85);
    color: #333333;
    font-size: 12px;
    line-height: 1;
    padding: 7px 0;
    cursor: pointer;
    text-align: center;
    font-family: inherit;
  }
  .uh-fmp-btn:hover {
    background: #ffffff;
  }
  .uh-fmp-btn-primary {
    background: rgba(22, 119, 255, 0.92);
    border-color: transparent;
    color: #ffffff;
  }
  .uh-fmp-btn-primary:hover {
    background: #1677ff;
  }
  .uh-fmp-hint {
    flex-basis: 100%;
    text-align: center;
    font-size: 10px;
    line-height: 1;
    color: rgba(0, 0, 0, 0.45);
  }

  /* ===== 弹窗（遮罩 + 居中卡片，渲染于 shadow DOM 内） ===== */
  .uh-fmp-overlay {
    position: fixed;
    inset: 0;
    z-index: 2147483000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.45);
    padding: 16px;
    box-sizing: border-box;
  }
  .uh-fmp-modal {
    box-sizing: border-box;
    width: 100%;
    max-width: 360px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    border-radius: 14px;
    box-shadow: 0 16px 60px rgba(0, 0, 0, 0.18);
    overflow: hidden;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC",
      "Microsoft YaHei", sans-serif;
  }
  .uh-fmp-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  }
  .uh-fmp-modal-title {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a1a;
  }
  .uh-fmp-modal-close {
    border: none;
    background: transparent;
    font-size: 18px;
    line-height: 1;
    color: #999999;
    cursor: pointer;
    padding: 2px 4px;
  }
  .uh-fmp-modal-close:hover {
    color: #333333;
  }
  .uh-fmp-modal-body {
    padding: 14px;
    overflow-y: auto;
  }

  /* ===== 申请表单 ===== */
  .uh-fmp-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .uh-fmp-field {
    display: flex;
    flex-direction: column;
    gap: 4px;
    font-size: 12px;
    color: #666666;
  }
  .uh-fmp-field input[type="text"] {
    box-sizing: border-box;
    width: 100%;
    height: 32px;
    padding: 0 10px;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    font-size: 13px;
    color: #1a1a1a;
    outline: none;
    font-family: inherit;
  }
  .uh-fmp-field input[type="text"]:focus {
    border-color: #1677ff;
  }
  .uh-fmp-captcha-input {
    display: flex;
    gap: 8px;
  }
  .uh-fmp-captcha-input input {
    flex: 1;
  }
  .uh-fmp-captcha-img {
    width: 100px;
    height: 32px;
    border-radius: 8px;
    border: 1px solid rgba(0, 0, 0, 0.1);
    cursor: pointer;
    object-fit: cover;
  }
  .uh-fmp-form-actions {
    display: flex;
    gap: 8px;
    margin-top: 2px;
  }

  /* ===== 友链信息（小程序信息 + 博主信息） ===== */
  .uh-fmp-info-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    padding: 12px;
    border-radius: 10px;
    background: rgba(0, 0, 0, 0.03);
    margin-bottom: 10px;
  }
  .uh-fmp-info-card:last-child {
    margin-bottom: 0;
  }
  .uh-fmp-info-card-img {
    width: 72px;
    height: 72px;
    border-radius: 12px;
    object-fit: cover;
  }
  .uh-fmp-info-card-avatar {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    object-fit: cover;
  }
  .uh-fmp-info-card-title {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a1a;
  }
  .uh-fmp-info-card-desc {
    font-size: 12px;
    color: #999999;
    text-align: center;
    word-break: break-all;
    line-height: 1.5;
  }
  .uh-fmp-info-card-link {
    font-size: 12px;
    color: #1677ff;
    text-decoration: none;
    word-break: break-all;
  }
  .uh-fmp-info-card-link:hover {
    text-decoration: underline;
  }
  .uh-fmp-loading,
  .uh-fmp-empty {
    padding: 20px 0;
    text-align: center;
    font-size: 13px;
    color: #999999;
  }

  /* ===== 深色模式 ===== */
  @media (prefers-color-scheme: dark) {
    .uh-fmp {
      background: rgba(28, 28, 32, 0.85);
      border-color: rgba(255, 255, 255, 0.08);
      box-shadow: 0 16px 60px rgba(0, 0, 0, 0.35);
    }
    .uh-fmp-close {
      background: rgba(255, 255, 255, 0.25);
    }
    .uh-fmp-close:hover {
      background: rgba(255, 255, 255, 0.4);
    }
    .uh-fmp-modal {
      background: #1c1c20;
    }
    .uh-fmp-modal-header {
      border-bottom-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-modal-title,
    .uh-fmp-info-card-title {
      color: #f5f5f5;
    }
    .uh-fmp-field,
    .uh-fmp-info-card-desc {
      color: #999999;
    }
    .uh-fmp-field input[type="text"] {
      background: #2a2a30;
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-info-card {
      background: rgba(255, 255, 255, 0.06);
    }
    .uh-fmp-btn {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.12);
      color: #f5f5f5;
    }
    .uh-fmp-btn:hover {
      background: rgba(255, 255, 255, 0.16);
    }
    .uh-fmp-btn-primary {
      background: rgba(22, 119, 255, 0.9);
      color: #ffffff;
    }
    .uh-fmp-hint {
      color: rgba(255, 255, 255, 0.4);
    }
  }
`;

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
    background: rgba(255, 255, 255, 1);
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
    top: 8px;
    right: 8px;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    border-radius: 6px;
    border: 1px solid rgba(255,255,255, 0.5);
    background: rgba(255, 255, 255, 0.75);
    box-shadow: 0 0 12px rgba(0, 0, 0, 0.075);
    color: #999999;
    font-size: 14px;
    line-height: 20px;
    text-align: center;
    cursor: pointer;
  }
  .uh-fmp-close:hover {
    color: #333;
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
    flex-wrap: wrap;
    gap: 8px;
    width: 100%;
    margin-top: 2px;
  }
  .uh-fmp-btn {
    flex: 1;
    box-sizing: border-box;
    border: 1px solid rgba(0,0,0,0.05);
    border-radius: 8px;
    background: #ffffff;
    color: #0E1731;
    font-size: 12px;
    line-height: 1;
    padding: 8px 0;
    cursor: pointer;
    text-align: center;
    font-family: inherit;
    box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
    transition: background 0.15s ease;
  }
  .uh-fmp-btn:hover {
    background: #f1f5f9;
  }
  .uh-fmp-btn-primary {
    /* 主色 #0E1731 + 白色文字 */
    background: #0E1731;
    border-color: transparent;
    color: #ffffff;
  }
  .uh-fmp-btn-primary:hover {
    background: #16244a;
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
    /* glass 遮罩：半透明 + 轻微毛玻璃（对齐 app 弹窗遮罩） */
    background: rgba(0, 0, 0, 0.45);
    -webkit-backdrop-filter: blur(4px);
    backdrop-filter: blur(4px);
    padding: 16px;
    box-sizing: border-box;
  }
  .uh-fmp-modal {
    box-sizing: border-box;
    width: 100%;
    max-width: 420px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.98);
    -webkit-backdrop-filter: blur(16px);
    backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.6);
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
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    border-radius: 6px;
    border: 1px solid rgba(255,255,255, 0.5);
    background: rgba(255, 255, 255, 0.75);
    box-shadow: 0 0 12px rgba(0, 0, 0, 0.075);
    font-size: 14px;
    color: #999999;
    cursor: pointer;
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
  /* 申请弹窗面板内字段上下间距（footer 内验证码区不受影响） */
  .uh-fmp-apply-panel .uh-fmp-field {
    margin-bottom: 10px;
  }
  .uh-fmp-apply-panel .uh-fmp-field:last-child {
    margin-bottom: 0;
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
  .uh-fmp-field input[type="text"]:focus,
  .uh-fmp-field select:focus,
  .uh-fmp-field textarea:focus {
    border-color: #37c2bc;
  }
  .uh-fmp-field select,
  .uh-fmp-field textarea {
    box-sizing: border-box;
    width: 100%;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    font-size: 13px;
    color: #1a1a1a;
    outline: none;
    font-family: inherit;
    background: #ffffff;
  }
  .uh-fmp-field select {
    height: 32px;
    padding: 0 8px;
  }
  .uh-fmp-field textarea {
    padding: 6px 10px;
    resize: vertical;
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
    margin-top: 12px;
  }

  /* ===== 申请弹窗：分段器 + 面板 + 底部固定操作区 ===== */
  .uh-fmp-modal-apply {
    max-height: 80vh;
  }
  /* shadcn Tabs（radix tabs）风格分段器：track 连体浅灰背景，激活项浮起 */
  .uh-fmp-segmented {
    display: flex;
    gap: 4px;
    margin: 8px 14px 0;
    padding: 4px; /* track 内边距（对齐 tabs-list p-1） */
    background: rgba(0, 0, 0, 0.05);
    border-radius: 8px;
  }
  .uh-fmp-seg-item {
    flex: 1;
    box-sizing: border-box;
    padding: 6px 0;
    border: none;
    border-radius: 8px;
    background: transparent;
    color: #666666;
    font-size: 13px;
    cursor: pointer;
    font-family: inherit;
  }
  .uh-fmp-seg-active {
    /* 激活块：主色 #0E1731 + 白字 + 轻投影 */
    background: #0E1731;
    color: #ffffff;
    font-weight: 600;
    box-shadow: 0 1px 2px rgba(14, 23, 49, 0.35);
  }
  .uh-fmp-apply-body {
    display: flex;
    flex-direction: column;
    padding: 10px 14px 14px;
    overflow: hidden;
  }
  .uh-fmp-apply-panels {
    flex: 1;
    min-height: 0;
    max-height: 40vh; /* 内容区最大高度 40vh + 滚动 */
    overflow-y: auto;
    scrollbar-width: thin; /* Firefox */
    scrollbar-color: rgba(120, 130, 150, 0.4) transparent;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar {
    width: 6px;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar-track {
    background: transparent;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar-thumb {
    background: rgba(120, 130, 150, 0.4);
    border-radius: 3px;
  }
  .uh-fmp-apply-panels::-webkit-scrollbar-thumb:hover {
    background: rgba(120, 130, 150, 0.6);
  }
  .uh-fmp-apply-footer {
    flex-shrink: 0;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    padding-top: 10px;
    margin-top: 10px;
  }
  /* 预览图动态行 */
  .uh-fmp-shot-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 6px; /* 预览图行之间上下间距 */
  }
  .uh-fmp-shot-input {
    flex: 1;
    min-width: 0;
    box-sizing: border-box;
    height: 32px;
    padding: 0 10px;
    border: 1px solid rgba(0, 0, 0, 0.12);
    border-radius: 8px;
    font-size: 13px;
    color: #1a1a1a;
    outline: none;
    font-family: inherit;
    background: #ffffff;
  }
  .uh-fmp-shot-input:focus {
    border-color: #37c2bc;
  }
  .uh-fmp-shot-remove {
    flex-shrink: 0;
    width: 28px;
    height: 28px;
    border: none;
    border-radius: 6px;
    background: rgba(0, 0, 0, 0.05);
    color: #999999;
    font-size: 16px;
    line-height: 1;
    cursor: pointer;
    padding: 0;
  }
  .uh-fmp-shot-remove:hover {
    background: rgba(0, 0, 0, 0.1);
    color: #333333;
  }
  .uh-fmp-shot-add {
    margin-top: 2px;
  }

  /* ===== 友链信息（小程序信息 + 博主信息，输入框行 + 复制） ===== */
  .uh-fmp-info-card {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 12px;
    border-radius: 10px;
    background: rgba(0, 0, 0, 0.03);
    margin-bottom: 10px;
  }
  .uh-fmp-info-card-title {
    font-size: 13px;
    font-weight: 600;
    color: #1a1a1a;
    margin-bottom: 2px;
  }
  .uh-fmp-copy-row {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .uh-fmp-copy-label {
    flex-shrink: 0;
    font-size: 12px;
    color: #000000; /* label 正常黑色 */
    min-width: 64px;
    text-align: right;
  }
  .uh-fmp-copy-input {
    flex: 1;
    min-width: 0;
    box-sizing: border-box;
    height: 28px;
    padding: 0 8px;
    /* 只读输入框：浅白背景 + 白色边框（与玻璃弹窗背景区分） */
    border: 1px solid #ffffff;
    border-radius: 6px;
    font-size: 12px;
    color: #1a1a1a;
    background: rgba(255, 255, 255, 0.75);
    outline: none;
    font-family: inherit;
  }
  .uh-fmp-copy-input:focus {
    border-color: #37c2bc;
  }
  .uh-fmp-copy-textarea {
    height: auto;
    min-height: 40px;
    padding: 5px 8px;
    line-height: 1.4;
    resize: none;
    font-family: inherit;
  }
  .uh-fmp-copy-btn {
    flex-shrink: 0;
    flex: none;
    width: 60px;
    padding: 8px 0;
  }
  .uh-fmp-copy-all {
    width: 100%;
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
      background: rgba(0, 0, 0, 0.75);
      border-color: rgba(0, 0, 0, 0.9);
    }
    .uh-fmp-close:hover {
      color: #fff;
    }
    .uh-fmp-modal {
      background: rgba(28, 28, 32, 0.9);
      border-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-modal-header {
      border-bottom-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-modal-title,
    .uh-fmp-info-card-title {
      color: #f5f5f5;
    }
    .uh-fmp-field,
    .uh-fmp-copy-label {
      color: #999999;
    }
    .uh-fmp-field input[type="text"],
    .uh-fmp-field select,
    .uh-fmp-field textarea {
      background: #2a2a30;
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-segmented {
      background: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-seg-item {
      background: transparent;
      color: #999999;
    }
    .uh-fmp-seg-active {
      background: #0E1731;
      color: #ffffff;
    }
    .uh-fmp-shot-input {
      background: #2a2a30;
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-shot-remove {
      background: rgba(255, 255, 255, 0.1);
      color: #999999;
    }
    .uh-fmp-apply-footer {
      border-top-color: rgba(255, 255, 255, 0.08);
    }
    .uh-fmp-info-card {
      background: rgba(255, 255, 255, 0.06);
    }
    .uh-fmp-copy-input {
      background: rgba(255, 255, 255, 0.06);
      border-color: rgba(255, 255, 255, 0.1);
      color: #f5f5f5;
    }
    .uh-fmp-btn {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.12);
      color: #f5f5f5;
    }
    .uh-fmp-btn:hover {
      background: rgba(255, 255, 255, 0.14);
    }
    .uh-fmp-btn-primary {
      background: #0E1731;
      color: #ffffff;
    }
    .uh-fmp-btn-primary:hover {
      background: #16244a;
    }
    .uh-fmp-hint {
      color: rgba(255, 255, 255, 0.4);
    }
  }
`;

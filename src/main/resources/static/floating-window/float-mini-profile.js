/**
 * UniHalo 主题悬浮窗（小程序太阳码悬浮球）
 *
 * 由 FloatingWindowHeadProcessor 注入到主题页 <head>，此脚本 defer 加载，
 * 读取 window.__UNI_HALO_FLOAT_MINI_PROFILE__ 配置后自挂载到 body 末尾。职责：
 *   1. 页面显示范围匹配（all / only / except + 通配符路径模式）
 *   2. 关闭状态记忆（localStorage）
 *   3. 渲染太阳码 + 名称 + 描述（textContent 渲染，防注入）
 *   4. 9 向锚点定位 + 偏移（inline transform）
 *   5. 拖拽（Pointer Events，兼容触屏）与贴边隐藏（hover 滑出）
 *
 * 设计见 .docs/floating-window-design.md。
 */
(function () {
  "use strict";

  var CONFIG = window.__UNI_HALO_FLOAT_MINI_PROFILE__;
  if (!CONFIG || typeof CONFIG !== "object") {
    return;
  }

  var STORAGE_KEY = "float-mini-profile-closed";
  var EDGE_TRIGGER = 80; // 距视口边缘小于该值视为贴边

  // ===== 页面显示范围匹配（对齐 plugin-announcement 的 matchUrlPattern 语义） =====
  function matchUrlPattern(patterns) {
    var path = window.location.pathname;
    // 为空或只有 /，表示仅首页
    if (!patterns || !patterns.trim()) {
      return path === "/" || path === "";
    }
    var lines = patterns
      .split("\n")
      .map(function (line) {
        return line.trim();
      })
      .filter(Boolean);
    if (lines.length === 1 && lines[0] === "/") {
      return path === "/" || path === "";
    }
    // 匹配任意一个规则即可；* 匹配非 / 字符，** 匹配任意字符（含 /）
    return lines.some(function (pattern) {
      var regex = pattern
        .replace(/[.+?^${}()|[\]\\]/g, "\\$&")
        .replace(/\*\*/g, "{{DOUBLE}}")
        .replace(/\*/g, "[^/]*")
        .replace(/\{\{DOUBLE\}\}/g, ".*");
      try {
        return new RegExp("^" + regex + "$").test(path);
      } catch (e) {
        return false;
      }
    });
  }

  function matchPage() {
    var scope = CONFIG.pageScope || "all";
    if (scope === "all") {
      return true;
    }
    var matched = matchUrlPattern(CONFIG.pagePatterns);
    return scope === "only" ? matched : !matched;
  }

  // ===== DOM 构建 =====
  function buildElement() {
    var card = document.createElement("div");
    card.className = "float-mini-profile";

    if (CONFIG.closeEnabled !== false) {
      var close = document.createElement("button");
      close.type = "button";
      close.className = "float-mini-profile-close";
      close.setAttribute("aria-label", "关闭悬浮窗");
      close.innerHTML = "&times;"; // 固定符号，非用户输入
      card.appendChild(close);
    }

    if (CONFIG.imageUrl) {
      var img = document.createElement("img");
      img.className = "float-mini-profile-img";
      img.src = CONFIG.imageUrl;
      img.alt = CONFIG.name || "小程序太阳码";
      var size = Number(CONFIG.imageSize) || 100;
      img.style.width = size + "px";
      img.style.height = size + "px";
      card.appendChild(img);
    }

    if (CONFIG.name) {
      var nameEl = document.createElement("div");
      nameEl.className = "float-mini-profile-name";
      nameEl.textContent = String(CONFIG.name);
      nameEl.style.fontSize = (Number(CONFIG.nameSize) || 14) + "px";
      nameEl.style.color = CONFIG.nameColor || "#333333";
      card.appendChild(nameEl);
    }

    if (CONFIG.description) {
      var descEl = document.createElement("div");
      descEl.className = "float-mini-profile-desc";
      descEl.textContent = String(CONFIG.description);
      descEl.style.fontSize = (Number(CONFIG.descSize) || 12) + "px";
      descEl.style.color = CONFIG.descColor || "#999999";
      card.appendChild(descEl);
    }

    return card;
  }

  // ===== 定位：9 向锚点 + 偏移（inline transform 携带动态偏移） =====
  function applyPosition(el) {
    var pos = CONFIG.position || "bottom-right";
    el.classList.add("float-mini-profile-pos-" + pos);

    var x = Number(CONFIG.offsetX) || 0;
    var y = Number(CONFIG.offsetY) || 0;
    var tx =
      pos === "center" || pos === "top-center" || pos === "bottom-center"
        ? "calc(-50% + " + x + "px)"
        : x + "px";
    var ty =
      pos === "center" || pos === "left-center" || pos === "right-center"
        ? "calc(-50% + " + y + "px)"
        : y + "px";
    el.style.transform = "translate(" + tx + ", " + ty + ")";
    el.style.setProperty("--float-mini-profile-edge", (Number(CONFIG.edgeHideDistance) || 24) + "px");
  }

  // ===== 拖拽：Pointer Events（拖动转 left/top 自由定位，松手检测贴边） =====
  function setFree(el, left, top) {
    el.style.left = left + "px";
    el.style.top = top + "px";
    el.style.right = "auto";
    el.style.bottom = "auto";
    el.style.transform = "translate(0, 0)";
    el.classList.remove("float-mini-profile-edge");
  }

  function maybeEdgeHide(el) {
    if (!CONFIG.edgeHideEnabled) {
      return;
    }
    var rect = el.getBoundingClientRect();
    var distances = {
      left: rect.left,
      right: window.innerWidth - rect.right,
      top: rect.top,
      bottom: window.innerHeight - rect.bottom,
    };
    var side = null;
    var min = EDGE_TRIGGER;
    ["left", "right", "top", "bottom"].forEach(function (key) {
      if (distances[key] < min) {
        min = distances[key];
        side = key;
      }
    });
    if (side) {
      el.classList.add("float-mini-profile-edge", "float-mini-profile-edge-" + side);
    }
  }

  function initDrag(el) {
    var dragState = null;

    el.addEventListener("pointerdown", function (e) {
      if (e.target.closest(".float-mini-profile-close")) {
        return; // 关闭按钮不触发拖拽
      }
      var rect = el.getBoundingClientRect();
      dragState = {
        startX: e.clientX,
        startY: e.clientY,
        left: rect.left,
        top: rect.top,
      };
      el.classList.add("float-mini-profile-dragging");
      el.style.touchAction = "none"; // 拖拽期间禁止触摸滚动
      el.setPointerCapture(e.pointerId);
      e.preventDefault();
    });

    el.addEventListener("pointermove", function (e) {
      if (!dragState) {
        return;
      }
      var left = dragState.left + (e.clientX - dragState.startX);
      var top = dragState.top + (e.clientY - dragState.startY);
      // 限制在视口内
      left = Math.max(0, Math.min(left, window.innerWidth - el.offsetWidth));
      top = Math.max(0, Math.min(top, window.innerHeight - el.offsetHeight));
      setFree(el, left, top);
    });

    function endDrag() {
      if (!dragState) {
        return;
      }
      dragState = null;
      el.classList.remove("float-mini-profile-dragging");
      el.style.touchAction = "";
      maybeEdgeHide(el);
    }

    el.addEventListener("pointerup", endDrag);
    el.addEventListener("pointercancel", endDrag);
  }

  // ===== 关闭：淡出移除 + localStorage 记忆 =====
  function initClose(el) {
    var close = el.querySelector(".float-mini-profile-close");
    if (!close) {
      return;
    }
    close.addEventListener("click", function () {
      if (CONFIG.rememberClosed !== false) {
        try {
          localStorage.setItem(STORAGE_KEY, "1");
        } catch (e) {
          // localStorage 不可用时仅本次会话关闭
        }
      }
      el.classList.add("float-mini-profile-closing");
      setTimeout(function () {
        el.remove();
      }, 200);
    });
  }

  // ===== 挂载 =====
  function mount() {
    // 关闭记忆（rememberClosed 默认 true）
    if (CONFIG.rememberClosed !== false) {
      try {
        if (localStorage.getItem(STORAGE_KEY) === "1") {
          return;
        }
      } catch (e) {
        // 忽略读取失败
      }
    }
    // 防重复注入
    if (document.querySelector(".float-mini-profile")) {
      return;
    }
    var el = buildElement();
    document.body.appendChild(el);
    applyPosition(el);
    if (CONFIG.dragEnabled !== false) {
      initDrag(el);
    }
    if (CONFIG.closeEnabled !== false) {
      initClose(el);
    }
  }

  if (document.body) {
    mount();
  } else {
    document.addEventListener("DOMContentLoaded", mount);
  }
})();

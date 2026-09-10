/**
 * UniHalo 主题悬浮窗（小程序太阳码悬浮卡片）
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

  var STORAGE_KEY = "uh-fmp-closed";
  var EDGE_TRIGGER = 80; // 距视口边缘小于该值视为贴边

  // 公开接口（api.unihalo.ialley.cn 分组，匿名可访问；app 端同源接口）
  var API_BASE = "/apis/api.unihalo.ialley.cn/v1alpha1/plugins/plugin-uni-halo";
  var CAPTCHA_URL = API_BASE + "/captcha/generate";
  var LINK_LIST_URL = API_BASE + "/mini-program-links";
  var LINK_SUBMIT_URL = API_BASE + "/mini-program-links/submissions";

  // ===== 页面显示范围匹配 =====
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
    card.className = "uh-fmp";

    if (CONFIG.closeEnabled !== false) {
      var close = document.createElement("button");
      close.type = "button";
      close.className = "uh-fmp-close";
      close.setAttribute("aria-label", "关闭悬浮窗");
      close.innerHTML = "&times;"; // 固定符号，非用户输入
      card.appendChild(close);
    }

    if (CONFIG.imageUrl) {
      var img = document.createElement("img");
      img.className = "uh-fmp-img";
      img.src = CONFIG.imageUrl;
      img.alt = CONFIG.name || "小程序太阳码";
      var size = Number(CONFIG.imageSize) || 100;
      img.style.width = size + "px";
      img.style.height = size + "px";
      card.appendChild(img);
    }

    if (CONFIG.name) {
      var nameEl = document.createElement("div");
      nameEl.className = "uh-fmp-name";
      nameEl.textContent = String(CONFIG.name);
      nameEl.style.fontSize = (Number(CONFIG.nameSize) || 14) + "px";
      nameEl.style.color = CONFIG.nameColor || "#333333";
      card.appendChild(nameEl);
    }

    if (CONFIG.description) {
      var descEl = document.createElement("div");
      descEl.className = "uh-fmp-desc";
      descEl.textContent = String(CONFIG.description);
      descEl.style.fontSize = (Number(CONFIG.descSize) || 12) + "px";
      descEl.style.color = CONFIG.descColor || "#999999";
      card.appendChild(descEl);
    }

    // 小程序申请开关：卡片底部双按钮（申请 / 友链信息）
    if (CONFIG.miniProgramApply) {
      var actions = document.createElement("div");
      actions.className = "uh-fmp-actions";
      var applyBtn = document.createElement("button");
      applyBtn.type = "button";
      applyBtn.className = "uh-fmp-btn";
      applyBtn.textContent = "申请";
      applyBtn.addEventListener("click", function (e) {
        e.stopPropagation();
        openApplyModal();
      });
      var linksBtn = document.createElement("button");
      linksBtn.type = "button";
      linksBtn.className = "uh-fmp-btn";
      linksBtn.textContent = "友链信息";
      linksBtn.addEventListener("click", function (e) {
        e.stopPropagation();
        openLinksModal();
      });
      actions.appendChild(applyBtn);
      actions.appendChild(linksBtn);
      // 按钮底部浅色说明（10px）
      var hint = document.createElement("div");
      hint.className = "uh-fmp-hint";
      hint.textContent = "小程序申请和友链信息";
      actions.appendChild(hint);
      card.appendChild(actions);
    }

    return card;
  }

  // ===== 定位：9 向锚点 + 偏移（inline transform 携带动态偏移） =====
  function applyPosition(el) {
    var pos = CONFIG.position || "bottom-right";
    el.classList.add("uh-fmp-pos-" + pos);

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
    el.style.setProperty("--uh-fmp-edge", (Number(CONFIG.edgeHideDistance) || 24) + "px");
  }

  // ===== 拖拽：Pointer Events（拖动转 left/top 自由定位，松手检测贴边） =====
  function setFree(el, left, top) {
    el.style.left = left + "px";
    el.style.top = top + "px";
    el.style.right = "auto";
    el.style.bottom = "auto";
    el.style.transform = "translate(0, 0)";
    el.classList.remove("uh-fmp-edge");
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
      el.classList.add("uh-fmp-edge", "uh-fmp-edge-" + side);
    }
  }

  function initDrag(el) {
    var dragState = null;

    el.addEventListener("pointerdown", function (e) {
      if (e.target.closest(".uh-fmp-close")) {
        return; // 关闭按钮不触发拖拽
      }
      var rect = el.getBoundingClientRect();
      dragState = {
        startX: e.clientX,
        startY: e.clientY,
        left: rect.left,
        top: rect.top,
      };
      el.classList.add("uh-fmp-dragging");
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
      el.classList.remove("uh-fmp-dragging");
      el.style.touchAction = "";
      maybeEdgeHide(el);
    }

    el.addEventListener("pointerup", endDrag);
    el.addEventListener("pointercancel", endDrag);
  }

  // ===== 关闭：淡出移除 + localStorage 记忆 =====
  function initClose(el) {
    var close = el.querySelector(".uh-fmp-close");
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
      el.classList.add("uh-fmp-closing");
      setTimeout(function () {
        el.remove();
      }, 200);
    });
  }

  // ===== 弹窗（小程序申请表单 / 友链信息展示） =====
  function createModal(title) {
    var overlay = document.createElement("div");
    overlay.className = "uh-fmp-overlay";

    var box = document.createElement("div");
    box.className = "uh-fmp-modal";

    var header = document.createElement("div");
    header.className = "uh-fmp-modal-header";
    var titleEl = document.createElement("div");
    titleEl.className = "uh-fmp-modal-title";
    titleEl.textContent = title;
    var closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.className = "uh-fmp-modal-close";
    closeBtn.setAttribute("aria-label", "关闭");
    closeBtn.innerHTML = "&times;"; // 固定符号，非用户输入
    closeBtn.addEventListener("click", function () {
      closeModal(overlay);
    });
    header.appendChild(titleEl);
    header.appendChild(closeBtn);

    var body = document.createElement("div");
    body.className = "uh-fmp-modal-body";

    box.appendChild(header);
    box.appendChild(body);
    overlay.appendChild(box);

    // 点击遮罩空白处关闭
    overlay.addEventListener("click", function (e) {
      if (e.target === overlay) {
        closeModal(overlay);
      }
    });

    document.body.appendChild(overlay);
    return { overlay: overlay, body: body };
  }

  function closeModal(overlay) {
    if (overlay) {
      overlay.remove();
    }
  }

  /** 加载验证码图片（接口返回 {id, imageBase64}，base64 无 data 前缀需手动拼接） */
  function loadCaptcha(img) {
    fetch(CAPTCHA_URL)
      .then(function (res) {
        return res.json();
      })
      .then(function (data) {
        if (data && data.imageBase64) {
          img.src = "data:image/png;base64," + data.imageBase64;
          img.dataset.captchaId = data.id;
        }
      })
      .catch(function () {
        // 验证码加载失败静默处理
      });
  }

  /** 小程序申请弹窗：表单 + 验证码，提交 POST /mini-program-links/submissions */
  function openApplyModal() {
    var m = createModal("小程序申请");

    var form = document.createElement("form");
    form.className = "uh-fmp-form";

    var fields = [
      { key: "displayName", label: "小程序名称", required: true },
      { key: "miniProgramCode", label: "太阳码图片地址", required: true },
      { key: "link", label: "小程序地址", required: false },
      { key: "authorName", label: "作者昵称", required: false },
      { key: "website", label: "作者网站", required: false },
      { key: "description", label: "描述", required: false },
      { key: "applyRemark", label: "申请说明", required: false },
      { key: "email", label: "邮箱（选填，用于审核结果通知）", required: false },
    ];
    fields.forEach(function (field) {
      var label = document.createElement("label");
      label.className = "uh-fmp-field";
      var span = document.createElement("span");
      span.textContent = field.label + (field.required ? " *" : "");
      var input = document.createElement("input");
      input.type = "text";
      input.name = field.key;
      input.required = field.required;
      label.appendChild(span);
      label.appendChild(input);
      form.appendChild(label);
    });

    // 验证码行（点击图片刷新）
    var captchaRow = document.createElement("label");
    captchaRow.className = "uh-fmp-field uh-fmp-captcha-row";
    var captchaSpan = document.createElement("span");
    captchaSpan.textContent = "验证码 *";
    var captchaWrap = document.createElement("span");
    captchaWrap.className = "uh-fmp-captcha-input";
    var captchaInput = document.createElement("input");
    captchaInput.type = "text";
    captchaInput.name = "captchaCode";
    captchaInput.required = true;
    captchaInput.autocomplete = "off";
    var captchaImg = document.createElement("img");
    captchaImg.className = "uh-fmp-captcha-img";
    captchaImg.alt = "验证码";
    captchaImg.title = "看不清？点击刷新";
    captchaImg.addEventListener("click", function () {
      loadCaptcha(captchaImg);
    });
    captchaWrap.appendChild(captchaInput);
    captchaWrap.appendChild(captchaImg);
    captchaRow.appendChild(captchaSpan);
    captchaRow.appendChild(captchaWrap);
    form.appendChild(captchaRow);

    // 提交 / 取消
    var actions = document.createElement("div");
    actions.className = "uh-fmp-form-actions";
    var submitBtn = document.createElement("button");
    submitBtn.type = "submit";
    submitBtn.className = "uh-fmp-btn uh-fmp-btn-primary";
    submitBtn.textContent = "提交申请";
    var cancelBtn = document.createElement("button");
    cancelBtn.type = "button";
    cancelBtn.className = "uh-fmp-btn";
    cancelBtn.textContent = "取消";
    cancelBtn.addEventListener("click", function () {
      closeModal(m.overlay);
    });
    actions.appendChild(submitBtn);
    actions.appendChild(cancelBtn);
    form.appendChild(actions);

    form.addEventListener("submit", function (e) {
      e.preventDefault();
      submitApply(form, m);
    });

    m.body.appendChild(form);
    loadCaptcha(captchaImg);
  }

  /** 提交申请：验证码走 query 参数；403 时按响应附的新验证码刷新重试 */
  function submitApply(form, m) {
    var spec = {};
    ["displayName", "miniProgramCode", "link", "authorName", "website",
      "description", "applyRemark", "email"].forEach(function (key) {
      var el = form.elements[key];
      if (el && el.value && el.value.trim()) {
        spec[key] = el.value.trim();
      }
    });
    var captchaImg = form.querySelector(".uh-fmp-captcha-img");
    var captchaId = captchaImg ? captchaImg.dataset.captchaId : "";
    var captchaCode = (form.elements.captchaCode.value || "").trim();
    var query = "?captchaId=" + encodeURIComponent(captchaId || "") +
      "&captchaCode=" + encodeURIComponent(captchaCode);

    submitBtnBusy(form, true);

    fetch(LINK_SUBMIT_URL + query, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ spec: spec }),
    })
      .then(function (res) {
        return res.json().catch(function () {
          return {};
        }).then(function (data) {
          return { status: res.status, data: data };
        });
      })
      .then(function (result) {
        if (result.status === 200 || result.status === 201) {
          closeModal(m.overlay);
          alert("申请提交成功，请等待审核");
          return;
        }
        // 403：验证码错误/过期，附新验证码即时刷新
        if (result.status === 403 && result.data && result.data.captcha) {
          if (captchaImg) {
            captchaImg.src = "data:image/png;base64," + result.data.captcha.imageBase64;
            captchaImg.dataset.captchaId = result.data.captcha.id;
          }
        }
        alert((result.data && result.data.message) || "提交失败，请重试");
      })
      .catch(function () {
        alert("网络异常，请稍后重试");
      })
      .finally(function () {
        submitBtnBusy(form, false);
      });
  }

  function submitBtnBusy(form, busy) {
    var btn = form.querySelector('button[type="submit"]');
    if (btn) {
      btn.disabled = busy;
      btn.textContent = busy ? "提交中…" : "提交申请";
    }
  }

  /** 友链信息弹窗：GET /mini-program-links?grouped=true 按分组展示 */
  function openLinksModal() {
    var m = createModal("友链信息");
    m.body.className = "uh-fmp-modal-body uh-fmp-links-body";
    var loading = document.createElement("div");
    loading.className = "uh-fmp-loading";
    loading.textContent = "加载中…";
    m.body.appendChild(loading);

    fetch(LINK_LIST_URL + "?grouped=true")
      .then(function (res) {
        return res.json();
      })
      .then(function (groups) {
        m.body.textContent = "";
        if (!Array.isArray(groups) || groups.length === 0) {
          var empty = document.createElement("div");
          empty.className = "uh-fmp-empty";
          empty.textContent = "暂无友链信息";
          m.body.appendChild(empty);
          return;
        }
        groups.forEach(function (group) {
          var section = document.createElement("div");
          section.className = "uh-fmp-links-group";
          var groupTitle = document.createElement("div");
          groupTitle.className = "uh-fmp-links-group-title";
          groupTitle.textContent = group.displayName || "未分组";
          section.appendChild(groupTitle);
          var links = group.links || [];
          if (links.length === 0) {
            var groupEmpty = document.createElement("div");
            groupEmpty.className = "uh-fmp-empty";
            groupEmpty.textContent = "该分组暂无链接";
            section.appendChild(groupEmpty);
          }
          links.forEach(function (link) {
            var spec = link.spec || {};
            var item = document.createElement("a");
            item.className = "uh-fmp-link-item";
            if (spec.link) {
              item.href = spec.link;
              item.target = "_blank";
              item.rel = "noopener noreferrer";
            }
            var name = document.createElement("div");
            name.className = "uh-fmp-link-name";
            name.textContent = spec.displayName || "未命名";
            var desc = document.createElement("div");
            desc.className = "uh-fmp-link-desc";
            desc.textContent = spec.description || spec.authorName || "";
            item.appendChild(name);
            if (desc.textContent) {
              item.appendChild(desc);
            }
            section.appendChild(item);
          });
          m.body.appendChild(section);
        });
      })
      .catch(function () {
        m.body.textContent = "";
        var error = document.createElement("div");
        error.className = "uh-fmp-empty";
        error.textContent = "加载失败，请稍后重试";
        m.body.appendChild(error);
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
    if (document.querySelector(".uh-fmp")) {
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

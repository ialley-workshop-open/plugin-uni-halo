/**
 * 页面显示范围匹配（对齐 plugin-announcement 的 matchUrlPattern 语义）：
 * pageScope = all 恒显示；only 仅匹配路径显示；except 匹配路径不显示。
 * 通配符：* 匹配非 / 字符，** 匹配任意字符（含 /）；空或 / 表示仅首页。
 */
import { CONFIG } from "./config";

export function matchUrlPattern(patterns?: string): boolean {
  const path = window.location.pathname;
  // 为空或只有 /，表示仅首页
  if (!patterns || !patterns.trim()) {
    return path === "/" || path === "";
  }
  const lines = patterns
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean);
  if (lines.length === 1 && lines[0] === "/") {
    return path === "/" || path === "";
  }
  // 匹配任意一个规则即可
  return lines.some((pattern) => {
    const regex = pattern
      .replace(/[.+?^${}()|[\]\\]/g, "\\$&")
      .replace(/\*\*/g, "{{DOUBLE}}")
      .replace(/\*/g, "[^/]*")
      .replace(/\{\{DOUBLE\}\}/g, ".*");
    try {
      return new RegExp("^" + regex + "$").test(path);
    } catch {
      return false;
    }
  });
}

export function matchPage(): boolean {
  const scope = CONFIG?.pageScope || "all";
  if (scope === "all") {
    return true;
  }
  const matched = matchUrlPattern(CONFIG?.pagePatterns);
  return scope === "only" ? matched : !matched;
}

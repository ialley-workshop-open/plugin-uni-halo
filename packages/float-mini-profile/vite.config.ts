/**
 * 悬浮卡片独立构建
 * vite lib mode（iife）产出压缩版 float-mini-profile.js + float-mini-profile.css，
 * closeBundle 时自动拷贝到插件静态目录 src/main/resources/static/floating-window/
 * （文件名与手写版一致，插件注入 URL 无需改动）。
 */
import { copyFileSync, existsSync, mkdirSync } from "fs";
import { join } from "path";
import { fileURLToPath } from "url";
import { defineConfig, type Plugin } from "vite";

const copyToStatic = (): Plugin => ({
  name: "copy-to-static",
  closeBundle() {
    const distDir = join(process.cwd(), "dist");
    const staticDir = fileURLToPath(
      new URL("../../src/main/resources/static/floating-window", import.meta.url),
    );

    if (!existsSync(staticDir)) {
      mkdirSync(staticDir, { recursive: true });
    }

    const targets: Array<[string, string]> = [
      ["float-mini-profile.js", "float-mini-profile.js"],
      ["float-mini-profile.css", "float-mini-profile.css"],
    ];
    targets.forEach(([src, dest]) => {
      const srcPath = join(distDir, src);
      const destPath = join(staticDir, dest);
      if (existsSync(srcPath)) {
        copyFileSync(srcPath, destPath);
        console.log(`✓ Copied ${src} -> ${staticDir}`);
      }
    });
  },
});

export default defineConfig({
  plugins: [copyToStatic()],
  build: {
    lib: {
      entry: "src/index.ts",
      name: "FloatMiniProfile",
      // 固定产物文件名（含扩展名），避免 vite 为 iife 追加 .iife 后缀，
      // 与插件静态目录/注入 URL 保持一致（float-mini-profile.js）
      fileName: () => "float-mini-profile.js",
      formats: ["iife"],
    },
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        extend: true,
        assetFileNames: "float-mini-profile.[ext]",
      },
    },
    // minify 默认 esbuild，产物即压缩版
  },
});

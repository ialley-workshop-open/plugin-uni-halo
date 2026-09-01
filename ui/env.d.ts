/// <reference types="vite/client" />
/// <reference types="unplugin-icons/types/vue" />

// @ckpack/vue-color 无内置类型声明，补充最小声明（公告类型颜色选择用）
declare module "@ckpack/vue-color" {
  import type { DefineComponent } from "vue";

  export interface ColorState {
    hex: string;
    rgba: { r: number; g: number; b: number; a: number };
    hsv: { h: number; s: number; v: number; a: number };
  }

  export const Sketch: DefineComponent<{
    modelValue?: ColorState | { hex: string } | string;
    disableAlpha?: boolean;
    presetColors?: string[];
    width?: string | number;
  }>;
  export const ChromePicker: DefineComponent;
  export const PhotoshopPicker: DefineComponent;
  export const CompactPicker: DefineComponent;
  export const GrayscalePicker: DefineComponent;
  export const MaterialPicker: DefineComponent;
  export const SliderPicker: DefineComponent;
  export const TwitterPicker: DefineComponent;
  export const SwatchesPicker: DefineComponent;
  export const HueSlider: DefineComponent;
  export const tinycolor: (...args: unknown[]) => unknown;
}


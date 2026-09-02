<script setup lang="ts">
import {Toast, VCard, VPageHeader, VSpace, VSwitch, VTabbar} from "@halo-dev/components";
import {useQuery, useQueryClient} from "@tanstack/vue-query";
import {cloneDeep} from "lodash-es";
import {computed, nextTick, ref, watch} from "vue";
import SubmitButton from "@/components/button/SubmitButton.vue";
import RichTextEditorField from "@/components/common/RichTextEditorField.vue";
import {generalConfigApi} from "@/api";
import type {GeneralConfig, GeneralConfigSpec} from "@/types";

const queryClient = useQueryClient();

type BigGroup = "profile" | "preferences" | "pages" | "assets";

const GROUP_ITEMS: Array<{id: BigGroup; label: string; desc: string}> = [
  {id: "profile", label: "应用资料", desc: "应用信息 / 博主 / 社交 / 版权与声明"},
  {id: "preferences", label: "偏好设置", desc: "列表布局 / 封面位置 / 头像"},
  {id: "pages", label: "页面与排版", desc: "首页 / 图库 / 关于页"},
  {id: "assets", label: "资源与兜底", desc: "默认图 / 加载占位"},
];

const SUB_TABS: Record<BigGroup, Array<{id: string; label: string}>> = {
  profile: [
    {id: "appInfo", label: "应用信息"},
    {id: "blogger", label: "博主资料"},
    {id: "social", label: "社交信息"},
    {id: "copyright", label: "页脚版权"},
    {id: "disclaimers", label: "免责声明"},
    {id: "about", label: "关于与详情"},
  ],
  preferences: [
    {id: "layout", label: "列表与卡片"},
    {id: "avatar", label: "评论头像"},
  ],
  pages: [
    {id: "home", label: "首页"},
    {id: "gallery", label: "图库"},
    {id: "aboutPage", label: "关于页"},
  ],
  assets: [
    {id: "defaults", label: "默认图片"},
    {id: "loading", label: "加载占位"},
  ],
};

const bigGroup = ref<BigGroup>("profile");
const subTab = ref<string>("appInfo");

watch(bigGroup, (group) => {
  const first = SUB_TABS[group][0];
  if (first) {
    subTab.value = first.id;
  }
});

const subTabItems = computed(() => SUB_TABS[bigGroup.value]);

const {data: config, isLoading} = useQuery({
  queryKey: ["uni-halo:general-config"],
  queryFn: () => generalConfigApi.get(),
});

const formState = ref<GeneralConfig>(defaultConfig());

function defaultConfig(): GeneralConfig {
  return {
    metadata: {name: "general-config"},
    spec: defaultSpec(),
  };
}

function defaultSpec(): GeneralConfigSpec {
  return {
    profile: {
      appInfo: {name: "uni-halo", logo: "/plugins/uni-halo/assets/logo.png"},
      blogger: {nickname: "uni-halo", avatar: "", email: "", description: ""},
      social: {
        enabled: true,
        qq: "",
        wechat: "",
        weibo: "",
        email: "",
        blog: "",
        bilibili: "",
        juejin: "",
        csdn: "",
        gitee: "",
        github: "",
      },
      copyrightConfig: {enabled: true, content: "「 2022 uni-halo 丨 开源项目@小莫唐尼 」"},
      disclaimers: {enabled: true, content: ""},
      showAboutSystem: true,
      postDetailConfig: {
        showComment: true,
        copyrightEnabled: true,
        copyrightAuthor: "uni-halo",
        copyrightDesc:
          "使用《非商业性使用-相同方式共享 4.0 国际 (CC BY-NC-SA 4.0)》协议授权，文章来源于网上收集或者原创，若未在文章内说明的均为原创文章",
        copyrightViolation:
          "若侵害到您的权利，请您及时联系我，在收到通知后第一时间处理，邮箱：xxxx@xx.com",
      },
    },
    pages: {
      homeConfig: {
        pageTitle: "首页",
        useQuickNavigation: true,
        useCategory: true,
        bannerConfig: {
          enabled: true,
          showTitle: true,
          showIndicator: true,
          height: "400rpx",
          dotPosition: "right",
        },
      },
      galleryConfig: {pageTitle: "图库", useWaterfall: true},
      aboutConfig: {
        pageTitle: "关于博主",
        bgImageUrl: "/plugins/uni-halo/assets/uni_halo_profile_bg.jpg",
        waveImageUrl: "/plugins/uni-halo/assets/uni_halo_about_wave.gif",
      },
    },
    assets: {
      // 默认图片地址默认值：仅加载占位 gif 内置插件资源，其余留空由站长配置
      defaultImageUrl: "",
      defaultThumbnailUrl: "",
      defaultStaticThumbnailUrl: "",
      defaultAvatarUrl: "",
      loadingGifUrl: "/plugins/uni-halo/assets/uni_halo_img_lazyload.gif",
      loadingErrUrl: "",
      loadingEmptyUrl: "",
    },
    preferences: {
      homeListLayout: "h_row_col1",
      articleCardType: "lr_image_text",
      avatarRadius: true,
    },
  };
}

/** 回显/加载期间抑制脏标记；加载完成后开启变更追踪 */
let suppressDirty = true;

watch(
  () => config.value,
  (value) => {
    if (!value) {
      return;
    }
    const loaded = cloneDeep(value);
    loaded.spec = deepMerge(defaultSpec(), loaded.spec || {});
    loaded.metadata = {name: "general-config", ...loaded.metadata};
    suppressDirty = true;
    formState.value = loaded;
    dirty.value = false;
    nextTick(() => {
      suppressDirty = false;
    });
  },
  {immediate: true}
);

const dirty = ref(false);

watch(
  formState,
  () => {
    if (!suppressDirty) {
      dirty.value = true;
    }
  },
  {deep: true}
);

function deepMerge<T>(base: T, overlay: any): T {
  const out: any = cloneDeep(base);
  if (!overlay || typeof overlay !== "object") {
    return out;
  }
  for (const [key, value] of Object.entries(overlay)) {
    if (value === null || value === undefined) {
      continue;
    }
    if (value && typeof value === "object" && !Array.isArray(value) && out[key] && typeof out[key] === "object") {
      out[key] = deepMerge(out[key], value);
    } else {
      out[key] = cloneDeep(value);
    }
  }
  return out;
}

const handleSave = async () => {
  try {
    await generalConfigApi.save(formState.value);
    Toast.success("保存成功");
    dirty.value = false;
    queryClient.invalidateQueries({queryKey: ["uni-halo:general-config"]});
  } catch (error) {
    Toast.error((error as Error).message);
  }
};
</script>

<template>
  <VPageHeader title="UniHalo-通用配置">
    <template #actions>
      <div class=":uno: flex items-center">
        <VSpace>
          <SubmitButton type="secondary" :loading="isLoading" :disabled="!dirty" text="保存" @submit="handleSave" />
        </VSpace>
      </div>
    </template>
  </VPageHeader>

  <div class=":uno: flex flex-col gap-4 md:m-4 lg:flex-row">
    <!-- 左列表：切换大分区（VCard，参考公告管理左侧类型栏） -->
    <aside class=":uno: w-full flex-shrink-0 lg:w-64">
      <VCard :body-class="[':uno: !p-0']">
        <div class=":uno: flex items-center justify-between border-b border-gray-100 px-4 py-3">
          <span class=":uno: text-sm font-semibold text-gray-700">配置分类</span>
        </div>
        <div
          v-for="item in GROUP_ITEMS"
          :key="item.id"
          class=":uno: cursor-pointer px-4 py-3"
          :class="
            bigGroup === item.id
              ? ':uno: bg-gray-50 font-medium text-gray-900'
              : ':uno: text-gray-700 hover:bg-gray-50'
          "
          @click="bigGroup = item.id"
        >
          <div class=":uno: text-sm">{{ item.label }}</div>
          <div class=":uno: mt-0.5 text-xs text-gray-400">{{ item.desc }}</div>
        </div>
      </VCard>
    </aside>

    <!-- 右侧：VTabbar 子切换 + 表单内容 -->
    <div class=":uno: min-w-0 flex-1">
      <VCard :loading="isLoading">
        <template #header>
          <div class=":uno: p-2 pb-0">
            <VTabbar v-model:active-id="subTab" :items="subTabItems"/>
          </div>
        </template>

        <!-- 应用资料 → 应用信息 -->
        <template v-if="bigGroup === 'profile' && subTab === 'appInfo'">
          <FormKit v-model="formState.spec.profile.appInfo.name" name="appinfo_name" label="应用名称" type="text" help="小程序应用展示名称，如关于页标题等处使用" />
          <FormKit v-model="formState.spec.profile.appInfo.logo" name="appinfo_logo" label="应用图标" type="attachment" :accepts="['image/*']" help="小程序应用图标" />
        </template>

        <!-- 应用资料 → 博主资料 -->
        <template v-if="bigGroup === 'profile' && subTab === 'blogger'">
          <FormKit v-model="formState.spec.profile.blogger.nickname" name="blogger_nickname" label="昵称" type="text" />
          <FormKit v-model="formState.spec.profile.blogger.email" name="blogger_email" label="邮箱" type="text" />
          <FormKit v-model="formState.spec.profile.blogger.avatar" name="blogger_avatar" label="头像" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.profile.blogger.description" name="blogger_description" label="简介" type="textarea" />
        </template>

        <!-- 应用资料 → 社交信息 -->
        <template v-if="bigGroup === 'profile' && subTab === 'social'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">启用社交信息</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">开启后在小程序「关于」等页面展示社交入口</div>
            </div>
            <VSwitch v-model="formState.spec.profile.social.enabled" />
          </div>
          <div class=":uno: mt-3">
            <FormKit v-model="formState.spec.profile.social.qq" name="social_qq" label="QQ号" type="text" />
              <FormKit v-model="formState.spec.profile.social.wechat" name="social_wechat" label="微信号" type="text" />
              <FormKit v-model="formState.spec.profile.social.weibo" name="social_weibo" label="微博地址" type="text" />
              <FormKit v-model="formState.spec.profile.social.email" name="social_email" label="邮箱" type="text" />
              <FormKit v-model="formState.spec.profile.social.blog" name="social_blog" label="博客" type="text" />
              <FormKit v-model="formState.spec.profile.social.bilibili" name="social_bilibili" label="B站" type="text" />
              <FormKit v-model="formState.spec.profile.social.juejin" name="social_juejin" label="掘金" type="text" />
              <FormKit v-model="formState.spec.profile.social.csdn" name="social_csdn" label="CSDN" type="text" />
              <FormKit v-model="formState.spec.profile.social.gitee" name="social_gitee" label="Gitee" type="text" />
              <FormKit v-model="formState.spec.profile.social.github" name="social_github" label="GitHub" type="text" />
            </div>
        </template>

        <!-- 应用资料 → 页脚版权 -->
        <template v-if="bigGroup === 'profile' && subTab === 'copyright'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">显示版权信息</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">小程序页脚展示的版权文案</div>
            </div>
            <VSwitch v-model="formState.spec.profile.copyrightConfig.enabled" />
          </div>
          <div class=":uno: mt-3">
            <FormKit v-model="formState.spec.profile.copyrightConfig.content" name="copyright_content" label="版权内容" type="textarea" />
          </div>
        </template>

        <!-- 应用资料 → 免责声明（Halo 富文本） -->
        <template v-if="bigGroup === 'profile' && subTab === 'disclaimers'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">启用免责声明</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">在小程序「关于」等页面展示免责声明</div>
            </div>
            <VSwitch v-model="formState.spec.profile.disclaimers.enabled" />
          </div>
          <div class=":uno: mt-3">
            <RichTextEditorField v-model="formState.spec.profile.disclaimers.content" placeholder="输入免责声明内容，支持图文混排……留空使用默认模板" />
          </div>
        </template>

        <!-- 应用资料 → 关于与详情 -->
        <template v-if="bigGroup === 'profile' && subTab === 'about'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">关于项目</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">「关于」页展示开源项目介绍入口</div>
            </div>
            <VSwitch v-model="formState.spec.profile.showAboutSystem" />
          </div>

          <div class=":uno: mt-4 text-sm font-medium text-gray-700">文章详情</div>
          <div class=":uno: mt-2 rounded-lg bg-gray-50 p-4">
            <div class=":uno: flex items-center justify-between gap-4">
              <div>
                <div class=":uno: text-sm text-gray-700">显示评论相关</div>
                <div class=":uno: mt-0.5 text-xs text-gray-400">文章详情页是否展示评论相关功能</div>
              </div>
              <VSwitch v-model="formState.spec.profile.postDetailConfig.showComment" />
            </div>
            <div class=":uno: mt-3 flex items-center justify-between gap-4 border-t border-gray-100 pt-3">
              <div>
                <div class=":uno: text-sm text-gray-700">文章版权</div>
                <div class=":uno: mt-0.5 text-xs text-gray-400">文章底部是否展示版权声明</div>
              </div>
              <VSwitch v-model="formState.spec.profile.postDetailConfig.copyrightEnabled" />
            </div>
            <div class=":uno: mt-3">
              <FormKit v-model="formState.spec.profile.postDetailConfig.copyrightAuthor" name="post_copyright_author" label="文章版权作者" type="text" />
                <FormKit v-model="formState.spec.profile.postDetailConfig.copyrightDesc" name="post_copyright_desc" label="文章版权描述" type="textarea" />
                <FormKit v-model="formState.spec.profile.postDetailConfig.copyrightViolation" name="post_copyright_violation" label="文章侵权说明" type="textarea" />
              </div>
          </div>
        </template>

        <!-- 页面与排版 → 首页 -->
        <template v-if="bigGroup === 'pages' && subTab === 'home'">
          <FormKit v-model="formState.spec.pages.homeConfig.pageTitle" name="home_page_title" label="页面标题" type="text" />
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
            <div>
              <div class=":uno: text-sm text-gray-700">显示快捷导航</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">首页顶部快捷入口</div>
            </div>
            <VSwitch v-model="formState.spec.pages.homeConfig.useQuickNavigation" />
          </div>
          <div class=":uno: flex items-center justify-between gap-4 py-3">
            <div>
              <div class=":uno: text-sm text-gray-700">显示分类</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">首页是否展示精品文章分类</div>
            </div>
            <VSwitch v-model="formState.spec.pages.homeConfig.useCategory" />
          </div>

          <div class=":uno: mt-4 rounded-lg bg-gray-50 p-4">
            <div class=":uno: mb-2 text-sm font-medium text-gray-700">轮播图渲染参数</div>
            <p class=":uno: mb-3 text-xs text-gray-400">轮播数据请在「轮播管理」菜单维护，此处仅渲染参数。</p>
            <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
              <div class=":uno: text-sm text-gray-700">启用</div>
              <VSwitch v-model="formState.spec.pages.homeConfig.bannerConfig.enabled" />
            </div>
            <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
              <div class=":uno: text-sm text-gray-700">显示标题</div>
              <VSwitch v-model="formState.spec.pages.homeConfig.bannerConfig.showTitle" />
            </div>
            <div class=":uno: flex items-center justify-between gap-4 py-3">
              <div class=":uno: text-sm text-gray-700">显示指示器</div>
              <VSwitch v-model="formState.spec.pages.homeConfig.bannerConfig.showIndicator" />
            </div>
            <FormKit v-model="formState.spec.pages.homeConfig.bannerConfig.height" name="banner_height" label="轮播图高度" type="text" help="单位为 rpx" />
            <FormKit
              v-model="formState.spec.pages.homeConfig.bannerConfig.dotPosition"
              name="banner_dot_position"
              label="指示器位置"
              type="select"
              :options="[
                {label: '左', value: 'left'},
                {label: '右', value: 'right'},
                {label: '上', value: 'top'},
                {label: '下', value: 'bottom'},
              ]"
            />
          </div>
        </template>

        <!-- 页面与排版 → 图库 -->
        <template v-if="bigGroup === 'pages' && subTab === 'gallery'">
          <FormKit v-model="formState.spec.pages.galleryConfig.pageTitle" name="gallery_page_title" label="页面标题" type="text" />
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 py-3">
            <div>
              <div class=":uno: text-sm text-gray-700">使用瀑布流布局</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">关闭后使用列表布局</div>
            </div>
            <VSwitch v-model="formState.spec.pages.galleryConfig.useWaterfall" />
          </div>
        </template>

        <!-- 页面与排版 → 关于页 -->
        <template v-if="bigGroup === 'pages' && subTab === 'aboutPage'">
          <FormKit v-model="formState.spec.pages.aboutConfig.pageTitle" name="about_page_title" label="页面标题" type="text" />
          <FormKit v-model="formState.spec.pages.aboutConfig.bgImageUrl" name="about_bg_image" label="资料卡背景图" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.pages.aboutConfig.waveImageUrl" name="about_wave_image" label="资料卡波浪图" type="attachment" :accepts="['image/*']" />
        </template>

        <!-- 资源与兜底 → 默认图片 -->
        <template v-if="bigGroup === 'assets' && subTab === 'defaults'">
          <p class=":uno: mb-3 text-xs text-gray-400">
            以上为小程序端兜底/默认图资源，留空时由客户端内置回退处理；也可引用插件内置资源，如
            <code class=":uno: rounded bg-gray-100 px-1">/plugins/uni-halo/assets/uni_halo_profile_bg.jpg</code>
            （素材放插件 <code class=":uno: rounded bg-gray-100 px-1">src/main/resources/static/assets/</code>，同名替换即生效）。
          </p>
          <FormKit v-model="formState.spec.assets.defaultImageUrl" name="assets_default_image" label="默认图片" type="attachment" :accepts="['image/*', 'application/json']" help="支持静态图片与随机图 API" />
          <FormKit v-model="formState.spec.assets.defaultThumbnailUrl" name="assets_default_thumbnail" label="文章默认封面" type="attachment" :accepts="['image/*', 'application/json']" />
          <FormKit v-model="formState.spec.assets.defaultStaticThumbnailUrl" name="assets_default_static_thumbnail" label="文章默认静态封面" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.assets.defaultAvatarUrl" name="assets_default_avatar" label="默认头像" type="attachment" :accepts="['image/*', 'application/json']" />
        </template>

        <!-- 资源与兜底 → 加载占位 -->
        <template v-if="bigGroup === 'assets' && subTab === 'loading'">
          <FormKit v-model="formState.spec.assets.loadingGifUrl" name="assets_loading_gif" label="加载中的图片" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.assets.loadingErrUrl" name="assets_loading_err" label="加载失败图片" type="attachment" :accepts="['image/*']" />
          <FormKit v-model="formState.spec.assets.loadingEmptyUrl" name="assets_loading_empty" label="空图片（可选）" type="attachment" :accepts="['image/*']" />
        </template>

        <!-- 偏好设置 → 列表与卡片（L0 站点默认，用户可在小程序端偏好覆盖） -->
        <template v-if="bigGroup === 'preferences' && subTab === 'layout'">
          <p class=":uno: mb-3 text-xs text-gray-400">小程序端用户可在「我的-设置」中按个人偏好覆盖。</p>
          <FormKit
            v-model="formState.spec.preferences.homeListLayout"
            name="pref_home_layout"
            label="首页列表布局"
            type="select"
            :options="[
              {label: '一行一列（单列）', value: 'h_row_col1'},
              {label: '一行两列（双列）', value: 'h_row_col2'},
            ]"
            help="首页文章列表默认展示方式"
          />
          <FormKit
            v-model="formState.spec.preferences.articleCardType"
            name="pref_card_type"
            label="文章卡片排版（封面位置）"
            type="select"
            :options="[
              {label: '左图右文', value: 'lr_image_text'},
              {label: '左文右图', value: 'lr_text_image'},
              {label: '上图下文', value: 'tb_image_text'},
              {label: '上文下图', value: 'tb_text_image'},
              {label: '仅文字', value: 'only_text'},
            ]"
            help="文章卡片中封面图与文字的位置关系"
          />
        </template>

        <!-- 偏好设置 → 评论头像 -->
        <template v-if="bigGroup === 'preferences' && subTab === 'avatar'">
          <div class=":uno: flex items-center justify-between gap-4 border-b border-gray-100 pb-3">
            <div>
              <div class=":uno: text-sm text-gray-700">评论头像圆角</div>
              <div class=":uno: mt-0.5 text-xs text-gray-400">评论列表中头像是否以圆形展示</div>
            </div>
            <VSwitch v-model="formState.spec.preferences.avatarRadius" />
          </div>
        </template>
      </VCard>
    </div>
  </div>
</template>

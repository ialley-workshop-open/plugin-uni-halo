package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 通用配置（单例，metadata.name 固定为 general-config）。
 *
 * <p>承载原 setting.yaml 中迁出的「小程序通用内容与外观」配置（决策见
 * {@code .docs/config-system-v2-redesign.md} v2.3 / X1 单例形态），分三块：</p>
 * <ul>
 *   <li>{@link Profile} profile：应用资料——应用信息（名称/图标，原基本配置）、博主信息、
 *       社交信息、页脚版权、免责声明、关于项目与文章详情版权文案；</li>
 *   <li>{@link Pages} pages：首页（含轮播图渲染参数）、图库、关于页的视觉配置；</li>
 *   <li>{@link Assets} assets：全局默认图片/封面/头像/加载占位等兜底资源；</li>
 *   <li>{@link Preferences} preferences：站点级展示偏好默认（L0，经 getConfigs 顶层
 *       {@code preferences} 下发，与客户端 layout.home/cardType/isAvatarRadius 对齐）；</li>
 *   <li>{@link Love} love：恋爱模块（原 setting.featureConfig.loveConfig 剩余字段，
 *       2026-09-03 迁入）——总开关、恋爱页图片与恋爱故事/相册/清单模块入口开关；</li>
 *   <li>{@link Maintenance} maintenance：维护模式（2026-09-04 新增）——维护页标题/富文本说明
 *       与排期窗口，状态由服务端按时间窗口计算（设计见 {@code .docs/maintenance-config-design.md}）。</li>
 * </ul>
 *
 * <p>本模型是控制台「通用配置」页的写端事实源；小程序端仍通过 {@code getConfigs}
 * 读取（由服务端输出合成层把本模型映射回旧 shape，见 UniHaloServiceImpl）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "GeneralConfig", plural = "generalConfigs", singular = "generalConfig")
public class GeneralConfig extends AbstractExtension {

    private Spec spec;

    @Data
    public static class Spec {
        private Profile profile;
        private Pages pages;
        private Assets assets;
        private Preferences preferences;
        private Love love;
        /** 链接配置（2026-09-08 新增）：站长小程序展示信息，经 getConfigs 覆盖
         * pluginConfig.linksSubmitPlugin 对应键下发，供小程序端「申请信息」弹窗展示 */
        private LinkInfo linkInfo;
        /** 维护模式（2026-09-04 新增，见 {@code .docs/maintenance-config-design.md}） */
        private Maintenance maintenance;
    }

    /** 应用资料：应用信息（名称/图标，原基本配置 appInfo）+ 博主/社交/版权/免责/文章详情文案
     * （原 authorConfig + basicConfig 内容部分） */
    @Data
    public static class Profile {
        /** 应用信息（原 setting「基本配置」baseConfig.appInfo，2026-09-02 并入） */
        private AppInfo appInfo;
        private Blogger blogger;
        private Social social;
        private Copyright copyrightConfig;
        private Disclaimer disclaimers;
        /** 显示关于项目页面入口（【关于】导航页） */
        private Boolean showAboutSystem;
        private PostDetail postDetailConfig;
    }

    /** 应用信息（应用名称/图标；原 setting「基本配置」应用信息，2026-09-02 并入） */
    @Data
    public static class AppInfo {
        private String name;
        private String logo;
    }

    /** 博主信息（原 authorConfig.blogger） */
    @Data
    public static class Blogger {
        private String nickname;
        private String avatar;
        private String email;
        private String description;
    }

    /** 社交信息（原 authorConfig.social） */
    @Data
    public static class Social {
        private Boolean enabled;
        private String qq;
        private String wechat;
        private String weibo;
        private String email;
        private String blog;
        private String bilibili;
        private String juejin;
        private String csdn;
        private String gitee;
        private String github;
    }

    /** 页脚版权（原 basicConfig.copyrightConfig，显示于【关于】页面） */
    @Data
    public static class Copyright {
        private Boolean enabled;
        private String content;
    }

    /** 免责声明（原 basicConfig.disclaimers） */
    @Data
    public static class Disclaimer {
        private Boolean enabled;
        private String content;
    }

    /** 文章详情内容与版权文案（原 basicConfig.postDetailConfig） */
    @Data
    public static class PostDetail {
        /** 是否显示评论相关（优先级高于系统评论开关） */
        private Boolean showComment;
        /** 是否使用文章版权 */
        private Boolean copyrightEnabled;
        private String copyrightAuthor;
        private String copyrightDesc;
        private String copyrightViolation;
    }

    /** 页面与排版：首页/图库/分类页/瞬间页/关于页视觉（原 pageConfig；startConfig 已下线不在此） */
    @Data
    public static class Pages {
        private Home homeConfig;
        private Gallery galleryConfig;
        private About aboutConfig;
        /** 分类页（2026-09-08 新增：分类页标题，客户端 pageConfig.categoryConfig） */
        private CategoryPage categoryConfig;
        /** 瞬间页（2026-09-08 新增：瞬间页标题，客户端 pageConfig.momentConfig） */
        private MomentPage momentConfig;
    }

    /** 首页（原 pageConfig.homeConfig；2026-09-08 起轮播渲染参数下线由 app 端默认开启、
     * 快捷导航改为逐项可配置） */
    @Data
    public static class Home {
        /** 首页标题（客户端默认「首页」；2026-09-08 起控制台不再提供配置项） */
        private String pageTitle;
        /** 是否显示快捷导航 */
        private Boolean useQuickNavigation;
        /** 快捷导航项列表（2026-09-08 新增：每项可配置名称/排序/显示隐藏，排序=数组顺序） */
        private List<QuickNavigationItem> quickNavigation;
        /** 是否显示分类（精品文章分类） */
        private Boolean useCategory;
        /** 首页分类栏展示的分类引用（2026-09-08 新增：固定 3 个，数据在「分类管理」维护） */
        private List<CategoryItem> categories;
    }

    /** 快捷导航项（2026-09-08 新增；字段与客户端 uh-home-quick-nav 对齐，
     * bgColor 原 bgGlass、visible 原 show） */
    @Data
    public static class QuickNavigationItem {
        private String key;
        private String title;
        /** 图标颜色（十六进制色值，如 #03A9F4） */
        private String color;
        /** 背景色（原 bgGlass，rgba 半透明值） */
        private String bgColor;
        /** 图标字体前缀（如 uhemoji2-icon） */
        private String iconPrefix;
        /** 图标名（如 -mask） */
        private String icon;
        /** 跳转路径（小程序页面路径） */
        private String path;
        /** 是否显示（原 show） */
        private Boolean visible;
    }

    /** 首页分类栏选中引用（固定 3 个；name = Category.metadata.name） */
    @Data
    public static class CategoryItem {
        private String name;
        /** 分类名称（展示用冗余快照） */
        private String displayName;
    }

    /** 图库页（原 pageConfig.galleryConfig；2026-09-08 起瀑布流配置下线，app 端默认） */
    @Data
    public static class Gallery {
        private String pageTitle;
    }

    /** 分类页（2026-09-08 新增：客户端 pageConfig.categoryConfig） */
    @Data
    public static class CategoryPage {
        /** 分类页标题 */
        private String pageTitle;
    }

    /** 瞬间页（2026-09-08 新增：客户端 pageConfig.momentConfig） */
    @Data
    public static class MomentPage {
        /** 瞬间页标题 */
        private String pageTitle;
    }

    /** 关于页（原 pageConfig.aboutConfig） */
    @Data
    public static class About {
        private String pageTitle;
        /** 资料卡背景图 */
        private String bgImageUrl;
        /** 资料卡波浪图 */
        private String waveImageUrl;
    }

    /** 资源与兜底：加载占位图片（原 imagesConfig；2026-09-08 起默认图片/空图片配置已下线，
     * 客户端内置回退兜底，仅保留 loading/error 两图） */
    @Data
    public static class Assets {
        /** 加载中的图片 */
        private String loadingGifUrl;
        /** 加载失败图片 */
        private String loadingErrUrl;
    }

    /**
     * 站点级展示偏好默认（L0；客户端本地偏好可覆盖，值枚举与客户端
     * {@code hermes/preferences.md} §5 映射对齐，2026-09-08 起按页面分组）。
     * getConfigs 顶层 {@code preferences} 原样下发本结构，客户端 collectSiteDefaults
     * 按字段名映射到本地 layout.home/articles/archives。
     */
    @Data
    public static class Preferences {
        /** 首页列表布局（客户端 layout.home.listLayout）：single 单列 / double 双列 */
        private String homeListLayout;
        /** 首页卡片样式（客户端 layout.home.cardType）：image_top/image_right/
         * image_bottom/image_left */
        private String homeCardType;
        /** 文章列表页列表布局（客户端 layout.articles.listLayout）：single / double */
        private String articlesListLayout;
        /** 文章列表页卡片样式（客户端 layout.articles.cardType，沿用旧字段名兼容下发） */
        private String articleCardType;
        /** 文章归档页列表布局（客户端 layout.archives.listLayout）：single / double */
        private String archivesListLayout;
        /** 文章归档页卡片样式（客户端 layout.archives.cardType） */
        private String archivesCardType;
        /** 评论头像是否圆角（客户端 isAvatarRadius） */
        private Boolean avatarRadius;
    }

    /** 恋爱模块（原 setting.featureConfig.loveConfig 剩余字段，2026-09-03 迁入；
     * getConfigs 输出由装配器映射回旧顶层 loveConfig shape，客户端无感） */
    @Data
    public static class Love {
        /** 总开关：启用后小程序端「我的页面」导航出现恋爱入口 */
        private Boolean loveEnabled;
        /** 恋爱页图片配置 */
        private PageImages pageImages;
        /** 恋爱故事模块入口开关（数据在「恋爱管理-恋爱故事」维护） */
        private ModuleSwitch ourStory;
        /** 恋爱相册模块入口开关（数据在「恋爱管理-恋爱相册」维护） */
        private ModuleSwitch lovePhoto;
        /** 恋爱清单模块入口开关（数据在「恋爱管理-恋爱清单」维护） */
        private ModuleSwitch loveDaily;
    }

    /** 恋爱页图片（原 loveConfig.pageImages；2026-09-08 起仅保留背景图，
     * 波浪/爱心图配置下线，客户端内置回退） */
    @Data
    public static class PageImages {
        /** 背景图片 */
        private String bgImageUrl;
    }

    /**
     * 恋爱模块入口开关（enabled 是否在恋爱页展示入口；passwordEnabled/password/
     * passwordHash/passwordRemoved 为入口密码，与恋爱相册密码同一套 BCrypt 语义：
     * 管理端设置后，小程序端访问对应模块数据前需先经 {@code POST /love-modules/unlock}
     * 验证密码换取 HMAC token）。
     */
    @Data
    public static class ModuleSwitch {
        /** 是否在恋爱页展示该模块入口 */
        private Boolean enabled;
        /** 是否已设置密码（视图字段：控制台 GET 返回，由后端按 passwordHash 计算，保存时忽略） */
        private Boolean passwordEnabled;
        /** 新密码（仅管理端写请求携带；非空 = 重设并启用；GET 一律置空不回显） */
        private String password;
        /** 密码 BCrypt 哈希（仅内部存储；控制台 GET 与公开输出一律脱敏置空） */
        private String passwordHash;
        /** 是否清除密码（仅管理端写请求携带；true = 清除该入口密码并关闭验证） */
        private Boolean passwordRemoved;
    }

    /**
     * 链接配置：站长小程序展示信息（2026-09-08 新增）。字段与小程序端「申请信息」
     * 弹窗（客户端 {@code uh-links-mini-info} 组件）展示项对齐；getConfigs 输出时
     * 由装配器映射覆盖 {@code pluginConfig.linksSubmitPlugin} 对应键
     * （displayName→blogName / miniProgramCode→blogLogo / link→blogUrl /
     * description→blogDesc，其余同名），客户端读取 shape 不变。
     */
    @Data
    public static class LinkInfo {
        /** 小程序名称（linksSubmitPlugin.blogName） */
        private String displayName;
        /** 太阳码/小程序码图片（blogLogo） */
        private String miniProgramCode;
        /** 跳转地址（blogUrl） */
        private String link;
        /** 作者昵称（authorName） */
        private String authorName;
        /** 作者头像（avatar） */
        private String avatar;
        /** 作者网站（website） */
        private String website;
        /** 小程序描述（blogDesc） */
        private String description;
        /** 申请说明（applyRemark） */
        private String applyRemark;
    }

    /** 维护模式（2026-09-04 新增）。维护页展示内容与排期窗口；实际状态（未维护/预告/维护中）
     * 由服务端按 {@code enabled + startTime/endTime} 与当前时间计算（见
     * {@code utils/MaintenanceResolver}），「到点自动结束」为输出端判定、配置不回写。 */
    @Data
    public static class Maintenance {
        /** 安排开关：开启后按时间窗口即时生效（startTime 为空/已过 = 立即进入维护中） */
        private Boolean enabled;
        /** 维护页标题（默认「站点维护中」，scheduled/active 共用，客户端自行组织文案） */
        private String title;
        /** 维护说明（纯文本，textarea 编辑；小程序端维护页标题下方直接展示，留空则展示默认文案） */
        private String notice;
        /** 维护详情（富文本 HTML，RichTextEditorField 编辑；小程序端「维护详情」弹窗展示，
         * 留空则不展示详情入口） */
        private String description;
        /** 维护开始时间（RFC3339 UTC 字符串，如 2026-09-05T02:00:00Z）；空 = 立即维护 */
        private String startTime;
        /** 预计恢复时间（RFC3339 UTC 字符串）；空 = 持续至手动关闭；到点自动结束 */
        private String endTime;
    }
}

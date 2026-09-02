package cn.ialley.unihalo.scheme;

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
 *   <li>{@link Profile} profile：博主信息、社交信息、页脚版权、免责声明、关于项目与文章详情版权文案；</li>
 *   <li>{@link Pages} pages：首页（含轮播图渲染参数）、图库、关于页的视觉配置；</li>
 *   <li>{@link Assets} assets：全局默认图片/封面/头像/加载占位等兜底资源。</li>
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
    }

    /** 站点资料：博主/社交/版权/免责/文章详情文案（原 authorConfig + basicConfig 内容部分） */
    @Data
    public static class Profile {
        private Blogger blogger;
        private Social social;
        private Copyright copyrightConfig;
        private Disclaimer disclaimers;
        /** 显示关于项目页面入口（【关于】导航页） */
        private Boolean showAboutSystem;
        private PostDetail postDetailConfig;
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

    /** 页面与排版：首页/图库/关于页视觉（原 pageConfig；startConfig 已下线不在此） */
    @Data
    public static class Pages {
        private Home homeConfig;
        private Gallery galleryConfig;
        private About aboutConfig;
    }

    /** 首页（原 pageConfig.homeConfig） */
    @Data
    public static class Home {
        private String pageTitle;
        /** 是否显示快捷导航 */
        private Boolean useQuickNavigation;
        /** 是否显示分类（精品文章分类） */
        private Boolean useCategory;
        /** 轮播图渲染参数（数据在「轮播管理」菜单维护） */
        private Banner bannerConfig;
    }

    /** 轮播图渲染参数（原 homeConfig.bannerConfig） */
    @Data
    public static class Banner {
        private Boolean enabled;
        private Boolean showTitle;
        private Boolean showIndicator;
        /** 轮播图高度，单位 rpx */
        private String height;
        /** 指示器位置 left/right/top/bottom */
        private String dotPosition;
    }

    /** 图库（原 pageConfig.galleryConfig） */
    @Data
    public static class Gallery {
        private String pageTitle;
        /** 是否使用瀑布流，否则列表布局 */
        private Boolean useWaterfall;
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

    /** 资源与兜底：全局默认图片（原 imagesConfig，客户端 utils/url.ts 依赖） */
    @Data
    public static class Assets {
        private String defaultImageUrl;
        private String defaultThumbnailUrl;
        private String defaultStaticThumbnailUrl;
        private String defaultAvatarUrl;
        private String loadingGifUrl;
        private String loadingErrUrl;
        private String loadingEmptyUrl;
    }
}

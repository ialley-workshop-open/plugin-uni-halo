package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 首页轮播图（多条，归一化条目模型，设计见 .docs/banner-design.md 决策 D1/D2）。
 *
 * <p>所有条目统一数据格式：来源 {@code source} 由服务端按 {@code postId} 非空自动
 * 判定（post=文章快照 / custom=自定义），保存时强制覆盖、不手动设置（决策 D2）。
 * 文章模式仅快照 title/cover/date/authorName/authorAvatar/postId，content 恒为空
 * （决策 D3），点击直接跳转文章详情；自定义模式 content 为富文本 HTML（决策 D4）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "Banner", plural = "banners", singular = "banner")
public class Banner extends AbstractExtension {

    private BannerSpec spec;

    @Data
    public static class BannerSpec {

        /**
         * 标题（必填；文章模式为快照的文章标题）
         */
        private String title;

        /**
         * 封面图（Halo 附件 URL，必填；文章模式为快照的文章封面）
         */
        private String cover;

        /**
         * 展示日期（ISO-8601；文章模式快照文章发布时间，自定义模式手填可选，留空不展示）
         */
        private String date;

        /**
         * 作者昵称（文章模式快照作者 displayName，User 缺失时回退 owner 用户名；自定义模式手填可选）
         */
        private String authorName;

        /**
         * 作者头像（Halo 附件 URL，可选；文章模式快照作者头像）
         */
        private String authorAvatar;

        /**
         * 来源：post / custom，由服务端按 postId 非空自动判定（决策 D2）
         */
        private String source;

        /**
         * 内容（富文本 HTML；仅自定义模式，文章模式恒为空，决策 D3/D4）
         */
        private String content;

        /**
         * 备注（仅管理端可见，不对外展示）
         */
        private String remark;

        /**
         * 文章 id（Post 的 metadata.name；文章模式必填，自定义模式为空；小程序端据此跳转文章详情）
         */
        private String postId;

        /**
         * 外链地址（可选，自定义模式可填跳转链接）
         */
        private String link;

        /**
         * 排序，越大越靠前，默认 0
         */
        private Integer priority;
    }
}

package cn.ialley.unihalo.vo;

import lombok.Data;
import cn.ialley.unihalo.scheme.Banner;

/**
 * 轮播图公开列表视图（脱敏，决策 D5）。
 *
 * <p>不返回 content 富文本正文与 remark 备注（仅管理端可见），
 * 详情接口才返回完整 HTML。source/postId 供小程序端区分跳转行为。</p>
 *
 * @author 小莫唐尼
 */
@Data
public class BannerListVo {

    private String name;

    private String title;

    private String cover;

    /**
     * 展示日期（ISO-8601；文章模式为快照的文章发布时间）。
     */
    private String date;

    private String authorName;

    private String authorAvatar;

    /**
     * 来源：post / custom（服务端判定）。
     */
    private String source;

    /**
     * 文章 id（source=post 时非空，小程序端跳转文章详情用）。
     */
    private String postId;

    private String link;

    private Integer priority;

    public static BannerListVo from(Banner banner) {
        BannerListVo vo = new BannerListVo();
        vo.setName(banner.getMetadata().getName());
        var spec = banner.getSpec();
        if (spec != null) {
            vo.setTitle(spec.getTitle());
            vo.setCover(spec.getCover());
            vo.setDate(spec.getDate());
            vo.setAuthorName(spec.getAuthorName());
            vo.setAuthorAvatar(spec.getAuthorAvatar());
            vo.setSource(spec.getSource());
            vo.setPostId(spec.getPostId());
            vo.setLink(spec.getLink());
            vo.setPriority(spec.getPriority());
        }
        return vo;
    }
}

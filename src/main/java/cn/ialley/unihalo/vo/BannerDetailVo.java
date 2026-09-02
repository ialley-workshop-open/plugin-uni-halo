package cn.ialley.unihalo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import cn.ialley.unihalo.scheme.Banner;

/**
 * 轮播图公开详情视图（决策 D5）。
 *
 * <p>在列表脱敏字段基础上追加 content 富文本正文；仍不含 remark 备注。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BannerDetailVo extends BannerListVo {

    /**
     * 内容（富文本 HTML，仅自定义模式非空）。
     */
    private String content;

    public static BannerDetailVo from(Banner banner) {
        BannerDetailVo vo = new BannerDetailVo();
        BannerListVo base = BannerListVo.from(banner);
        vo.setName(base.getName());
        vo.setTitle(base.getTitle());
        vo.setCover(base.getCover());
        vo.setDate(base.getDate());
        vo.setAuthorName(base.getAuthorName());
        vo.setAuthorAvatar(base.getAuthorAvatar());
        vo.setSource(base.getSource());
        vo.setPostId(base.getPostId());
        vo.setLink(base.getLink());
        vo.setPriority(base.getPriority());
        if (banner.getSpec() != null) {
            vo.setContent(banner.getSpec().getContent());
        }
        return vo;
    }
}

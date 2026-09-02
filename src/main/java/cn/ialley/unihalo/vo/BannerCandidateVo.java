package cn.ialley.unihalo.vo;

import java.util.List;

import lombok.Data;

/**
 * 轮播图文章候选视图（管理端文章选择器数据源）。
 *
 * @author 小莫唐尼
 */
@Data
public class BannerCandidateVo {

    /**
     * Post 的 metadata.name（保存时写入 Banner.spec.postId）。
     */
    private String name;

    private String title;

    private String cover;

    /**
     * 文章发布时间（ISO-8601，展示用）。
     */
    private String publishTime;

    /**
     * 文章分类（name 列表，展示用）。
     */
    private List<String> categories;
}

package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * 跨插件查询引用（content.halo.run/v1alpha1 Post，Halo 核心扩展）。
 *
 * <p>仅用于按 GVK 查询候选数据（审核配置-选择文章），非本插件 Scheme，
 * 不要在 {@code UniHaloPlugin} 中注册。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1", kind = "Post",
        plural = "posts", singular = "post")
public class PostRef extends AbstractExtension {

    private PostRefSpec spec;

    @Data
    public static class PostRefSpec {
        private String title;
        private String cover;
        private String summary;
        private String publishTime;
        private Boolean publish;
        private List<String> categories;

        /**
         * 文章作者（Post.spec.owner，用户名；快照作者信息时据此查 User）
         */
        private String owner;
    }
}

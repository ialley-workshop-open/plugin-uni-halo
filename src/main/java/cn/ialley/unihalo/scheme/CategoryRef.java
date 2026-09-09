package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * 跨插件查询引用（content.halo.run/v1alpha1 Category，Halo 核心扩展）。
 *
 * <p>仅用于按 GVK 查询候选数据（审核配置-选择分类），非本插件 Scheme，
 * 不要在 {@code UniHaloPlugin} 中注册。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "content.halo.run", version = "v1alpha1", kind = "Category",
        plural = "categories", singular = "category")
public class CategoryRef extends AbstractExtension {

    private CategoryRefSpec spec;

    @Data
    public static class CategoryRefSpec {
        private String displayName;
        private String slug;
        /** 分类封面图（Halo Category.spec.cover，审核配置候选/精选分类快照展示用） */
        private String cover;
        /** 分类排序权重（Halo Category.spec.priority，越大越靠前） */
        private Integer priority;
    }
}

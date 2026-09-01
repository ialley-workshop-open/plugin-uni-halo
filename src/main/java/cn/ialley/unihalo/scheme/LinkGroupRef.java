package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * 跨插件查询引用（core.halo.run/v1alpha1 LinkGroup，由 plugin-links 注册）。
 *
 * <p>仅用于按 GVK 查询候选数据（审核配置-选择链接分组），非本插件 Scheme，
 * 不要在 {@code UniHaloPlugin} 中注册。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "core.halo.run", version = "v1alpha1", kind = "LinkGroup",
        plural = "linkgroups", singular = "linkgroup")
public class LinkGroupRef extends AbstractExtension {

    private LinkGroupRefSpec spec;

    @Data
    public static class LinkGroupRefSpec {
        private String displayName;
        private String description;
    }
}

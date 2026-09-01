package cn.ialley.unihalo.enums;

import run.halo.app.extension.GroupVersionKind;

/**
 * 审核配置候选数据类型（与审核模式覆盖的内容模块一一对应）。
 *
 * <p>GVK 映射（已核实）：文章/分类为 Halo 核心扩展；图库分组/链接分组由
 * plugin-photos / plugin-links 注册在 core.halo.run；瞬间由 plugin-moments
 * 注册在 moment.halo.run。</p>
 *
 * @author 小莫唐尼
 */
public enum CandidateType {

    /**
     * 文章（Post，content.halo.run）
     */
    post("content.halo.run", "v1alpha1", "Post"),

    /**
     * 分类（Category，content.halo.run）
     */
    category("content.halo.run", "v1alpha1", "Category"),

    /**
     * 图库分组（PhotoGroup，plugin-photos）
     */
    galleryGroup("core.halo.run", "v1alpha1", "PhotoGroup"),

    /**
     * 瞬间（Moment，plugin-moments）
     */
    moment("moment.halo.run", "v1alpha1", "Moment"),

    /**
     * 链接分组（LinkGroup，plugin-links）
     */
    linkGroup("core.halo.run", "v1alpha1", "LinkGroup");

    private final String group;
    private final String version;
    private final String kind;

    CandidateType(String group, String version, String kind) {
        this.group = group;
        this.version = version;
        this.kind = kind;
    }

    public GroupVersionKind groupVersionKind() {
        return new GroupVersionKind(group, version, kind);
    }
}

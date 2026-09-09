package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 审核配置（单例，metadata.name 固定为 audit-data-config）。
 *
 * <p>承载「审核模式模拟数据」的选中引用：审核模式开启后，小程序端仅展示
 * 此处挑选的站内真实数据。每条引用以 {@link AuditDataRef} 存储（name 为
 * 扩展 metadata.name，title/cover 等为展示字段快照，便于控制台直接渲染，
 * 小程序端二期仍按 name 过滤真实数据）。设置页 auditConfig 组的开关
 * auditModeEnabled 保留不动，本模型只管「展示哪些数据」；引用对象被删除后
 * 保存时服务端校验并剔除失效项。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "AuditDataConfig", plural = "auditDataConfigs", singular = "auditDataConfig")
public class AuditDataConfig extends AbstractExtension {

    private AuditDataConfigSpec spec;

    @Data
    public static class AuditDataConfigSpec {

        /**
         * 选中的文章 Post 引用列表（数组顺序即展示顺序）
         */
        private List<AuditDataRef> posts;

        /**
         * 选中的分类 Category 引用列表
         */
        private List<AuditDataRef> categories;

        /**
         * 选中的图库分组 PhotoGroup 引用列表（未分组照片不展示）
         */
        private List<AuditDataRef> galleryGroups;

        /**
         * 选中的瞬间 Moment 引用列表
         */
        private List<AuditDataRef> moments;

        /**
         * 选中的链接分组 LinkGroup 引用列表
         */
        private List<AuditDataRef> linkGroups;

        /**
         * 备注（如「微信审核用模拟数据」）
         */
        private String description;
    }

    /**
     * 被选中引用的快照（name 为扩展 metadata.name，必填且唯一；其余字段按类型
     * 选择性填充：文章=title/cover/subTitle(发布时间)/extra(分类)，分类=title/subTitle(slug)，
     * 图库分组=title/extra(照片数)，瞬间=title(摘要)/cover(首图)，链接分组=title）。
     */
    @Data
    public static class AuditDataRef {
        private String name;
        private String title;
        private String cover;
        private String subTitle;
        private String extra;
        /** 排序权重（分类=spec.priority，精选分类快照排序用；其余类型可空） */
        private Integer priority;
        /** 文章数（分类=status.postCount，缺失默认 0；app 端审核模式免请求复用） */
        private Integer postCount;
    }
}

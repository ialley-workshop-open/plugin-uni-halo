package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 友情链接-分组（对标 plugin-links 的 LinkGroup）。
 *
 * <p>链接通过 {@link MiniProgramLink} 的 spec.groupName 引用本模型 metadata.name；
 * 删除分组不影响已关联链接（公开接口中分组不存在时按未分组展示）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "MiniProgramLinkGroup", plural = "miniProgramLinkGroups",
        singular = "miniProgramLinkGroup")
public class MiniProgramLinkGroup extends AbstractExtension {

    private MiniProgramLinkGroupSpec spec;

    @Data
    public static class MiniProgramLinkGroupSpec {

        /**
         * 分组名称（必填），如「工具」「生活」
         */
        private String displayName;

        /**
         * 排序，越大越靠前，默认 0
         */
        private Integer priority;
    }
}

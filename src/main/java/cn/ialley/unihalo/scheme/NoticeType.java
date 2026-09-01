package cn.ialley.unihalo.scheme;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 公告类型（决策 D9）。
 *
 * <p>公告通过 spec.typeName 引用本模型的 metadata.name；删除类型不影响已关联公告
 * （公开接口中类型不存在时忽略标签展示，决策 D11）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "NoticeType", plural = "noticeTypes", singular = "noticeType")
public class NoticeType extends AbstractExtension {

    private NoticeTypeSpec spec;

    @Data
    public static class NoticeTypeSpec {

        /**
         * 类型名称（必填），如「活动」「维护」
         */
        private String displayName;

        /**
         * 标签颜色（hex，如 #10B981），app 端渲染标签用
         */
        private String color;

        /**
         * 排序，越大越靠前，默认 0
         */
        private Integer priority;
    }
}

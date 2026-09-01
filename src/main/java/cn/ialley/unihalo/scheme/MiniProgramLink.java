package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 友情链接-小程序链接（设计决策 D1/D3/D4/D5/D13）。
 *
 * <p>用于展示管理小程序入口：太阳码为小程序码图片（必填），小程序地址与作者网站
 * 非必填；类型为自由文本（公开列表分组依据，D3），标签为自由多标签（D4）；
 * visible 为可见性开关（默认 true，公开接口仅返回可见项，D2/D8）；website 归属
 * 作者信息（D13）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "MiniProgramLink", plural = "miniProgramLinks", singular = "miniProgramLink")
public class MiniProgramLink extends AbstractExtension {

    private MiniProgramLinkSpec spec;

    @Data
    public static class MiniProgramLinkSpec {

        /**
         * 小程序名称（必填）
         */
        private String displayName;

        /**
         * 太阳码（小程序码图片 URL，必填）
         */
        private String miniProgramCode;

        /**
         * 小程序地址（非必填，跳转链接）
         */
        private String link;

        /**
         * 作者昵称
         */
        private String authorName;

        /**
         * 作者头像（图片 URL）
         */
        private String avatar;

        /**
         * 作者网站（归属作者信息，非必填，D13）
         */
        private String website;

        /**
         * 分组（引用 {@link MiniProgramLinkGroup} 的 metadata.name；为空=未分组）
         */
        private String groupName;

        /**
         * 描述
         */
        private String description;

        /**
         * 预览图（多图，图片 URL 列表）
         */
        private List<String> screenshots;

        /**
         * 可见性：true 公开显示（默认）/ false 隐藏（仅控制台可见）
         */
        private Boolean visible;

        /**
         * 来源（根据操作自动设置，不手动填写，D26）：manual 手动添加 /
         * submitted 自助申请审核通过
         */
        private String source;

        /**
         * 排序，越大越靠前，默认 0
         */
        private Integer priority;
    }
}

package cn.ialley.unihalo.scheme;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import static cn.ialley.unihalo.constants.Constants.BASIC_DOMAIN_NAME;
import static cn.ialley.unihalo.constants.Constants.PLUGIN_API_VERSION;

/**
 * 友情链接-小程序链接申请单（设计决策 D1/D6/D7/D10）。
 *
 * <p>公开提交的自助申请：业务字段与 {@link MiniProgramLink} 对齐，另含 email
 * （非必填，填写则审核结果邮件通知）、status 三态（PENDING/APPROVED/REJECTED）、
 * reason（拒绝时必填）、submittedAt/reviewedAt（服务端自动记录）。审核通过后
 * 自动生成 MiniProgramLink 并以 spec.linkName 关联（幂等，D7）。</p>
 *
 * @author 小莫唐尼
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = BASIC_DOMAIN_NAME, version = PLUGIN_API_VERSION,
        kind = "MiniProgramLinkSubmission", plural = "miniProgramLinkSubmissions",
        singular = "miniProgramLinkSubmission")
public class MiniProgramLinkSubmission extends AbstractExtension {

    private MiniProgramLinkSubmissionSpec spec;

    @Data
    public static class MiniProgramLinkSubmissionSpec {

        /**
         * 申请的小程序名称（必填）
         */
        private String displayName;

        /**
         * 太阳码（小程序码图片 URL，必填）
         */
        private String miniProgramCode;

        /**
         * 小程序地址（非必填）
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
         * 作者网站（归属作者信息，非必填）
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
         * 申请说明（小程序端提交时填写，方便管理员了解申请意图）
         */
        private String applyRemark;

        /**
         * 预览图（多图，图片 URL 列表）
         */
        private List<String> screenshots;

        /**
         * 申请人邮箱（非必填；填写则审核结果邮件通知，填写时校验格式）
         */
        private String email;

        /**
         * 审核状态：PENDING（默认）/ APPROVED / REJECTED
         */
        private String status;

        /**
         * 审核结果说明（拒绝时必填，通过时可填备注）
         */
        private String reason;

        /**
         * 提交时间（服务端自动记录，ISO-8601）
         */
        private String submittedAt;

        /**
         * 审核时间（服务端自动记录，ISO-8601）
         */
        private String reviewedAt;

        /**
         * 审核通过后生成的链接 name（关联 MiniProgramLink.metadata.name，幂等判断用）
         */
        private String linkName;
    }
}

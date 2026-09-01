package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 友情链接-小程序链接申请单服务（决策 D6/D7/D10）。
 *
 * @author 小莫唐尼
 */
public interface MiniProgramLinkSubmissionService {

    /**
     * 分页列表；status 非空按审核状态过滤，keyword 模糊匹配名称/作者/邮箱；
     * sort 支持 submittedAt（申请时间，默认）/ reviewedAt（审核时间）/ status（状态），
     * 均倒序；排序字段需已注册索引（D20/D23）。
     */
    Mono<ListResult<MiniProgramLinkSubmission>> list(String status, String keyword,
            String sort, int page, int size);

    /**
     * 公开提交（displayName、miniProgramCode 必填，email 非空时校验格式）；
     * 服务端强制 status=PENDING 并记录 submittedAt。
     */
    Mono<MiniProgramLinkSubmission> submit(MiniProgramLinkSubmission submission);

    /**
     * 审核通过：自动生成 {@link cn.ialley.unihalo.scheme.MiniProgramLink}
     * （visible=true，priority=0，D7）并回写 spec.linkName；幂等：已 APPROVED 且
     * 已有 linkName 时不重复创建。reason 可选（通过备注）；groupName 非 null 时
     * 调整申请与生成链接的分组（null 表示保持不变，"" 表示未分组，D23）。
     */
    Mono<MiniProgramLinkSubmission> approve(String name, String reason, String groupName);

    /**
     * 审核拒绝：reason 必填，记录 reviewedAt；groupName 非 null 时同步调整分组。
     */
    Mono<MiniProgramLinkSubmission> reject(String name, String reason, String groupName);

    Mono<Void> delete(String name);
}

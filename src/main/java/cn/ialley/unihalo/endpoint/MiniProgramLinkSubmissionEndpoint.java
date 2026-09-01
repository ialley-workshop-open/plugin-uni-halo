package cn.ialley.unihalo.endpoint;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import cn.ialley.unihalo.services.MiniProgramLinkSubmissionService;
import cn.ialley.unihalo.utils.EmailService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 友情链接-小程序链接申请单接口（控制台，需登录）。
 *
 * <p>审核通过/拒绝后触发邮件通知（若开关开启且申请人填了邮箱，D11）。</p>
 *
 * @author 小莫唐尼
 */
@Slf4j
@Component
public class MiniProgramLinkSubmissionEndpoint implements CustomEndpoint {

    private final MiniProgramLinkSubmissionService submissionService;
    private final EmailService emailService;

    public MiniProgramLinkSubmissionEndpoint(
            MiniProgramLinkSubmissionService submissionService, EmailService emailService) {
        this.submissionService = submissionService;
        this.emailService = emailService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.MINI_PROGRAM_LINK_SUBMISSION_API_BASE_PATH, this::listSubmissions)
                .POST(Constants.MINI_PROGRAM_LINK_SUBMISSION_API_BASE_PATH, this::createSubmission)
                .POST(Constants.MINI_PROGRAM_LINK_SUBMISSION_API_BASE_PATH
                        + "/{name}/approve", this::approveSubmission)
                .POST(Constants.MINI_PROGRAM_LINK_SUBMISSION_API_BASE_PATH
                        + "/{name}/reject", this::rejectSubmission)
                .DELETE(Constants.MINI_PROGRAM_LINK_SUBMISSION_API_BASE_PATH
                        + "/{name}", this::deleteSubmission)
                .build();
    }

    /**
     * 控制台新增申请（测试用）：复用公开提交逻辑，强制 PENDING 并记录 submittedAt，
     * 不走 submissionEnabled 开关（控制台操作不受公开开关影响）。
     */
    private Mono<ServerResponse> createSubmission(ServerRequest request) {
        return request.bodyToMono(MiniProgramLinkSubmission.class)
                .flatMap(submissionService::submit)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> listSubmissions(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String status = request.queryParam("status").orElse("").trim();
        String keyword = request.queryParam("keyword").orElse("").trim();
        String sort = request.queryParam("sort").orElse("").trim();
        return submissionService.list(blankToNull(status), blankToNull(keyword),
                        blankToNull(sort), page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> approveSubmission(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(ReviewRequest.class)
                .defaultIfEmpty(new ReviewRequest(null, null))
                .flatMap(review -> submissionService.approve(name,
                        blankToNull(review.reason()), review.groupName()))
                .flatMap(submission -> {
                    emailService.sendAuditEmail(submission).subscribe(
                            null, e -> log.warn("审核邮件发送异常（不影响审核结果）", e));
                    return ServerResponse.ok().bodyValue(submission);
                })
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> rejectSubmission(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(ReviewRequest.class)
                .defaultIfEmpty(new ReviewRequest(null, null))
                .flatMap(review -> submissionService.reject(name,
                        blankToNull(review.reason()), review.groupName()))
                .flatMap(submission -> {
                    emailService.sendAuditEmail(submission).subscribe(
                            null, e -> log.warn("审核邮件发送异常（不影响审核结果）", e));
                    return ServerResponse.ok().bodyValue(submission);
                })
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteSubmission(ServerRequest request) {
        return submissionService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    /**
     * 审核请求体：reason 为审核说明（拒绝时必填）；groupName 非 null 时调整分组
     * （null 保持不变，"" 表示未分组，D23）。
     */
    public record ReviewRequest(String reason, String groupName) {
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }
}

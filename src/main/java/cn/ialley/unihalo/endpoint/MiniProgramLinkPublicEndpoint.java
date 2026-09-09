package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.captcha.CaptchaScope;
import cn.ialley.unihalo.captcha.CaptchaService;
import cn.ialley.unihalo.captcha.CaptchaValidationException;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.MiniProgramLink;
import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import cn.ialley.unihalo.services.MiniProgramLinkService;
import cn.ialley.unihalo.services.MiniProgramLinkSubmissionService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import cn.ialley.unihalo.utils.SettingGroupResolver;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * 友情链接-小程序链接公开接口（app 端，匿名可访问）。
 *
 * <p>仅返回可见（visible=true）的链接（D8）；支持 grouped=true 按类型分组返回
 * （D3）；公开提交申请（D6），提交接口校验必填项并落库为待审核；设置
 * linkConfig.submissionEnabled 关闭时提交申请返回提示。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class MiniProgramLinkPublicEndpoint implements CustomEndpoint {

    private static final String SETTING_GROUP_LINK_CONFIG = "linkConfig";
    private static final String SETTING_KEY_SUBMISSION_ENABLED = "submissionEnabled";

    private final MiniProgramLinkService miniProgramLinkService;
    private final MiniProgramLinkSubmissionService miniProgramLinkSubmissionService;
    private final ReactiveSettingFetcher settingFetcher;
    private final CaptchaService captchaService;

    public MiniProgramLinkPublicEndpoint(MiniProgramLinkService miniProgramLinkService,
            MiniProgramLinkSubmissionService miniProgramLinkSubmissionService,
            ReactiveSettingFetcher settingFetcher,
            CaptchaService captchaService) {
        this.miniProgramLinkService = miniProgramLinkService;
        this.miniProgramLinkSubmissionService = miniProgramLinkSubmissionService;
        this.settingFetcher = settingFetcher;
        this.captchaService = captchaService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.MINI_PROGRAM_LINK_API_BASE_PATH + "/types", this::listTypes)
                .POST(Constants.MINI_PROGRAM_LINK_API_BASE_PATH + "/submissions",
                        this::submitApplication)
                .GET(Constants.MINI_PROGRAM_LINK_API_BASE_PATH, this::listLinks)
                .GET(Constants.MINI_PROGRAM_LINK_API_BASE_PATH + "/{name}", this::getLink)
                .build();
    }

    private Mono<ServerResponse> listTypes(ServerRequest request) {
        return miniProgramLinkService.listGroups(true)
                .flatMap(groups -> ServerResponse.ok().bodyValue(groups));
    }

    private Mono<ServerResponse> submitApplication(ServerRequest request) {
        return captchaService.requireValid(request, CaptchaScope.LINK_SUBMISSION)
                .then(SettingGroupResolver.group(settingFetcher, "featureConfig",
                                SETTING_GROUP_LINK_CONFIG)
                        .defaultIfEmpty(JsonNodeFactory.instance.objectNode())
                        .flatMap(config -> {
                            if (!config.path(SETTING_KEY_SUBMISSION_ENABLED).asBoolean(true)) {
                                return Mono.error(new IllegalArgumentException("暂未开放提交申请"));
                            }
                            return request.bodyToMono(MiniProgramLinkSubmission.class)
                                    .flatMap(miniProgramLinkSubmissionService::submit)
                                    .flatMap(created -> ServerResponse.ok().bodyValue(created));
                        })
                        .onErrorResume(IllegalArgumentException.class,
                                e -> ServerResponse.badRequest()
                                        .bodyValue(Map.of("message", e.getMessage()))))
                .onErrorResume(CaptchaValidationException.class, this::captchaForbidden);
    }

    /**
     * 验证码校验失败：403 + 附新验证码（前端即时刷新重试）。
     */
    private Mono<ServerResponse> captchaForbidden(CaptchaValidationException e) {
        return captchaService.generate()
                .flatMap(captcha -> ServerResponse.status(HttpStatus.FORBIDDEN)
                        .bodyValue(Map.of("message", e.getMessage(), "captcha", captcha)));
    }

    private Mono<ServerResponse> listLinks(ServerRequest request) {
        boolean grouped = request.queryParam("grouped")
                .map(Boolean::parseBoolean).orElse(false);
        String keyword = request.queryParam("keyword").orElse("").trim();
        if (grouped) {
            return miniProgramLinkService.listGrouped(true, blankToNull(keyword))
                    .flatMap(groups -> ServerResponse.ok().bodyValue(groups));
        }
        int page = queryPage(request);
        int size = querySize(request);
        String group = request.queryParam("group").orElse("").trim();
        // 公开读路径专用查询：visible=true 且排除删除中对象（决策 D7）
        return miniProgramLinkService.listPublic(blankToNull(group),
                        blankToNull(keyword), page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> getLink(ServerRequest request) {
        String name = request.pathVariable("name");
        return miniProgramLinkService.getByName(name)
                // 公开详情：仅可见且非删除中（决策 D7）
                .filter(link -> (link.getMetadata() == null
                        || link.getMetadata().getDeletionTimestamp() == null)
                        && link.getSpec() != null
                        && Boolean.TRUE.equals(link.getSpec().getVisible()))
                .flatMap(link -> ServerResponse.ok().bodyValue(link))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
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

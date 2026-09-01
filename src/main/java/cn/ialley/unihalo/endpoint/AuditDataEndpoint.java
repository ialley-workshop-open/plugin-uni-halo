package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.enums.CandidateType;
import cn.ialley.unihalo.scheme.AuditDataConfig;
import cn.ialley.unihalo.services.AuditDataService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 审核配置接口（控制台，需登录）。
 *
 * <p>单例读写 + 候选数据查询（选择器数据源，按类型映射外部扩展 GVK）。
 * 公开只读接口见 {@link AuditDataPublicEndpoint}（匿名，联动审核模式开关）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class AuditDataEndpoint implements CustomEndpoint {

    private final AuditDataService auditDataService;

    public AuditDataEndpoint(AuditDataService auditDataService) {
        this.auditDataService = auditDataService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.AUDIT_DATA_API_BASE_PATH, this::getAuditData)
                .PUT(Constants.AUDIT_DATA_API_BASE_PATH, this::saveAuditData)
                .GET(Constants.AUDIT_DATA_API_BASE_PATH + "/candidates", this::listCandidates)
                .build();
    }

    private Mono<ServerResponse> getAuditData(ServerRequest request) {
        return auditDataService.getDetail()
                .flatMap(detail -> ServerResponse.ok().bodyValue(detail));
    }

    private Mono<ServerResponse> saveAuditData(ServerRequest request) {
        return request.bodyToMono(AuditDataConfig.class)
                .flatMap(auditDataService::save)
                .flatMap(saved -> ServerResponse.ok().bodyValue(saved))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> listCandidates(ServerRequest request) {
        String typeName = request.queryParam("type").orElse("").trim();
        String keyword = request.queryParam("keyword").orElse("").trim();
        int page = queryPage(request);
        int size = querySize(request);
        CandidateType type;
        try {
            type = CandidateType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return ServerResponse.badRequest()
                    .bodyValue(Map.of("message", "type 参数不合法: " + typeName));
        }
        return auditDataService.listCandidates(type, keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }
}

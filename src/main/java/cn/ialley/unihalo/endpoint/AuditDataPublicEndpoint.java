package cn.ialley.unihalo.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.enums.CandidateType;
import cn.ialley.unihalo.services.AuditDataService;
import cn.ialley.unihalo.services.GeneralConfigService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 审核配置公开接口（app 端/小程序端，匿名可访问）。
 *
 * <p>联动通用配置「应用设置-审核模式」开关（2026-09-11 起由设置页 safetyConfig.
 * auditConfig 迁入 GeneralConfig.spec.auditMode.enabled）：enabled=true 时返回剔除
 * 失效引用后的选中列表（小程序端据此过滤真实数据展示），并附带 {@code categoryDetails}
 * （分类完整快照：name/title/cover/priority/postCount，剔除失效、按配置顺序），供 app 端
 * 审核模式下分类页免请求直接映射 ICategory；开关关闭时返回 {@code {enabled:false}}。
 * 匿名放行由 role-anonymous.yaml 的全局规则（api.unihalo.ialley.cn 全部资源）覆盖。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class AuditDataPublicEndpoint implements CustomEndpoint {

    private final AuditDataService auditDataService;
    private final GeneralConfigService generalConfigService;

    public AuditDataPublicEndpoint(AuditDataService auditDataService,
            GeneralConfigService generalConfigService) {
        this.auditDataService = auditDataService;
        this.generalConfigService = generalConfigService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.AUDIT_DATA_API_BASE_PATH, this::getAuditData)
                .build();
    }

    private Mono<ServerResponse> getAuditData(ServerRequest request) {
        return generalConfigService.get().flatMap(config -> {
            boolean enabled = config.getSpec() != null
                    && config.getSpec().getAuditMode() != null
                    && Boolean.TRUE.equals(config.getSpec().getAuditMode().getEnabled());
            Map<String, Object> body = new HashMap<>();
            body.put("enabled", enabled);
            if (enabled) {
                return auditDataService.getEffective()
                        .zipWith(auditDataService.getDetail())
                        .flatMap(tuple -> {
                            body.put("spec",
                                    tuple.getT1().getSpec() == null ? Map.of()
                                            : tuple.getT1().getSpec());
                            // 分类完整快照（剔除失效、按配置顺序，app 端审核模式免请求映射 ICategory）
                            var categoryDetails = tuple.getT2().selections()
                                    .get(CandidateType.category);
                            if (categoryDetails != null && !categoryDetails.isEmpty()) {
                                body.put("categoryDetails", categoryDetails);
                            }
                            return ServerResponse.ok().bodyValue(body);
                        });
            }
            return ServerResponse.ok().bodyValue(body);
        });
    }
}

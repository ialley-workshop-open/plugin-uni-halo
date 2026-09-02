package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.GeneralConfig;
import cn.ialley.unihalo.services.GeneralConfigService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 通用配置接口（控制台，需登录）。
 *
 * <p>单例读写：GET 不存在时返回默认结构（含历史 ConfigMap 旧值合并），
 * PUT 写入前做非空合并并保存（见 {@link GeneralConfigService}）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class GeneralConfigEndpoint implements CustomEndpoint {

    private final GeneralConfigService generalConfigService;

    public GeneralConfigEndpoint(GeneralConfigService generalConfigService) {
        this.generalConfigService = generalConfigService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.GENERAL_CONFIG_API_BASE_PATH, this::getGeneralConfig)
                .PUT(Constants.GENERAL_CONFIG_API_BASE_PATH, this::saveGeneralConfig)
                .build();
    }

    private Mono<ServerResponse> getGeneralConfig(ServerRequest request) {
        return generalConfigService.get()
                .flatMap(config -> ServerResponse.ok().bodyValue(config));
    }

    private Mono<ServerResponse> saveGeneralConfig(ServerRequest request) {
        return request.bodyToMono(GeneralConfig.class)
                .flatMap(generalConfigService::save)
                .flatMap(saved -> ServerResponse.ok().bodyValue(saved))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }
}

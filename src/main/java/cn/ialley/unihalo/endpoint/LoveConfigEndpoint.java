package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.LoveConfig;
import cn.ialley.unihalo.services.LoveConfigService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 恋爱配置接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class LoveConfigEndpoint implements CustomEndpoint {

    private final LoveConfigService loveConfigService;

    public LoveConfigEndpoint(LoveConfigService loveConfigService) {
        this.loveConfigService = loveConfigService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.LOVE_CONFIG_API_BASE_PATH, this::getLoveConfig)
                .PUT(Constants.LOVE_CONFIG_API_BASE_PATH, this::saveLoveConfig)
                .build();
    }

    private Mono<ServerResponse> getLoveConfig(ServerRequest request) {
        return loveConfigService.get()
                .flatMap(config -> ServerResponse.ok().bodyValue(config));
    }

    private Mono<ServerResponse> saveLoveConfig(ServerRequest request) {
        return request.bodyToMono(LoveConfig.class)
                .flatMap(loveConfigService::save)
                .flatMap(saved -> ServerResponse.ok().bodyValue(saved))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }
}

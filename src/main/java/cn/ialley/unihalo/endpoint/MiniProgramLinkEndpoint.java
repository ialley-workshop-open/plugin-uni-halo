package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.MiniProgramLink;
import cn.ialley.unihalo.services.MiniProgramLinkService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 友情链接-小程序链接接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class MiniProgramLinkEndpoint implements CustomEndpoint {

    private final MiniProgramLinkService miniProgramLinkService;

    public MiniProgramLinkEndpoint(MiniProgramLinkService miniProgramLinkService) {
        this.miniProgramLinkService = miniProgramLinkService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.MINI_PROGRAM_LINK_API_BASE_PATH, this::listLinks)
                .POST(Constants.MINI_PROGRAM_LINK_API_BASE_PATH, this::createLink)
                .PUT(Constants.MINI_PROGRAM_LINK_API_BASE_PATH + "/{name}", this::updateLink)
                .DELETE(Constants.MINI_PROGRAM_LINK_API_BASE_PATH + "/{name}", this::deleteLink)
                .build();
    }

    private Mono<ServerResponse> listLinks(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String group = request.queryParam("group").orElse("").trim();
        String keyword = request.queryParam("keyword").orElse("").trim();
        Boolean visible = request.queryParam("visible")
                .map(Boolean::parseBoolean).orElse(null);
        return miniProgramLinkService.list(blankToNull(group), visible,
                        blankToNull(keyword), page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createLink(ServerRequest request) {
        return request.bodyToMono(MiniProgramLink.class)
                .flatMap(miniProgramLinkService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateLink(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(MiniProgramLink.class)
                .doOnNext(link -> {
                    if (link.getMetadata() == null) {
                        link.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    link.getMetadata().setName(name);
                })
                .flatMap(miniProgramLinkService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteLink(ServerRequest request) {
        return miniProgramLinkService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
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

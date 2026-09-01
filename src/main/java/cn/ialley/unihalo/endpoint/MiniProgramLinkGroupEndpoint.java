package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.MiniProgramLinkGroup;
import cn.ialley.unihalo.services.MiniProgramLinkGroupService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 友情链接-分组接口（控制台，需登录；对标 plugin-links LinkGroup）。
 *
 * @author 小莫唐尼
 */
@Component
public class MiniProgramLinkGroupEndpoint implements CustomEndpoint {

    private final MiniProgramLinkGroupService miniProgramLinkGroupService;

    public MiniProgramLinkGroupEndpoint(MiniProgramLinkGroupService miniProgramLinkGroupService) {
        this.miniProgramLinkGroupService = miniProgramLinkGroupService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.MINI_PROGRAM_LINK_GROUP_API_BASE_PATH, this::listGroups)
                .POST(Constants.MINI_PROGRAM_LINK_GROUP_API_BASE_PATH, this::createGroup)
                .PUT(Constants.MINI_PROGRAM_LINK_GROUP_API_BASE_PATH + "/{name}",
                        this::updateGroup)
                .DELETE(Constants.MINI_PROGRAM_LINK_GROUP_API_BASE_PATH + "/{name}",
                        this::deleteGroup)
                .build();
    }

    private Mono<ServerResponse> listGroups(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String keyword = request.queryParam("keyword").orElse("").trim();
        return miniProgramLinkGroupService.list(blankToNull(keyword), page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createGroup(ServerRequest request) {
        return request.bodyToMono(MiniProgramLinkGroup.class)
                .flatMap(miniProgramLinkGroupService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateGroup(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(MiniProgramLinkGroup.class)
                .doOnNext(group -> {
                    if (group.getMetadata() == null) {
                        group.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    group.getMetadata().setName(name);
                })
                .flatMap(miniProgramLinkGroupService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteGroup(ServerRequest request) {
        return miniProgramLinkGroupService.delete(request.pathVariable("name"))
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

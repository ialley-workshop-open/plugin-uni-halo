package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.NoticeType;
import cn.ialley.unihalo.services.NoticeTypeService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 公告类型接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class NoticeTypeEndpoint implements CustomEndpoint {

    private final NoticeTypeService noticeTypeService;

    public NoticeTypeEndpoint(NoticeTypeService noticeTypeService) {
        this.noticeTypeService = noticeTypeService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.NOTICE_TYPE_API_BASE_PATH, this::listNoticeTypes)
                .POST(Constants.NOTICE_TYPE_API_BASE_PATH, this::createNoticeType)
                .PUT(Constants.NOTICE_TYPE_API_BASE_PATH + "/{name}", this::updateNoticeType)
                .DELETE(Constants.NOTICE_TYPE_API_BASE_PATH + "/{name}", this::deleteNoticeType)
                .build();
    }

    private Mono<ServerResponse> listNoticeTypes(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String keyword = request.queryParam("keyword").orElse("").trim();
        return noticeTypeService.list(keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createNoticeType(ServerRequest request) {
        return request.bodyToMono(NoticeType.class)
                .flatMap(noticeTypeService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateNoticeType(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(NoticeType.class)
                .doOnNext(noticeType -> {
                    if (noticeType.getMetadata() == null) {
                        noticeType.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    noticeType.getMetadata().setName(name);
                })
                .flatMap(noticeTypeService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteNoticeType(ServerRequest request) {
        return noticeTypeService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }
}

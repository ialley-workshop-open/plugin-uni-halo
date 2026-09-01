package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.Notice;
import cn.ialley.unihalo.services.NoticeService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 通知公告接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class NoticeEndpoint implements CustomEndpoint {

    private final NoticeService noticeService;

    public NoticeEndpoint(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.NOTICE_API_BASE_PATH, this::listNotices)
                .POST(Constants.NOTICE_API_BASE_PATH, this::createNotice)
                .PUT(Constants.NOTICE_API_BASE_PATH + "/{name}", this::updateNotice)
                .DELETE(Constants.NOTICE_API_BASE_PATH + "/{name}", this::deleteNotice)
                .build();
    }

    private Mono<ServerResponse> listNotices(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String status = request.queryParam("status").orElse("").trim();
        String type = request.queryParam("type").orElse("").trim();
        String keyword = request.queryParam("keyword").orElse("").trim();
        return noticeService.list(status, type, keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createNotice(ServerRequest request) {
        return request.bodyToMono(Notice.class)
                .flatMap(noticeService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateNotice(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(Notice.class)
                .doOnNext(notice -> {
                    if (notice.getMetadata() == null) {
                        notice.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    notice.getMetadata().setName(name);
                })
                .flatMap(noticeService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteNotice(ServerRequest request) {
        return noticeService.delete(request.pathVariable("name"))
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

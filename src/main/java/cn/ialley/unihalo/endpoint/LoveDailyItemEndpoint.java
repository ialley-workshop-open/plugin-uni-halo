package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.LoveDailyItem;
import cn.ialley.unihalo.services.LoveDailyItemService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 恋爱清单接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class LoveDailyItemEndpoint implements CustomEndpoint {

    private final LoveDailyItemService loveDailyItemService;

    public LoveDailyItemEndpoint(LoveDailyItemService loveDailyItemService) {
        this.loveDailyItemService = loveDailyItemService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.LOVE_DAILY_API_BASE_PATH, this::listItems)
                .POST(Constants.LOVE_DAILY_API_BASE_PATH, this::createItem)
                .PUT(Constants.LOVE_DAILY_API_BASE_PATH + "/{name}", this::updateItem)
                .DELETE(Constants.LOVE_DAILY_API_BASE_PATH + "/{name}", this::deleteItem)
                .build();
    }

    private Mono<ServerResponse> listItems(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String status = request.queryParam("status").orElse("").trim();
        String keyword = request.queryParam("keyword").orElse("").trim();
        return loveDailyItemService.list(status, keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createItem(ServerRequest request) {
        return request.bodyToMono(LoveDailyItem.class)
                .flatMap(loveDailyItemService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateItem(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(LoveDailyItem.class)
                .doOnNext(item -> {
                    if (item.getMetadata() == null) {
                        item.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    item.getMetadata().setName(name);
                })
                .flatMap(loveDailyItemService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteItem(ServerRequest request) {
        return loveDailyItemService.delete(request.pathVariable("name"))
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

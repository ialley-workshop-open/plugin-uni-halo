package cn.ialley.unihalo.endpoint;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.AppInfo;
import cn.ialley.unihalo.services.AppInfoService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;

/**
 * 应用管理接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class AppInfoEndpoint implements CustomEndpoint {

    private final AppInfoService appInfoService;

    public AppInfoEndpoint(AppInfoService appInfoService) {
        this.appInfoService = appInfoService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.APP_INFO_API_BASE_PATH, this::listApps)
                .POST(Constants.APP_INFO_API_BASE_PATH, this::createApp)
                .PUT(Constants.APP_INFO_API_BASE_PATH + "/{name}", this::updateApp)
                .DELETE(Constants.APP_INFO_API_BASE_PATH + "/{name}", this::deleteApp)
                .build();
    }

    private Mono<ServerResponse> listApps(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String keyword = request.queryParam("keyword").orElse("").trim();
        return appInfoService.listAll()
                .filter(appInfo -> matchesKeyword(appInfo, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)))
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createApp(ServerRequest request) {
        return request.bodyToMono(AppInfo.class)
                .flatMap(appInfoService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateApp(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(AppInfo.class)
                .doOnNext(appInfo -> {
                    if (appInfo.getMetadata() == null) {
                        appInfo.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    appInfo.getMetadata().setName(name);
                })
                .flatMap(appInfoService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteApp(ServerRequest request) {
        return appInfoService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private static boolean matchesKeyword(AppInfo appInfo, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        if (appInfo.getSpec() == null) {
            return false;
        }
        String appid = appInfo.getSpec().getAppid();
        String name = appInfo.getSpec().getName();
        return (appid != null && appid.contains(keyword))
                || (name != null && name.contains(keyword));
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }
}
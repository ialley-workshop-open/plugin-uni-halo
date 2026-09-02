package cn.ialley.unihalo.endpoint;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.Banner;
import cn.ialley.unihalo.services.BannerService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * 首页轮播图接口（控制台，需登录）。
 *
 * <p>source 由服务端判定，不接受前端提交；/order、/candidates 必须注册在
 * /{name} 之前，避免 "order"/"candidates" 被当作 name 匹配。
 * custom 模式作者信息默认取当前登录用户（决策 D15），post 模式由服务端按文章快照。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class BannerEndpoint implements CustomEndpoint {

    private final BannerService bannerService;
    private final ReactiveExtensionClient client;

    public BannerEndpoint(BannerService bannerService, ReactiveExtensionClient client) {
        this.bannerService = bannerService;
        this.client = client;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.BANNER_API_BASE_PATH, this::listBanners)
                .POST(Constants.BANNER_API_BASE_PATH, this::createBanner)
                .PUT(Constants.BANNER_API_BASE_PATH + "/order", this::sortBanners)
                .GET(Constants.BANNER_API_BASE_PATH + "/candidates", this::listCandidates)
                .POST(Constants.BANNER_API_BASE_PATH + "/{name}/sync", this::syncBanner)
                .PUT(Constants.BANNER_API_BASE_PATH + "/{name}", this::updateBanner)
                .DELETE(Constants.BANNER_API_BASE_PATH + "/{name}", this::deleteBanner)
                .build();
    }

    private Mono<ServerResponse> listBanners(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String source = request.queryParam("source").orElse("").trim();
        String keyword = request.queryParam("keyword").orElse("").trim();
        String sort = request.queryParam("sort").orElse("").trim();
        return bannerService.list(source, keyword, page, size, sort)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createBanner(ServerRequest request) {
        return request.bodyToMono(Banner.class)
                .flatMap(banner -> fillCustomAuthor(request, banner))
                .flatMap(bannerService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> sortBanners(ServerRequest request) {
        return request.bodyToMono(new org.springframework.core.ParameterizedTypeReference<
                List<String>>() {
                })
                .flatMap(bannerService::sort)
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> listCandidates(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String keyword = request.queryParam("keyword").orElse("").trim();
        return bannerService.listCandidates(keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> updateBanner(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(Banner.class)
                .doOnNext(banner -> {
                    if (banner.getMetadata() == null) {
                        banner.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    banner.getMetadata().setName(name);
                })
                .flatMap(banner -> fillCustomAuthor(request, banner))
                .flatMap(bannerService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> syncBanner(ServerRequest request) {
        return bannerService.syncSnapshot(request.pathVariable("name"))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteBanner(ServerRequest request) {
        return bannerService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    /**
     * custom 模式（postId 为空）作者信息默认取当前登录用户（决策 D15），覆盖前端传入值；
     * post 模式由服务端按文章快照覆盖，不在此处理。
     */
    private Mono<Banner> fillCustomAuthor(ServerRequest request, Banner banner) {
        if (banner.getSpec() == null || !isBlank(banner.getSpec().getPostId())) {
            return Mono.just(banner);
        }
        return request.principal()
                .map(Principal::getName)
                .flatMap(username -> client.fetch(User.class, username))
                .map(user -> {
                    if (user.getSpec() != null) {
                        banner.getSpec().setAuthorName(user.getSpec().getDisplayName());
                        banner.getSpec().setAuthorAvatar(user.getSpec().getAvatar());
                    }
                    return banner;
                })
                .defaultIfEmpty(banner);
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

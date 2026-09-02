package cn.ialley.unihalo.endpoint;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.services.BannerService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 轮播图公开接口（app 端，匿名可访问）。
 *
 * <p>列表脱敏（不含 content/remark，决策 D5），详情才返回完整 HTML；
 * 公开接口已被 role-anonymous.yaml 的 api.unihalo.ialley.cn 全资源规则覆盖。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class BannerPublicEndpoint implements CustomEndpoint {

    private final BannerService bannerService;

    public BannerPublicEndpoint(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.BANNER_API_BASE_PATH, this::listBanners)
                .GET(Constants.BANNER_API_BASE_PATH + "/{name}", this::getBanner)
                .build();
    }

    /**
     * 公开有序列表（全量，脱敏视图）。
     */
    private Mono<ServerResponse> listBanners(ServerRequest request) {
        return bannerService.listPublic()
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 公开详情（含 content 富文本正文）；不存在返回 404。
     */
    private Mono<ServerResponse> getBanner(ServerRequest request) {
        String name = request.pathVariable("name");
        return bannerService.getPublicByName(name)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }
}

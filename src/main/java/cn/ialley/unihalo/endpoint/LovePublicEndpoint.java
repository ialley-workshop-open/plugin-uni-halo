package cn.ialley.unihalo.endpoint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.LoveAlbum;
import cn.ialley.unihalo.scheme.LoveConfig;
import cn.ialley.unihalo.services.LoveAlbumService;
import cn.ialley.unihalo.services.LoveConfigService;
import cn.ialley.unihalo.services.LoveDailyItemService;
import cn.ialley.unihalo.services.LoveStoryService;
import cn.ialley.unihalo.utils.AlbumTokenManager;
import cn.ialley.unihalo.vo.LoveAlbumVo;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.plugin.ReactiveSettingFetcher;
import tools.jackson.databind.ObjectMapper;

/**
 * 恋爱功能公开接口（小程序端，匿名可访问）。
 *
 * <p>聚合接口 GET /love-config 合并 settings 中的 loveEnabled（总开关仍保留在
 * 配置中，控制小程序端"我的页面"入口显示）；相册接口按锁定状态脱敏。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class LovePublicEndpoint implements CustomEndpoint {

    private static final String SETTING_GROUP_LOVE_CONFIG = "loveConfig";
    private static final String SETTING_KEY_LOVE_ENABLED = "loveEnabled";

    private final ReactiveSettingFetcher settingFetcher;
    private final LoveConfigService loveConfigService;
    private final LoveAlbumService loveAlbumService;
    private final LoveDailyItemService loveDailyItemService;
    private final LoveStoryService loveStoryService;
    private final AlbumTokenManager albumTokenManager;

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，故内部自行创建。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LovePublicEndpoint(ReactiveSettingFetcher settingFetcher,
            LoveConfigService loveConfigService,
            LoveAlbumService loveAlbumService,
            LoveDailyItemService loveDailyItemService,
            LoveStoryService loveStoryService,
            AlbumTokenManager albumTokenManager) {
        this.settingFetcher = settingFetcher;
        this.loveConfigService = loveConfigService;
        this.loveAlbumService = loveAlbumService;
        this.loveDailyItemService = loveDailyItemService;
        this.loveStoryService = loveStoryService;
        this.albumTokenManager = albumTokenManager;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.END_POINT_API_BASE_PATH + "/love-config", this::getLoveConfig)
                .GET(Constants.END_POINT_API_BASE_PATH + "/love-stories", this::listStories)
                .GET(Constants.END_POINT_API_BASE_PATH + "/love-albums", this::listAlbums)
                .GET(Constants.END_POINT_API_BASE_PATH + "/love-albums/{name}", this::getAlbum)
                .POST(Constants.END_POINT_API_BASE_PATH + "/love-albums/{name}/unlock",
                        this::unlockAlbum)
                .GET(Constants.END_POINT_API_BASE_PATH + "/love-daily-items", this::listDailyItems)
                .build();
    }

    /**
     * 恋爱配置：loveEnabled（来自 settings 总开关）+ LoveConfig 模型内容
     * （纪念日 + 恋人信息）。决策 D4：图片配置与模块开关不在此返回，
     * 小程序端继续从 getConfigs（loveConfig 组）读取。
     */
    private Mono<ServerResponse> getLoveConfig(ServerRequest request) {
        return Mono.zip(loveConfigService.get(), fetchLoveEnabled())
                .map(tuple -> {
                    LoveConfig config = tuple.getT1();
                    boolean enabled = Boolean.TRUE.equals(tuple.getT2());
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("enabled", enabled);
                    if (config.getSpec() != null) {
                        Map<?, ?> specMap = objectMapper.convertValue(
                                config.getSpec(), Map.class);
                        if (specMap != null) {
                            specMap.forEach((key, value) ->
                                    result.put(String.valueOf(key), value));
                        }
                    }
                    return result;
                })
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 故事列表（多条目，决策 D5）。
     */
    private Mono<ServerResponse> listStories(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        return loveStoryService.list("", page, size)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 相册列表：加密相册 locked=true 且不含 photos。
     */
    private Mono<ServerResponse> listAlbums(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        return loveAlbumService.list("", page, size)
                .map(result -> {
                    List<LoveAlbumVo> items = result.getItems().stream()
                            .map(album -> LoveAlbumVo.from(album, isLocked(album)))
                            .toList();
                    return new ListResult<>(result.getPage(), result.getSize(),
                            result.getTotal(), items);
                })
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 相册详情：加密相册需携带解锁 token 才返回 photos。
     */
    private Mono<ServerResponse> getAlbum(ServerRequest request) {
        String name = request.pathVariable("name");
        String token = request.queryParam("token").orElse("");
        return loveAlbumService.getByName(name)
                .map(album -> LoveAlbumVo.from(album,
                        isLocked(album) && !albumTokenManager.verify(name, token)))
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 密码解锁：校验通过后签发 HMAC 签名 token 并返回相册照片。
     */
    private Mono<ServerResponse> unlockAlbum(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(UnlockRequest.class)
                .flatMap(body -> loveAlbumService.verifyPassword(name, body.getPassword()))
                .flatMap(ok -> {
                    if (!ok) {
                        return ServerResponse.badRequest()
                                .bodyValue(Map.of("message", "密码不正确"));
                    }
                    String token = albumTokenManager.issue(name);
                    return loveAlbumService.getByName(name)
                            .map(album -> {
                                Map<String, Object> result = new LinkedHashMap<>();
                                result.put("token", token);
                                result.put("photos", album.getSpec() != null
                                        && album.getSpec().getPhotos() != null
                                                ? album.getSpec().getPhotos()
                                                : List.of());
                                return result;
                            })
                            .flatMap(body -> ServerResponse.ok().bodyValue(body));
                });
    }

    /**
     * 恋爱清单公开列表（只读，支持状态筛选与分页）。
     */
    private Mono<ServerResponse> listDailyItems(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String status = request.queryParam("status").orElse("").trim();
        return loveDailyItemService.list(status, "", page, size)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    private Mono<Boolean> fetchLoveEnabled() {
        return settingFetcher.getSettingValue(SETTING_GROUP_LOVE_CONFIG)
                .map(node -> node.hasNonNull(SETTING_KEY_LOVE_ENABLED)
                        && node.get(SETTING_KEY_LOVE_ENABLED).asBoolean())
                .defaultIfEmpty(false);
    }

    private static boolean isLocked(LoveAlbum album) {
        return album.getSpec() != null
                && Boolean.TRUE.equals(album.getSpec().getPasswordEnabled());
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }

    /**
     * 解锁请求体
     */
    @Data
    public static class UnlockRequest {
        private String password;
    }
}

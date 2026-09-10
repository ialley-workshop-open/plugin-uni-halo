package cn.ialley.unihalo.endpoint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.captcha.CaptchaScope;
import cn.ialley.unihalo.captcha.CaptchaService;
import cn.ialley.unihalo.captcha.CaptchaValidationException;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.GeneralConfig;
import cn.ialley.unihalo.scheme.GeneralConfig.ModuleSwitch;
import cn.ialley.unihalo.scheme.LoveAlbum;
import cn.ialley.unihalo.scheme.LoveConfig;
import cn.ialley.unihalo.services.GeneralConfigService;
import cn.ialley.unihalo.services.LoveAlbumService;
import cn.ialley.unihalo.services.LoveConfigService;
import cn.ialley.unihalo.services.LoveDailyItemService;
import cn.ialley.unihalo.services.LoveStoryService;
import cn.ialley.unihalo.utils.AlbumTokenManager;
import cn.ialley.unihalo.utils.LoveModuleTokenManager;
import cn.ialley.unihalo.vo.LoveAlbumVo;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import tools.jackson.databind.ObjectMapper;

/**
 * 恋爱功能公开接口（小程序端，匿名可访问）。
 *
 * <p>聚合接口 GET /love-config 合并通用配置中的 loveEnabled（总开关 2026-09-03 起
 * 由 setting 迁入 {@link GeneralConfig} spec.love，控制小程序端"我的页面"入口显示）；
 * 相册接口按锁定状态脱敏。</p>
 *
 * <p>恋爱模块入口密码（2026-09-08）：恋爱故事/相册/清单三个入口可在通用配置
 * 「恋爱设置-模块入口」分别设置密码，设置后对应数据接口（love-stories /
 * love-albums / love-daily-items）要求携带 {@code ?token=}（经
 * {@code POST /love-modules/unlock} 校验密码换取，30 分钟有效），未带或无效返回
 * 401 {@code {reason: "locked"}}；未设置密码的模块不校验（老客户端无感）。
 * 相册级密码保持现状（外层模块锁 + 内层相册锁）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class LovePublicEndpoint implements CustomEndpoint {

    private final GeneralConfigService generalConfigService;
    private final LoveConfigService loveConfigService;
    private final LoveAlbumService loveAlbumService;
    private final LoveDailyItemService loveDailyItemService;
    private final LoveStoryService loveStoryService;
    private final AlbumTokenManager albumTokenManager;
    private final LoveModuleTokenManager loveModuleTokenManager;
    private final CaptchaService captchaService;

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，故内部自行创建。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LovePublicEndpoint(GeneralConfigService generalConfigService,
            LoveConfigService loveConfigService,
            LoveAlbumService loveAlbumService,
            LoveDailyItemService loveDailyItemService,
            LoveStoryService loveStoryService,
            AlbumTokenManager albumTokenManager,
            LoveModuleTokenManager loveModuleTokenManager,
            CaptchaService captchaService) {
        this.generalConfigService = generalConfigService;
        this.loveConfigService = loveConfigService;
        this.loveAlbumService = loveAlbumService;
        this.loveDailyItemService = loveDailyItemService;
        this.loveStoryService = loveStoryService;
        this.albumTokenManager = albumTokenManager;
        this.loveModuleTokenManager = loveModuleTokenManager;
        this.captchaService = captchaService;
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
                .POST(Constants.END_POINT_API_BASE_PATH + "/love-modules/unlock",
                        this::unlockLoveModule)
                .GET(Constants.END_POINT_API_BASE_PATH + "/love-daily-items", this::listDailyItems)
                .build();
    }

    /**
     * 恋爱配置：loveEnabled（来自通用配置总开关）+ LoveConfig 模型内容
     * （纪念日 + 恋人信息）。恋爱页图片与模块开关 2026-09-03 起随通用配置
     * spec.love 下发（getConfigs loveConfig 组），不在本接口重复返回。
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
     * 故事列表（多条目，决策 D5；恋爱故事入口设置密码时要求携带模块 token）。
     */
    private Mono<ServerResponse> listStories(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        return requireModuleAccess(request, "ourStory",
                loveStoryService.listPublic(page, size)
                        .flatMap(body -> ServerResponse.ok().bodyValue(body)));
    }

    /**
     * 相册列表：加密相册 locked=true 且不含 photos；恋爱相册入口设置密码时
     * 要求携带模块 token（外层模块锁 + 内层相册锁）。
     */
    private Mono<ServerResponse> listAlbums(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        return requireModuleAccess(request, "lovePhoto",
                loveAlbumService.listPublic(page, size)
                        .map(result -> {
                            List<LoveAlbumVo> items = result.getItems().stream()
                                    .map(album -> LoveAlbumVo.from(album, isLocked(album)))
                                    .toList();
                            return new ListResult<>(result.getPage(), result.getSize(),
                                    result.getTotal(), items);
                        })
                        .flatMap(body -> ServerResponse.ok().bodyValue(body)));
    }

    /**
     * 相册详情：加密相册需携带解锁 token 才返回 photos；恋爱相册入口设置密码时
     * 同样要求模块 token。
     */
    private Mono<ServerResponse> getAlbum(ServerRequest request) {
        String name = request.pathVariable("name");
        String token = request.queryParam("token").orElse("");
        return requireModuleAccess(request, "lovePhoto",
                loveAlbumService.getByName(name)
                        // 公开详情：删除中对象视为不存在（决策 D7）
                        .filter(album -> album.getMetadata() == null
                                || album.getMetadata().getDeletionTimestamp() == null)
                        .map(album -> LoveAlbumVo.from(album,
                                isLocked(album) && !albumTokenManager.verify(name, token)))
                        .flatMap(body -> ServerResponse.ok().bodyValue(body))
                        .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build())));
    }

    /**
     * 密码解锁：校验通过后签发 HMAC 签名 token 并返回相册照片。
     * 验证码校验在密码校验之前，失败不暴露密码正确性；相册入口模块锁先行校验。
     */
    private Mono<ServerResponse> unlockAlbum(ServerRequest request) {
        String name = request.pathVariable("name");
        return requireModuleAccess(request, "lovePhoto",
                captchaService.requireValid(request, CaptchaScope.LOVE_ALBUM_UNLOCK)
                        .then(request.bodyToMono(UnlockRequest.class)
                                .flatMap(body -> loveAlbumService
                                        .verifyPassword(name, body.getPassword()))
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
                                }))
                        .onErrorResume(CaptchaValidationException.class, this::captchaForbidden));
    }

    /**
     * 恋爱模块入口解锁：校验模块存在且密码匹配后签发 HMAC 签名 token
     * （scope = 模块名，30 分钟有效；未设置密码的模块一律拒绝，不暴露是否已设置）。
     */
    private Mono<ServerResponse> unlockLoveModule(ServerRequest request) {
        return request.bodyToMono(LoveModuleUnlockRequest.class)
                .flatMap(body -> generalConfigService
                        .verifyLoveModulePassword(body.getModule(), body.getPassword())
                        .flatMap(ok -> {
                            if (!ok) {
                                return ServerResponse.badRequest()
                                        .bodyValue(Map.of("message", "密码不正确"));
                            }
                            return ServerResponse.ok()
                                    .bodyValue(Map.of("token",
                                            loveModuleTokenManager.issue(body.getModule())));
                        }));
    }

    /**
     * 恋爱模块入口访问控制：模块设置密码（锁定）时校验 {@code ?token=}，
     * 未带或无效返回 401 {@code {reason: "locked"}}（语义决策见
     * {@code .docs/module-lock-reminder-wechat-login-design.md} §3.4）；
     * 未锁定直接放行（老客户端无感）。
     */
    private Mono<ServerResponse> requireModuleAccess(ServerRequest request, String module,
            Mono<ServerResponse> body) {
        return generalConfigService.isLoveModuleLocked(module)
                .flatMap(locked -> {
                    if (!locked) {
                        return body;
                    }
                    String token = request.queryParam("token").orElse("");
                    if (loveModuleTokenManager.verify(module, token)) {
                        return body;
                    }
                    return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                            .bodyValue(Map.of("reason", "locked"));
                });
    }

    /**
     * 验证码校验失败：403 + 附新验证码（前端即时刷新重试）。
     */
    private Mono<ServerResponse> captchaForbidden(CaptchaValidationException e) {
        return captchaService.generate()
                .flatMap(captcha -> ServerResponse.status(HttpStatus.FORBIDDEN)
                        .bodyValue(Map.of("message", e.getMessage(), "captcha", captcha)));
    }

    /**
     * 恋爱清单公开列表（只读，支持状态筛选与分页）。
     */
    private Mono<ServerResponse> listDailyItems(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String status = request.queryParam("status").orElse("").trim();
        return loveDailyItemService.listPublic(status, page, size)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 恋爱总开关（/love-config enabled）派生：2026-09-10 起总开关 loveEnabled 已下线
     * （入口展示由模块入口开关与 navList 统一管理），此处按「任一模块入口开启」派生，
     * 兼容老客户端 /love-config 读取语义。
     */
    private Mono<Boolean> fetchLoveEnabled() {
        return generalConfigService.get()
                .map(config -> {
                    GeneralConfig.Love love = config.getSpec() != null
                            ? config.getSpec().getLove() : null;
                    if (love == null) {
                        return false;
                    }
                    return isModuleEnabled(love.getOurStory())
                            || isModuleEnabled(love.getLovePhoto())
                            || isModuleEnabled(love.getLoveDaily());
                })
                .defaultIfEmpty(false);
    }

    private static boolean isModuleEnabled(ModuleSwitch module) {
        return module != null && Boolean.TRUE.equals(module.getEnabled());
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

    /**
     * 恋爱模块入口解锁请求体（module = ourStory/lovePhoto/loveDaily）
     */
    @Data
    public static class LoveModuleUnlockRequest {
        private String module;
        private String password;
    }
}

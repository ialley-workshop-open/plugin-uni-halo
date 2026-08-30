package cn.ialley.unihalo.endpoint;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.services.AppVersionService;
import cn.ialley.unihalo.services.impl.AppVersionServiceImpl;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

/**
 * 应用升级公开接口（app 端调用，匿名可访问，需在 role-anonymous.yaml 放行）。
 *
 * @author 小莫唐尼
 */
@Component
public class UpgradeEndpoint implements CustomEndpoint {

    private final AppVersionService appVersionService;

    public UpgradeEndpoint(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final String apiTag = Constants.PUBLIC_CUSTOM_API_GROUP_NAME + "/Upgrade";

        return SpringdocRouteBuilder.route()
                .GET(Constants.UPGRADE_API_BASE_PATH + "/checkVersion", this::checkVersion,
                        builder -> builder.operationId("CheckVersion")
                                .description("检测应用更新")
                                .tag(apiTag)
                                .parameter(parameterBuilder()
                                        .name("appid")
                                        .in(ParameterIn.QUERY)
                                        .description("应用标识")
                                        .required(true)
                                        .implementation(String.class))
                                .parameter(parameterBuilder()
                                        .name("appVersion")
                                        .in(ParameterIn.QUERY)
                                        .description("当前原生 App 版本号")
                                        .required(true)
                                        .implementation(String.class))
                                .parameter(parameterBuilder()
                                        .name("wgtVersion")
                                        .in(ParameterIn.QUERY)
                                        .description("当前 wgt 资源版本号")
                                        .required(true)
                                        .implementation(String.class))
                                .parameter(parameterBuilder()
                                        .name("platform")
                                        .in(ParameterIn.QUERY)
                                        .description("目标平台：Android / iOS / Harmony，缺省按 User-Agent 推导")
                                        .implementation(String.class))
                                .parameter(parameterBuilder()
                                        .name("isUniappX")
                                        .in(ParameterIn.QUERY)
                                        .description("是否 uni-app x（Android 无 wgt 升级）")
                                        .implementation(Boolean.class)))
                .build();
    }

    private Mono<ServerResponse> checkVersion(ServerRequest request) {
        String appid = request.queryParam("appid").orElse(null);
        String appVersion = request.queryParam("appVersion").orElse(null);
        String wgtVersion = request.queryParam("wgtVersion").orElse(null);
        String platform = resolvePlatform(request);
        boolean isUniappX = Boolean.parseBoolean(
                request.queryParam("isUniappX").orElse("false"));

        return appVersionService.checkVersion(appid, appVersion, wgtVersion, platform, isUniappX)
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result));
    }

    /**
     * 平台推导：优先取参数，否则按 User-Agent（iPhone/iPad → iOS，Android → Android，否则 Harmony）。
     */
    private String resolvePlatform(ServerRequest request) {
        String platform = request.queryParam("platform").orElse("");
        if (!platform.isBlank()) {
            return platform;
        }
        String userAgent = request.headers().asHttpHeaders().getFirst("User-Agent");
        if (userAgent != null) {
            if (userAgent.matches(".*(iPhone|iPad).*")) {
                return AppVersionServiceImpl.PLATFORM_IOS;
            }
            if (userAgent.matches(".*[Aa]ndroid.*")) {
                return AppVersionServiceImpl.PLATFORM_ANDROID;
            }
        }
        return AppVersionServiceImpl.PLATFORM_HARMONY;
    }
}
package pro.uhalo.uni.endpoint;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pro.uhalo.uni.constants.Constants;
import pro.uhalo.uni.service.UniHaloService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
public class ConfigEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;
    private final UniHaloService uniHaloService;

    public ConfigEndpoint(ReactiveExtensionClient client, UniHaloService uniHaloService) {
        this.client = client;
        this.uniHaloService = uniHaloService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final String ApiTag = Constants.PUBLIC_CUSTOM_API_GROUP_NAME + "/Config";

        return SpringdocRouteBuilder.route()
            .GET(Constants.END_POINT_API_BASE_PATH + "/getConfigs", this::getAllConfigs,
                builder -> {
                    builder.operationId("GetConfigs")
                        .description("All config.")
                        .tag(ApiTag);
                })
            .GET(Constants.END_POINT_API_BASE_PATH + "/getConfigs/{groupName}",
                this::getConfigsByName, builder -> {
                    builder.operationId("GetConfigsByGroupName")
                        .description("Detail config.")
                        .tag(ApiTag)
                        .parameter(parameterBuilder()
                            .name("groupName")
                            .in(ParameterIn.PATH)
                            .description("Config group name")
                            .required(true)
                            .implementation(String.class)
                        );
                })
            .build();
    }

    Mono<ServerResponse> getAllConfigs(ServerRequest request) {
        return uniHaloService.getAppConfigs()
            .flatMap(config -> ServerResponse.ok().bodyValue(config));
    }

    Mono<ServerResponse> getConfigsByName(ServerRequest request) {
        var groupName = request.pathVariable("groupName");
        return uniHaloService.getAppConfigsByGroupName(groupName)
            .flatMap(config -> ServerResponse.ok().bodyValue(config));
    }
}

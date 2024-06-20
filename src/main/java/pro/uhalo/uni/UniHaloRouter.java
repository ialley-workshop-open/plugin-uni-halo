package pro.uhalo.uni;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static pro.uhalo.uni.constants.Constants.BASIC_DOMAIN_API_NAME;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.plugin.ReactiveSettingFetcher;


/**
 * @author 小莫唐尼
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class UniHaloRouter {
    private final ReactiveSettingFetcher settingFetcher;

    @Bean
    RouterFunction<ServerResponse> uHaloOptionsRouter() {
        return route(GET("/apis/" + BASIC_DOMAIN_API_NAME + "/xxxxs"), handlerFunction());
    }

    private HandlerFunction<ServerResponse> handlerFunction() {
        return request -> settingFetcher.getValues()
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }
}
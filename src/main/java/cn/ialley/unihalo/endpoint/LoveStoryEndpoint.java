package cn.ialley.unihalo.endpoint;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.LoveStory;
import cn.ialley.unihalo.services.LoveStoryService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 恋爱故事接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class LoveStoryEndpoint implements CustomEndpoint {

    private final LoveStoryService loveStoryService;

    public LoveStoryEndpoint(LoveStoryService loveStoryService) {
        this.loveStoryService = loveStoryService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.LOVE_STORY_API_BASE_PATH, this::listStories)
                .POST(Constants.LOVE_STORY_API_BASE_PATH, this::createStory)
                .PUT(Constants.LOVE_STORY_API_BASE_PATH + "/{name}", this::updateStory)
                .DELETE(Constants.LOVE_STORY_API_BASE_PATH + "/{name}", this::deleteStory)
                .build();
    }

    private Mono<ServerResponse> listStories(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String keyword = request.queryParam("keyword").orElse("").trim();
        return loveStoryService.list(keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createStory(ServerRequest request) {
        return request.bodyToMono(LoveStory.class)
                .flatMap(loveStoryService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateStory(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(LoveStory.class)
                .doOnNext(story -> {
                    if (story.getMetadata() == null) {
                        story.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    story.getMetadata().setName(name);
                })
                .flatMap(loveStoryService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteStory(ServerRequest request) {
        return loveStoryService.delete(request.pathVariable("name"))
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

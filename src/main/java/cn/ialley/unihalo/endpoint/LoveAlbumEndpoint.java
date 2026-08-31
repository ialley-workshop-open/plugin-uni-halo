package cn.ialley.unihalo.endpoint;

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
import cn.ialley.unihalo.services.LoveAlbumService;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

/**
 * 恋爱相册接口（控制台，需登录）。
 *
 * <p>密码语义：password 空 = 保持原密码；明文 = 重设；passwordRemoved = true = 清除。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class LoveAlbumEndpoint implements CustomEndpoint {

    private final LoveAlbumService loveAlbumService;

    public LoveAlbumEndpoint(LoveAlbumService loveAlbumService) {
        this.loveAlbumService = loveAlbumService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.LOVE_ALBUM_API_BASE_PATH, this::listAlbums)
                .GET(Constants.LOVE_ALBUM_API_BASE_PATH + "/{name}", this::getAlbum)
                .POST(Constants.LOVE_ALBUM_API_BASE_PATH, this::createAlbum)
                .PUT(Constants.LOVE_ALBUM_API_BASE_PATH + "/{name}", this::updateAlbum)
                .DELETE(Constants.LOVE_ALBUM_API_BASE_PATH + "/{name}", this::deleteAlbum)
                .POST(Constants.LOVE_ALBUM_API_BASE_PATH + "/{name}/photos", this::addPhoto)
                .PUT(Constants.LOVE_ALBUM_API_BASE_PATH + "/{name}/photos", this::updatePhotos)
                .DELETE(Constants.LOVE_ALBUM_API_BASE_PATH + "/{name}/photos/{photoName}",
                        this::deletePhoto)
                .build();
    }

    private Mono<ServerResponse> getAlbum(ServerRequest request) {
        return loveAlbumService.getByName(request.pathVariable("name"))
                .flatMap(album -> ServerResponse.ok().bodyValue(album))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> listAlbums(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String keyword = request.queryParam("keyword").orElse("").trim();
        return loveAlbumService.list(keyword, page, size)
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createAlbum(ServerRequest request) {
        return request.bodyToMono(LoveAlbumRequest.class)
                .flatMap(body -> loveAlbumService.create(body.getAlbum(), body.getPassword()))
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateAlbum(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(LoveAlbumRequest.class)
                .flatMap(body -> loveAlbumService.update(name, body.getAlbum(),
                        body.getPassword(), Boolean.TRUE.equals(body.getPasswordRemoved())))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteAlbum(ServerRequest request) {
        return loveAlbumService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> addPhoto(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(LoveAlbum.AlbumPhoto.class)
                .flatMap(photo -> loveAlbumService.addPhoto(name, photo))
                .flatMap(album -> ServerResponse.ok().bodyValue(album))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updatePhotos(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(PhotoListRequest.class)
                .flatMap(body -> loveAlbumService.updatePhotos(name, body.getPhotos()))
                .flatMap(album -> ServerResponse.ok().bodyValue(album))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deletePhoto(ServerRequest request) {
        String name = request.pathVariable("name");
        String photoName = request.pathVariable("photoName");
        return loveAlbumService.deletePhoto(name, photoName)
                .flatMap(album -> ServerResponse.ok().bodyValue(album))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }

    /**
     * 相册写请求体：album 为相册数据，password / passwordRemoved 为密码操作语义。
     */
    @Data
    public static class LoveAlbumRequest {
        private LoveAlbum album;
        private String password;
        private Boolean passwordRemoved;
    }

    @Data
    public static class PhotoListRequest {
        private List<LoveAlbum.AlbumPhoto> photos;
    }
}

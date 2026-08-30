package cn.ialley.unihalo.endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.AppVersion;
import cn.ialley.unihalo.services.AppVersionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.endpoint.SimpleFilePart;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.index.query.Condition;

import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.contains;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.or;

/**
 * 应用升级管理接口（控制台，需登录）。
 *
 * @author 小莫唐尼
 */
@Component
public class AppVersionEndpoint implements CustomEndpoint {

    private final AppVersionService appVersionService;
    private final AttachmentService attachmentService;

    public AppVersionEndpoint(AppVersionService appVersionService,
            AttachmentService attachmentService) {
        this.appVersionService = appVersionService;
        this.attachmentService = attachmentService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.CONSOLE_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.APP_VERSION_API_BASE_PATH, this::listVersions)
                .POST(Constants.APP_VERSION_API_BASE_PATH, this::createVersion)
                .PUT(Constants.APP_VERSION_API_BASE_PATH + "/{name}", this::updateVersion)
                .DELETE(Constants.APP_VERSION_API_BASE_PATH + "/{name}", this::deleteVersion)
                .POST(Constants.APP_VERSION_API_BASE_PATH + "/upload", this::uploadPackage)
                .build();
    }

    private Mono<ServerResponse> listVersions(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        String platform = request.queryParam("platform").orElse("").trim();

        return appVersionService.listAll(buildListOptions(request))
                .filter(version -> {
                    if (platform.isEmpty()) {
                        return true;
                    }
                    return version.getSpec().getPlatform() != null
                            && version.getSpec().getPlatform().contains(platform);
                })
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)))
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private Mono<ServerResponse> createVersion(ServerRequest request) {
        return request.bodyToMono(AppVersion.class)
                .flatMap(appVersionService::create)
                .flatMap(created -> ServerResponse.ok().bodyValue(created))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> updateVersion(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(AppVersion.class)
                .doOnNext(appVersion -> {
                    if (appVersion.getMetadata() == null) {
                        appVersion.setMetadata(new run.halo.app.extension.Metadata());
                    }
                    appVersion.getMetadata().setName(name);
                })
                .flatMap(appVersionService::update)
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .onErrorResume(IllegalArgumentException.class,
                        e -> ServerResponse.badRequest().bodyValue(Map.of("message", e.getMessage())));
    }

    private Mono<ServerResponse> deleteVersion(ServerRequest request) {
        return appVersionService.delete(request.pathVariable("name"))
                .then(ServerResponse.ok().bodyValue(Map.of("success", true)));
    }

    /**
     * 上传安装包（apk/wgt）到 Halo 附件库（内置存储策略 local），返回附件永久链接。
     * 控制台页面主要使用 Halo 附件选择器，此接口作为程序化上传入口。
     */
    private Mono<ServerResponse> uploadPackage(ServerRequest request) {
        return request.multipartData()
                .map(multipartData -> (FilePart) multipartData.getFirst("file"))
                .flatMap(filePart -> {
                    var simpleFilePart = new SimpleFilePart(filePart.filename(),
                            filePart.content(), filePart.headers().getContentType());
                    return attachmentService.upload("admin", "local", "plugin-uni-halo",
                            simpleFilePart, null);
                })
                .flatMap(attachment -> attachmentService.getPermalink(attachment)
                        .map(uri -> Map.of("url", uri.toString(), "name",
                                attachment.getSpec().getDisplayName())))
                .flatMap(result -> ServerResponse.ok().bodyValue(result));
    }

    private ListOptions buildListOptions(ServerRequest request) {
        List<Condition> queries = new ArrayList<>();
        request.queryParam("appid")
                .filter(value -> !value.isBlank())
                .ifPresent(value -> queries.add(equal("spec.appid", value)));
        request.queryParam("type")
                .filter(value -> !value.isBlank())
                .ifPresent(value -> queries.add(equal("spec.type", value)));
        request.queryParam("stablePublish")
                .filter(value -> !value.isBlank())
                .ifPresent(value -> queries.add(equal("spec.stablePublish", Boolean.parseBoolean(value))));
        request.queryParam("keyword")
                .filter(value -> !value.isBlank())
                .ifPresent(value -> queries.add(or(
                        contains("spec.title", value), contains("spec.version", value))));
        if (queries.isEmpty()) {
            return new ListOptions();
        }
        Condition combined = queries.stream()
                .reduce(Condition::and)
                .orElseThrow();
        return ListOptions.builder()
                .fieldQuery(combined)
                .build();
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
package cn.ialley.unihalo.endpoint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.scheme.Notice;
import cn.ialley.unihalo.scheme.NoticeType;
import cn.ialley.unihalo.services.NoticeService;
import cn.ialley.unihalo.services.NoticeTypeService;
import cn.ialley.unihalo.vo.NoticeListVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * 通知公告公开接口（app 端，匿名可访问）。
 *
 * <p>只暴露已发布（published）公告；列表脱敏不返回 content 正文（决策 D5），
 * 详情才返回完整 HTML；列表/详情/latest 均内嵌类型信息 typeDisplayName/typeColor
 * （决策 D10）；latest 无数据返回 200 + null（决策 D6）。</p>
 *
 * @author 小莫唐尼
 */
@Component
public class NoticePublicEndpoint implements CustomEndpoint {

    private static final String STATUS_PUBLISHED = "published";

    private final NoticeService noticeService;
    private final NoticeTypeService noticeTypeService;

    /**
     * 插件 Spring 上下文未注册 Jackson 3 ObjectMapper bean，故内部自行创建。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NoticePublicEndpoint(NoticeService noticeService,
            NoticeTypeService noticeTypeService) {
        this.noticeService = noticeService;
        this.noticeTypeService = noticeTypeService;
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(Constants.PUBLIC_CUSTOM_API_GROUP_NAME);
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        return RouterFunctions.route()
                .GET(Constants.NOTICE_API_BASE_PATH, this::listNotices)
                .GET(Constants.NOTICE_API_BASE_PATH + "/latest", this::getLatestNotice)
                .GET(Constants.NOTICE_API_BASE_PATH + "/{name}", this::getNotice)
                .build();
    }

    /**
     * 已发布公告分页列表（脱敏：不含 content，默认排序）。
     * 走公开专用查询：仅 published 且排除删除中对象（决策 D7）。
     */
    private Mono<ServerResponse> listNotices(ServerRequest request) {
        int page = queryPage(request);
        int size = querySize(request);
        return noticeService.listPublic(page, size)
                .flatMap(result -> Flux.fromIterable(result.getItems())
                        .flatMap(this::toListVo)
                        .collectList()
                        .map(items -> new ListResult<>(result.getPage(), result.getSize(),
                                result.getTotal(), items)))
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 最新一条已发布公告；无则 200 + null。
     */
    private Mono<ServerResponse> getLatestNotice(ServerRequest request) {
        return noticeService.getLatestPublished()
                .flatMap(this::toListVo)
                .defaultIfEmpty(null)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * 公告详情（含 content 富文本正文，内嵌类型信息）；不存在返回 404。
     * 删除中对象视为不存在（决策 D7）。
     */
    private Mono<ServerResponse> getNotice(ServerRequest request) {
        String name = request.pathVariable("name");
        return noticeService.getByName(name)
                // 公开详情：删除中对象视为不存在（决策 D7）
                .filter(notice -> notice.getMetadata() == null
                        || notice.getMetadata().getDeletionTimestamp() == null)
                .flatMap(this::toDetailMap)
                .flatMap(body -> ServerResponse.ok().bodyValue(body))
                .switchIfEmpty(Mono.defer(() -> ServerResponse.notFound().build()));
    }

    /**
     * 列表视图：脱敏 + 内嵌类型信息。
     */
    private Mono<NoticeListVo> toListVo(Notice notice) {
        return fetchType(notice)
                .map(type -> NoticeListVo.from(notice, type))
                .defaultIfEmpty(NoticeListVo.from(notice));
    }

    /**
     * 详情 Map：完整 Notice 字段 + spec 内追加 typeDisplayName/typeColor。
     */
    private Mono<Map<String, Object>> toDetailMap(Notice notice) {
        Map<String, Object> result = objectMapper.convertValue(notice,
                new TypeReference<Map<String, Object>>() { });
        if (notice.getSpec() == null || isBlank(notice.getSpec().getTypeName())) {
            return Mono.just(result);
        }
        return fetchType(notice)
                .map(type -> {
                    if (type.getSpec() != null
                            && result.get("spec") instanceof Map<?, ?> specMap) {
                        Map<String, Object> mutableSpec =
                                new LinkedHashMap<>(castMap(specMap));
                        mutableSpec.put("typeDisplayName", type.getSpec().getDisplayName());
                        mutableSpec.put("typeColor", type.getSpec().getColor());
                        result.put("spec", mutableSpec);
                    }
                    return result;
                })
                .defaultIfEmpty(result);
    }

    private Mono<NoticeType> fetchType(Notice notice) {
        if (notice.getSpec() == null || isBlank(notice.getSpec().getTypeName())) {
            return Mono.empty();
        }
        return noticeTypeService.getByName(notice.getSpec().getTypeName());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static int queryPage(ServerRequest request) {
        return request.queryParam("page").map(Integer::parseInt).orElse(1);
    }

    private static int querySize(ServerRequest request) {
        return request.queryParam("size").map(Integer::parseInt).orElse(20);
    }
}

package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.constants.Constants;
import cn.ialley.unihalo.enums.CandidateType;
import cn.ialley.unihalo.scheme.AuditDataConfig;
import cn.ialley.unihalo.scheme.CategoryRef;
import cn.ialley.unihalo.scheme.LinkGroupRef;
import cn.ialley.unihalo.scheme.MomentRef;
import cn.ialley.unihalo.scheme.PhotoGroupRef;
import cn.ialley.unihalo.scheme.PostRef;
import cn.ialley.unihalo.services.AuditDataService;
import cn.ialley.unihalo.vo.AuditDataCandidateResult;
import cn.ialley.unihalo.vo.AuditDataConfigDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.SchemeManager;

/**
 * 审核配置服务实现（单例模型 audit-data-config）。
 *
 * <p>选中引用以 {@link AuditDataConfig.AuditDataRef} 快照存储（name + 展示字段）；
 * 候选数据按类型映射到外部扩展 GVK（通过本地同 GVK 引用类查询，插件未安装时
 * 返回 pluginMissing 标记）；保存时校验并剔除失效引用。</p>
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class AuditDataServiceImpl implements AuditDataService {

    private static final int SUMMARY_MAX_LENGTH = 60;

    private final ReactiveExtensionClient client;
    private final SchemeManager schemeManager;

    @Override
    public Mono<AuditDataConfig> get() {
        return client.fetch(AuditDataConfig.class, Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME)
                .switchIfEmpty(Mono.fromSupplier(this::defaultConfig));
    }

    @Override
    public Mono<AuditDataConfig> save(AuditDataConfig config) {
        return withValidRefs(config).flatMap(this::saveSingleton);
    }

    @Override
    public Mono<AuditDataConfig> getEffective() {
        return get().flatMap(this::withValidRefs);
    }

    @Override
    public Mono<AuditDataConfigDetail> getDetail() {
        return get().flatMap(config -> {
            var spec = config.getSpec() == null
                    ? new AuditDataConfig.AuditDataConfigSpec()
                    : config.getSpec();
            Mono<Map<CandidateType, List<AuditDataConfig.AuditDataRef>>> selections =
                    Mono.just(new EnumMap<>(CandidateType.class));
            for (CandidateType type : CandidateType.values()) {
                selections = selections.zipWith(fetchSelected(type, refsOf(spec, type)),
                        (map, items) -> {
                            map.put(type, items);
                            return map;
                        });
            }
            return selections.map(map -> new AuditDataConfigDetail(config, map));
        });
    }

    private static List<AuditDataConfig.AuditDataRef> refsOf(
            AuditDataConfig.AuditDataConfigSpec spec, CandidateType type) {
        return switch (type) {
            case post -> spec.getPosts();
            case category -> spec.getCategories();
            case galleryGroup -> spec.getGalleryGroups();
            case moment -> spec.getMoments();
            case linkGroup -> spec.getLinkGroups();
        };
    }

    /**
     * 拉取已选 name 对应的最新详情（用于控制台已选列表刷新与失效标记：
     * 引用已删除或插件缺失时不在结果中，前端据此标记「已失效」）。
     */
    private Mono<List<AuditDataConfig.AuditDataRef>> fetchSelected(CandidateType type,
            List<AuditDataConfig.AuditDataRef> refs) {
        if (refs == null || refs.isEmpty()
                || isPluginMissing(type.groupVersionKind())) {
            return Mono.just(List.of());
        }
        var selectedNames = new HashSet<String>();
        refs.forEach(ref -> selectedNames.add(ref.getName()));
        return listAllRef(type)
                .map(extension -> toRef(type, extension))
                .filter(ref -> selectedNames.contains(ref.getName()))
                .collectList();
    }

    /**
     * 校验并剔除失效引用后返回同一配置对象（各列表仅保留存在的引用）。
     */
    private Mono<AuditDataConfig> withValidRefs(AuditDataConfig config) {
        if (config.getSpec() == null) {
            config.setSpec(new AuditDataConfig.AuditDataConfigSpec());
        }
        var spec = config.getSpec();
        return filterValid(spec.getPosts(), PostRef.class)
                .doOnNext(spec::setPosts)
                .flatMap(ignored -> filterValid(spec.getCategories(), CategoryRef.class))
                .doOnNext(spec::setCategories)
                .flatMap(ignored -> filterValid(spec.getGalleryGroups(), PhotoGroupRef.class))
                .doOnNext(spec::setGalleryGroups)
                .flatMap(ignored -> filterValid(spec.getMoments(), MomentRef.class))
                .doOnNext(spec::setMoments)
                .flatMap(ignored -> filterValid(spec.getLinkGroups(), LinkGroupRef.class))
                .doOnNext(spec::setLinkGroups)
                .thenReturn(config);
    }

    /**
     * 校验并剔除失效引用：逐个按 GVK 校验 name 存在性，仅保留存在的引用。
     * 对应扩展插件未安装/已卸载时，按「引用失效」处理（返回空集合）。
     */
    private <E extends Extension> Mono<List<AuditDataConfig.AuditDataRef>> filterValid(
            List<AuditDataConfig.AuditDataRef> refs, Class<E> type) {
        if (refs == null || refs.isEmpty()) {
            return Mono.just(new ArrayList<>());
        }
        return Flux.fromIterable(refs)
                .concatMap(ref -> client.fetch(type, ref.getName())
                        .map(found -> ref)
                        .onErrorResume(e -> Mono.empty()))
                .collectList();
    }

    @Override
    public Mono<AuditDataCandidateResult> listCandidates(CandidateType type, String keyword,
            int page, int size) {
        GroupVersionKind gvk = type.groupVersionKind();
        if (isPluginMissing(gvk)) {
            return Mono.just(new AuditDataCandidateResult(List.of(), page, size, 0, true));
        }
        return listAllRef(type)
                .map(extension -> toRef(type, extension))
                .filter(ref -> matchesKeyword(ref, keyword))
                .collectList()
                .map(list -> {
                    int total = list.size();
                    int from = Math.min((page - 1) * size, total);
                    int to = Math.min(from + size, total);
                    return new AuditDataCandidateResult(list.subList(from, to), page, size, total,
                            false);
                });
    }

    /**
     * 全量拉取某类型候选（按创建时间倒序），供内存关键字过滤 + 手动分页。
     * 数据量级：文章/分组/瞬间/链接分组，审核配置为低频操作，全量可接受。
     */
    private Flux<Extension> listAllRef(CandidateType type) {
        return client.listAll(resolveRefClass(type), ListOptions.builder().build(),
                        Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"))
                .map(extension -> (Extension) extension);
    }

    private Class<? extends Extension> resolveRefClass(CandidateType type) {
        return switch (type) {
            case post -> PostRef.class;
            case category -> CategoryRef.class;
            case galleryGroup -> PhotoGroupRef.class;
            case moment -> MomentRef.class;
            case linkGroup -> LinkGroupRef.class;
        };
    }

    /**
     * 候选/已选条目快照映射：name 必填，其余字段按类型选择性填充
     * （文章=title/cover/subTitle(发布时间)/extra(分类)；分类=title/subTitle(slug)；
     * 图库分组=title/extra(照片数)；瞬间=title(内容摘要)/cover(首图)；链接分组=title）。
     */
    @SuppressWarnings("unchecked")
    private AuditDataConfig.AuditDataRef toRef(CandidateType type, Extension extension) {
        var ref = new AuditDataConfig.AuditDataRef();
        ref.setName(extension.getMetadata() == null ? ""
                : extension.getMetadata().getName());
        switch (type) {
            case post -> {
                var spec = ((PostRef) extension).getSpec();
                if (spec != null) {
                    ref.setTitle(spec.getTitle());
                    ref.setCover(spec.getCover());
                    ref.setSubTitle(spec.getPublishTime());
                    ref.setExtra(firstOf(spec.getCategories()));
                }
            }
            case category -> {
                var spec = ((CategoryRef) extension).getSpec();
                if (spec != null) {
                    ref.setTitle(spec.getDisplayName());
                    ref.setSubTitle(spec.getSlug());
                }
            }
            case galleryGroup -> {
                var group = (PhotoGroupRef) extension;
                if (group.getSpec() != null) {
                    ref.setTitle(group.getSpec().getDisplayName());
                }
                Integer count = group.getStatus() == null ? null : group.getStatus().getPhotoCount();
                if (count != null) {
                    ref.setExtra(count + " 张照片");
                }
            }
            case moment -> {
                var spec = ((MomentRef) extension).getSpec();
                if (spec != null && spec.getContent() != null) {
                    var content = spec.getContent();
                    String text = content.getRaw() != null ? content.getRaw() : content.getHtml();
                    ref.setTitle(truncate(text, SUMMARY_MAX_LENGTH));
                    if (content.getMedium() != null && !content.getMedium().isEmpty()) {
                        ref.setCover(content.getMedium().get(0).getUrl());
                    }
                }
            }
            case linkGroup -> {
                var spec = ((LinkGroupRef) extension).getSpec();
                if (spec != null) {
                    ref.setTitle(spec.getDisplayName());
                }
            }
        }
        return ref;
    }

    private boolean isPluginMissing(GroupVersionKind gvk) {
        return schemeManager.schemes().stream()
                .noneMatch(scheme -> scheme.groupVersionKind().equals(gvk));
    }

    private static boolean matchesKeyword(AuditDataConfig.AuditDataRef ref, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        String kw = keyword.trim().toLowerCase();
        return contains(ref.getTitle(), kw)
                || contains(ref.getSubTitle(), kw)
                || contains(ref.getExtra(), kw);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private static String firstOf(List<String> values) {
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "…";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Mono<AuditDataConfig> saveSingleton(AuditDataConfig config) {
        return client.fetch(AuditDataConfig.class, Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME)
                .flatMap(existing -> {
                    existing.setSpec(config.getSpec());
                    return client.update(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME);
                    metadata.setCreationTimestamp(Instant.now());
                    config.setMetadata(metadata);
                    return client.create(config);
                }));
    }

    /**
     * 默认配置结构（不落库）：空 spec。
     */
    private AuditDataConfig defaultConfig() {
        AuditDataConfig config = new AuditDataConfig();
        Metadata metadata = new Metadata();
        metadata.setName(Constants.AUDIT_DATA_CONFIG_SINGLETON_NAME);
        config.setMetadata(metadata);
        config.setSpec(new AuditDataConfig.AuditDataConfigSpec());
        return config;
    }
}

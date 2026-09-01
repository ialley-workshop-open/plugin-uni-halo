package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.scheme.NoticeType;
import cn.ialley.unihalo.services.NoticeTypeService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * 公告类型服务实现
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class NoticeTypeServiceImpl implements NoticeTypeService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<NoticeType>> list(String keyword, int page, int size) {
        return client.listAll(NoticeType.class, new ListOptions(),
                        Sort.by(Sort.Direction.DESC, "spec.priority", "metadata.creationTimestamp"))
                .filter(type -> matchesKeyword(type, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<NoticeType> getByName(String name) {
        return client.fetch(NoticeType.class, name);
    }

    @Override
    public Mono<NoticeType> create(NoticeType noticeType) {
        return validate(noticeType)
                .then(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    noticeType.setMetadata(metadata);
                    return client.create(noticeType);
                }));
    }

    @Override
    public Mono<NoticeType> update(NoticeType noticeType) {
        String name = noticeType.getMetadata().getName();
        return validate(noticeType)
                .then(client.fetch(NoticeType.class, name))
                .flatMap(existing -> {
                    existing.setSpec(noticeType.getSpec());
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(NoticeType.class, name)
                .flatMap(client::delete)
                .then();
    }

    @Override
    public Mono<Void> sort(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(names)
                .index()
                .flatMap(tuple -> client.fetch(NoticeType.class, tuple.getT2())
                        .filter(type -> type != null)
                        .flatMap(type -> {
                            if (type.getSpec() == null) {
                                type.setSpec(new NoticeType.NoticeTypeSpec());
                            }
                            type.getSpec().setPriority(names.size() - tuple.getT1().intValue());
                            return client.update(type);
                        }))
                .then();
    }

    /**
     * 校验：displayName 必填。
     */
    private Mono<Void> validate(NoticeType noticeType) {
        var spec = noticeType.getSpec();
        if (spec == null || isBlank(spec.getDisplayName())) {
            return Mono.error(new IllegalArgumentException("类型名称不能为空"));
        }
        return Mono.empty();
    }

    private static boolean matchesKeyword(NoticeType noticeType, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        if (noticeType.getSpec() == null) {
            return false;
        }
        String displayName = noticeType.getSpec().getDisplayName();
        return displayName != null && displayName.contains(keyword);
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "noticetype-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

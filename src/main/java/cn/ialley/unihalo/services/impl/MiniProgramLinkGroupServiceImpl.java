package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.exception.NotFoundException;
import cn.ialley.unihalo.scheme.MiniProgramLinkGroup;
import cn.ialley.unihalo.services.MiniProgramLinkGroupService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * 友情链接-分组服务实现。
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class MiniProgramLinkGroupServiceImpl implements MiniProgramLinkGroupService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<MiniProgramLinkGroup>> list(String keyword, int page, int size) {
        return client.listAll(MiniProgramLinkGroup.class, ListOptions.builder().build(),
                        Sort.by(Sort.Direction.DESC, "spec.priority",
                                "metadata.creationTimestamp"))
                .filter(group -> {
                    if (isBlank(keyword)) {
                        return true;
                    }
                    return group.getSpec() != null
                            && contains(group.getSpec().getDisplayName(), keyword);
                })
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<List<MiniProgramLinkGroup>> listAll() {
        return client.listAll(MiniProgramLinkGroup.class, ListOptions.builder().build(),
                        Sort.by(Sort.Direction.DESC, "spec.priority",
                                "metadata.creationTimestamp"))
                .collectList();
    }

    @Override
    public Mono<MiniProgramLinkGroup> create(MiniProgramLinkGroup group) {
        return validate(group)
                .then(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    group.setMetadata(metadata);
                    return client.create(group);
                }));
    }

    @Override
    public Mono<MiniProgramLinkGroup> update(MiniProgramLinkGroup group) {
        String name = group.getMetadata().getName();
        return client.fetch(MiniProgramLinkGroup.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("分组不存在")))
                .flatMap(existing -> validate(group).thenReturn(existing))
                .flatMap(existing -> {
                    existing.setSpec(group.getSpec());
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(MiniProgramLinkGroup.class, name)
                .flatMap(client::delete)
                .then();
    }

    private Mono<Void> validate(MiniProgramLinkGroup group) {
        var spec = group.getSpec();
        if (spec == null) {
            return Mono.error(new IllegalArgumentException("分组数据不能为空"));
        }
        if (isBlank(spec.getDisplayName())) {
            return Mono.error(new IllegalArgumentException("分组名称不能为空"));
        }
        if (spec.getPriority() == null) {
            spec.setPriority(0);
        }
        return Mono.empty();
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "mini-program-link-group-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }
}

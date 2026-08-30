package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.scheme.LoveDailyItem;
import cn.ialley.unihalo.services.LoveDailyItemService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.equal;

/**
 * 恋爱清单服务实现
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class LoveDailyItemServiceImpl implements LoveDailyItemService {

    public static final String STATUS_WAIT = "wait";
    public static final String STATUS_DOING = "doing";
    public static final String STATUS_COMPLETE = "complete";

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<LoveDailyItem>> list(String status, String keyword, int page, int size) {
        var builder = ListOptions.builder();
        if (!isBlank(status)) {
            builder.fieldQuery(equal("spec.status", status));
        }
        ListOptions listOptions = builder.build();
        return client.listAll(LoveDailyItem.class, listOptions,
                        Sort.by(Sort.Direction.DESC, "spec.priority", "metadata.creationTimestamp"))
                .filter(item -> matchesKeyword(item, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<LoveDailyItem> getByName(String name) {
        return client.fetch(LoveDailyItem.class, name);
    }

    @Override
    public Mono<LoveDailyItem> create(LoveDailyItem item) {
        return validate(item)
                .then(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    item.setMetadata(metadata);
                    return client.create(item);
                }));
    }

    @Override
    public Mono<LoveDailyItem> update(LoveDailyItem item) {
        String name = item.getMetadata().getName();
        return validate(item)
                .then(client.fetch(LoveDailyItem.class, name))
                .flatMap(existing -> {
                    existing.setSpec(item.getSpec());
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(LoveDailyItem.class, name)
                .flatMap(client::delete)
                .then();
    }

    /**
     * 校验：title 必填；status 合法（wait/doing/complete）；complete 时 completeDate 必填；
     * 非 complete 状态清空 completeDate。
     */
    private Mono<Void> validate(LoveDailyItem item) {
        var spec = item.getSpec();
        if (spec == null || isBlank(spec.getTitle())) {
            return Mono.error(new IllegalArgumentException("标题不能为空"));
        }
        String status = isBlank(spec.getStatus()) ? STATUS_WAIT : spec.getStatus();
        if (!List.of(STATUS_WAIT, STATUS_DOING, STATUS_COMPLETE).contains(status)) {
            return Mono.error(new IllegalArgumentException("状态不合法：" + status));
        }
        spec.setStatus(status);
        if (STATUS_COMPLETE.equals(status) && isBlank(spec.getCompleteDate())) {
            return Mono.error(new IllegalArgumentException("已完成状态必须填写完成时间"));
        }
        if (!STATUS_COMPLETE.equals(status)) {
            spec.setCompleteDate(null);
            spec.setCompleteRemark(null);
        }
        return Mono.empty();
    }

    private static boolean matchesKeyword(LoveDailyItem item, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        if (item.getSpec() == null) {
            return false;
        }
        String title = item.getSpec().getTitle();
        String content = item.getSpec().getContent();
        return (title != null && title.contains(keyword))
                || (content != null && content.contains(keyword));
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "lovedaily-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

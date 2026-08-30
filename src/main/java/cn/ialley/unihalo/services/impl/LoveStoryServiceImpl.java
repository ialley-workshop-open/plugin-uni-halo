package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.scheme.LoveStory;
import cn.ialley.unihalo.services.LoveStoryService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * 恋爱故事服务实现
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class LoveStoryServiceImpl implements LoveStoryService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<LoveStory>> list(String keyword, int page, int size) {
        return client.listAll(LoveStory.class, new ListOptions(),
                        Sort.by(Sort.Direction.DESC, "spec.priority", "metadata.creationTimestamp"))
                .filter(story -> matchesKeyword(story, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<LoveStory> getByName(String name) {
        return client.fetch(LoveStory.class, name);
    }

    @Override
    public Mono<LoveStory> create(LoveStory story) {
        return validate(story)
                .then(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    story.setMetadata(metadata);
                    return client.create(story);
                }));
    }

    @Override
    public Mono<LoveStory> update(LoveStory story) {
        String name = story.getMetadata().getName();
        return validate(story)
                .then(client.fetch(LoveStory.class, name))
                .flatMap(existing -> {
                    existing.setSpec(story.getSpec());
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(LoveStory.class, name)
                .flatMap(client::delete)
                .then();
    }

    private Mono<Void> validate(LoveStory story) {
        if (story.getSpec() == null || isBlank(story.getSpec().getTitle())) {
            return Mono.error(new IllegalArgumentException("故事标题不能为空"));
        }
        return Mono.empty();
    }

    private static boolean matchesKeyword(LoveStory story, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        if (story.getSpec() == null) {
            return false;
        }
        String title = story.getSpec().getTitle();
        String content = story.getSpec().getContent();
        return (title != null && title.contains(keyword))
                || (content != null && content.contains(keyword));
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "lovestory-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

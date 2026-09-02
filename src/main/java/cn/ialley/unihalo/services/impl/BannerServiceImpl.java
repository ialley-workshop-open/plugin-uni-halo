package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.exception.NotFoundException;
import cn.ialley.unihalo.scheme.Banner;
import cn.ialley.unihalo.scheme.PostRef;
import cn.ialley.unihalo.services.BannerService;
import cn.ialley.unihalo.vo.BannerCandidateVo;
import cn.ialley.unihalo.vo.BannerDetailVo;
import cn.ialley.unihalo.vo.BannerListVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.equal;

/**
 * 首页轮播图服务实现
 *
 * <p>source 按 postId 非空自动判定并强制覆盖（决策 D2）；文章模式保存时服务端
 * 拉取 Post/User 回填快照字段（title/cover/date/authorName/authorAvatar），并清空
 * content（决策 D3），User 缺失时作者昵称回退 owner 用户名。</p>
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    public static final String SOURCE_POST = "post";
    public static final String SOURCE_CUSTOM = "custom";

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<Banner>> list(String source, String keyword, int page, int size,
            String sort) {
        var builder = ListOptions.builder();
        if (!isBlank(source)) {
            builder.fieldQuery(equal("spec.source", source));
        }
        return client.listAll(Banner.class, builder.build(), resolveSort(sort))
                .filter(banner -> matchesKeyword(banner, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    /**
     * 排序映射：manual 手动排序（priority 降序，默认）；date_desc 日期最新在前；
     * date_asc 日期最早在前（无日期条目按创建时间兜底）。
     */
    private static Sort resolveSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "date_desc" -> Sort.by(Sort.Order.desc("spec.date"),
                    Sort.Order.desc("metadata.creationTimestamp"));
            case "date_asc" -> Sort.by(Sort.Order.asc("spec.date"),
                    Sort.Order.asc("metadata.creationTimestamp"));
            default -> Sort.by(Sort.Order.desc("spec.priority"),
                    Sort.Order.desc("metadata.creationTimestamp"));
        };
    }

    @Override
    public Mono<Banner> getByName(String name) {
        return client.fetch(Banner.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("轮播图不存在")));
    }

    @Override
    public Mono<Banner> create(Banner banner) {
        return normalize(banner)
                .then(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    banner.setMetadata(metadata);
                    return client.create(banner);
                }));
    }

    @Override
    public Mono<Banner> update(Banner banner) {
        String name = banner.getMetadata() == null ? null : banner.getMetadata().getName();
        return normalize(banner)
                .then(client.fetch(Banner.class, name)
                        .switchIfEmpty(Mono.error(new NotFoundException("轮播图不存在")))
                        .flatMap(existing -> {
                            existing.setSpec(banner.getSpec());
                            return client.update(existing);
                        }));
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(Banner.class, name)
                .flatMap(client::delete)
                .then();
    }

    @Override
    public Mono<Banner> syncSnapshot(String name) {
        return client.fetch(Banner.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("轮播图不存在")))
                .flatMap(banner -> {
                    if (banner.getSpec() == null || isBlank(banner.getSpec().getPostId())) {
                        return Mono.error(new IllegalArgumentException("仅文章来源支持同步快照"));
                    }
                    // 重新拉取 Post/User 覆盖快照字段；文章已删除时抛异常
                    return snapshotFromPost(banner.getSpec()).thenReturn(banner);
                })
                .flatMap(client::update);
    }

    @Override
    public Mono<Void> sort(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(names)
                .index()
                .flatMap(tuple -> client.fetch(Banner.class, tuple.getT2())
                        .filter(banner -> banner != null)
                        .flatMap(banner -> {
                            if (banner.getSpec() == null) {
                                banner.setSpec(new Banner.BannerSpec());
                            }
                            banner.getSpec().setPriority((int) (names.size() - tuple.getT1()));
                            return client.update(banner);
                        }))
                .then();
    }

    @Override
    public Mono<ListResult<BannerCandidateVo>> listCandidates(String keyword, int page, int size) {
        return client.listAll(PostRef.class, ListOptions.builder().build(),
                        Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp"))
                .filter(post -> post.getSpec() != null
                        && Boolean.TRUE.equals(post.getSpec().getPublish()))
                .map(this::toCandidate)
                .filter(candidate -> matchesCandidateKeyword(candidate, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<List<BannerListVo>> listPublic() {
        return client.listAll(Banner.class, ListOptions.builder().build(), defaultSort())
                .map(BannerListVo::from)
                .collectList();
    }

    @Override
    public Mono<BannerDetailVo> getPublicByName(String name) {
        return client.fetch(Banner.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("轮播图不存在")))
                .map(BannerDetailVo::from);
    }

    /**
     * 校验与规整：source 按 postId 非空判定并强制覆盖（决策 D2）；
     * 文章模式清空 content（决策 D3）并**先快照再校验**（title 由 Post 填充，
     * 批量创建仅提交 postId 时 title 为空也能通过），快照后 title 仍为空才报错；
     * 自定义模式 title/cover 必填。
     */
    private Mono<Void> normalize(Banner banner) {
        var spec = banner.getSpec();
        if (spec == null) {
            return Mono.error(new IllegalArgumentException("轮播图数据不能为空"));
        }
        boolean fromPost = !isBlank(spec.getPostId());
        spec.setSource(fromPost ? SOURCE_POST : SOURCE_CUSTOM);
        if (fromPost) {
            spec.setContent(null);
            return snapshotFromPost(spec)
                    .flatMap(ignored -> {
                        if (isBlank(spec.getTitle())) {
                            return Mono.error(new IllegalArgumentException("标题不能为空"));
                        }
                        return Mono.empty();
                    });
        }
        if (isBlank(spec.getTitle())) {
            return Mono.error(new IllegalArgumentException("标题不能为空"));
        }
        if (isBlank(spec.getCover())) {
            return Mono.error(new IllegalArgumentException("封面图不能为空"));
        }
        return Mono.empty();
    }

    /**
     * 文章模式快照：拉取 Post 覆盖 title/cover/date，再查 User 覆盖作者昵称/头像；
     * User 缺失或字段为空时回退 owner 用户名。
     */
    private Mono<Void> snapshotFromPost(Banner.BannerSpec spec) {
        return client.fetch(PostRef.class, spec.getPostId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "文章不存在：" + spec.getPostId())))
                .flatMap(post -> {
                    var postSpec = post.getSpec();
                    if (postSpec != null) {
                        if (postSpec.getTitle() != null) {
                            spec.setTitle(postSpec.getTitle());
                        }
                        if (postSpec.getCover() != null) {
                            spec.setCover(postSpec.getCover());
                        }
                        if (postSpec.getPublishTime() != null) {
                            spec.setDate(postSpec.getPublishTime());
                        }
                    }
                    // Halo 2.x 的 metadata 无 owner，作者在 Post.spec.owner（用户名）
                    String owner = postSpec == null ? null : postSpec.getOwner();
                    return fillAuthor(spec, owner);
                });
    }

    private Mono<Void> fillAuthor(Banner.BannerSpec spec, String owner) {
        if (isBlank(owner)) {
            return Mono.empty();
        }
        return client.fetch(User.class, owner)
                .flatMap(user -> {
                    if (user.getSpec() != null) {
                        if (user.getSpec().getDisplayName() != null) {
                            spec.setAuthorName(user.getSpec().getDisplayName());
                        }
                        if (user.getSpec().getAvatar() != null) {
                            spec.setAuthorAvatar(user.getSpec().getAvatar());
                        }
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    if (isBlank(spec.getAuthorName())) {
                        spec.setAuthorName(owner);
                    }
                    return Mono.empty();
                }))
                .then();
    }

    private BannerCandidateVo toCandidate(PostRef post) {
        var vo = new BannerCandidateVo();
        if (post.getMetadata() != null) {
            vo.setName(post.getMetadata().getName());
        }
        var spec = post.getSpec();
        if (spec != null) {
            vo.setTitle(spec.getTitle());
            vo.setCover(spec.getCover());
            vo.setPublishTime(spec.getPublishTime());
            vo.setCategories(spec.getCategories());
        }
        return vo;
    }

    private static Sort defaultSort() {
        return Sort.by(Sort.Order.desc("spec.priority"),
                Sort.Order.desc("metadata.creationTimestamp"));
    }

    private static boolean matchesKeyword(Banner banner, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        if (banner.getSpec() == null) {
            return false;
        }
        String title = banner.getSpec().getTitle();
        String remark = banner.getSpec().getRemark();
        return (title != null && title.contains(keyword))
                || (remark != null && remark.contains(keyword));
    }

    private static boolean matchesCandidateKeyword(BannerCandidateVo candidate, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        return candidate.getTitle() != null && candidate.getTitle().contains(keyword);
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "banner-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

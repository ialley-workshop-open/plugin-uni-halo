package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.exception.NotFoundException;
import cn.ialley.unihalo.scheme.Notice;
import cn.ialley.unihalo.services.NoticeService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;

/**
 * 通知公告服务实现
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    public static final String STATUS_DRAFT = "draft";
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_OFFLINE = "offline";

    /**
     * 摘要截断长度（手填不截断，仅自动生成时截断）
     */
    private static final int SUMMARY_MAX_LENGTH = 200;

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<Notice>> list(String status, String type, String keyword,
            int page, int size, String sort) {
        var builder = ListOptions.builder();
        if (!isBlank(status)) {
            builder.fieldQuery(equal("spec.status", status));
        }
        if (!isBlank(type)) {
            builder.fieldQuery(equal("spec.typeName", type));
        }
        ListOptions listOptions = builder.build();
        return client.listAll(Notice.class, listOptions, resolveSort(sort))
                .filter(notice -> matchesKeyword(notice, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<ListResult<Notice>> listPublic(int page, int size) {
        // 公开读路径：仅 published 且排除删除中对象（决策 D7）
        return client.listAll(Notice.class,
                        ListOptions.builder()
                                .fieldQuery(and(
                                        equal("spec.status", STATUS_PUBLISHED),
                                        isNull("metadata.deletionTimestamp")))
                                .build(),
                        resolveSort(""))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    /**
     * 排序映射：date_desc 最新在前（默认，置顶优先）；date_asc 最早在前；
     * type 按类型分组（typeName 升序 + 组内最新在前）。
     */
    private static Sort resolveSort(String sort) {
        return switch (sort == null ? "" : sort) {
            case "date_asc" -> Sort.by(Sort.Order.asc("spec.publishTime"),
                    Sort.Order.asc("metadata.creationTimestamp"));
            case "type" -> Sort.by(Sort.Order.asc("spec.typeName"),
                    Sort.Order.desc("spec.publishTime"),
                    Sort.Order.desc("metadata.creationTimestamp"));
            default -> Sort.by(Sort.Order.desc("spec.priority"),
                    Sort.Order.desc("spec.publishTime"),
                    Sort.Order.desc("metadata.creationTimestamp"));
        };
    }

    @Override
    public Mono<Notice> getByName(String name) {
        return client.fetch(Notice.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("公告不存在")));
    }

    @Override
    public Mono<Notice> create(Notice notice) {
        return validate(notice, null)
                .then(Mono.defer(() -> {
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    notice.setMetadata(metadata);
                    return client.create(notice);
                }));
    }

    @Override
    public Mono<Notice> update(Notice notice) {
        String name = notice.getMetadata().getName();
        return client.fetch(Notice.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("公告不存在")))
                .flatMap(existing -> validate(notice, existing.getSpec() == null
                        ? null : existing.getSpec().getPublishTime())
                        .thenReturn(existing))
                .flatMap(existing -> {
                    existing.setSpec(notice.getSpec());
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(Notice.class, name)
                .flatMap(client::delete)
                .then();
    }

    @Override
    public Mono<Notice> getLatestPublished() {
        // 公开读路径：排除删除中对象（决策 D7）
        return client.listAll(Notice.class,
                        ListOptions.builder()
                                .fieldQuery(and(
                                        equal("spec.status", STATUS_PUBLISHED),
                                        isNull("metadata.deletionTimestamp")))
                                .build(),
                        Sort.by(Sort.Direction.DESC, "spec.priority", "spec.publishTime",
                                "metadata.creationTimestamp"))
                .next();
    }

    /**
     * 校验与规整：title 必填；status 合法（空则默认 draft）；summary 手填优先、
     * 为空时从 content 剥离生成（决策 D12）；publishTime 在 published 时首次写入、
     * 非 published 清空。
     *
     * @param oldPublishTime 已有记录的发布时间（update 时传入，create 传 null）
     */
    private Mono<Void> validate(Notice notice, String oldPublishTime) {
        var spec = notice.getSpec();
        if (spec == null || isBlank(spec.getTitle())) {
            return Mono.error(new IllegalArgumentException("标题不能为空"));
        }
        String status = isBlank(spec.getStatus()) ? STATUS_DRAFT : spec.getStatus();
        if (!List.of(STATUS_DRAFT, STATUS_PUBLISHED, STATUS_OFFLINE).contains(status)) {
            return Mono.error(new IllegalArgumentException("状态不合法：" + status));
        }
        spec.setStatus(status);
        if (isBlank(spec.getSummary())) {
            spec.setSummary(toPlainText(spec.getContent()));
        }
        if (STATUS_PUBLISHED.equals(status)) {
            if (isBlank(spec.getPublishTime()) && isBlank(oldPublishTime)) {
                spec.setPublishTime(Instant.now().toString());
            } else if (isBlank(spec.getPublishTime())) {
                spec.setPublishTime(oldPublishTime);
            }
        } else {
            spec.setPublishTime(null);
        }
        return Mono.empty();
    }

    private static boolean matchesKeyword(Notice notice, String keyword) {
        if (isBlank(keyword)) {
            return true;
        }
        if (notice.getSpec() == null) {
            return false;
        }
        String title = notice.getSpec().getTitle();
        String summary = notice.getSpec().getSummary();
        return (title != null && title.contains(keyword))
                || (summary != null && summary.contains(keyword));
    }

    /**
     * HTML 剥离为纯文本并截断 200 字（决策 D3/D4 修订）。
     *
     * <p>优先用 jsoup（Halo 传递依赖）解析；异常时退化为正则剔除
     * script/style 与标签。</p>
     */
    private static String toPlainText(String html) {
        if (isBlank(html)) {
            return "";
        }
        String text;
        try {
            text = Jsoup.parse(html).text();
        } catch (Throwable e) {
            text = html.replaceAll("(?is)<(script|style).*?</\\1>", " ")
                    .replaceAll("(?is)<[^>]+>", " ")
                    .replaceAll("&nbsp;", " ")
                    .replaceAll("&amp;", "&")
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("&quot;", "\"");
        }
        String trimmed = text.replaceAll("\\s+", " ").trim();
        return trimmed.length() > SUMMARY_MAX_LENGTH
                ? trimmed.substring(0, SUMMARY_MAX_LENGTH) + "..."
                : trimmed;
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "notice-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

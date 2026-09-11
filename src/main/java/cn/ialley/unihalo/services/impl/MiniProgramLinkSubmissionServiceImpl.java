package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.exception.NotFoundException;
import cn.ialley.unihalo.scheme.MiniProgramLink;
import cn.ialley.unihalo.scheme.MiniProgramLinkSubmission;
import cn.ialley.unihalo.services.MiniProgramLinkSubmissionService;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.equal;

/**
 * 友情链接-小程序链接申请单服务实现（决策 D6/D7/D10）。
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class MiniProgramLinkSubmissionServiceImpl implements MiniProgramLinkSubmissionService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<MiniProgramLinkSubmission>> list(String status, String keyword,
            String sort, int page, int size) {
        var builder = ListOptions.builder();
        if (!isBlank(status)) {
            builder.fieldQuery(equal("spec.status", status));
        }
        ListOptions listOptions = builder.build();
        return client.listAll(MiniProgramLinkSubmission.class, listOptions, Sort.unsorted())
                .filter(submission -> matches(submission, keyword))
                .collectList()
                .map(list -> {
                    list.sort(sortComparator(sort));
                    return new ListResult<>(page, size, list.size(), slice(list, page, size));
                });
    }

    /**
     * sort 参数解析为内存排序比较器（决策：审核待办优先）：
     * <ul>
     *   <li>空/默认：待审核优先（PENDING 在前），其余按申请时间倒序</li>
     *   <li>submittedAt / reviewedAt / status：字段倒序（白名单，防止任意字段排序）</li>
     *   <li>statusFirst:{STATUS}：目标状态优先（如 statusFirst:PENDING），其余按申请时间倒序</li>
     * </ul>
     */
    private static Comparator<MiniProgramLinkSubmission> sortComparator(String sort) {
        String statusFirst = statusFirstOf(sort);
        if (statusFirst != null) {
            return Comparator
                    .comparing((MiniProgramLinkSubmission s) ->
                            statusFirst.equals(s.getSpec().getStatus()) ? 0 : 1)
                    .thenComparing(submittedAtDesc());
        }
        return switch (sort == null ? "" : sort) {
            case "reviewedAt" -> Comparator.comparing(
                            (MiniProgramLinkSubmission s) -> s.getSpec().getReviewedAt(),
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(submittedAtDesc());
            case "status" -> Comparator.comparing(
                            (MiniProgramLinkSubmission s) -> s.getSpec().getStatus(),
                            Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(submittedAtDesc());
            case "submittedAt" -> submittedAtDesc();
            default -> pendingFirstDesc(); // 空/未知值：默认=待审核优先
        };
    }

    /** 解析 statusFirst:{STATUS}，非法值返回 null（走默认排序） */
    private static String statusFirstOf(String sort) {
        if (sort != null && sort.startsWith("statusFirst:")) {
            String statusValue = sort.substring("statusFirst:".length());
            if (List.of(STATUS_PENDING, STATUS_APPROVED, STATUS_REJECTED).contains(statusValue)) {
                return statusValue;
            }
        }
        return null;
    }

    /** 待审核优先（PENDING 在前），其余按申请时间倒序 */
    private static Comparator<MiniProgramLinkSubmission> pendingFirstDesc() {
        return Comparator
                .comparing((MiniProgramLinkSubmission s) ->
                        STATUS_PENDING.equals(s.getSpec().getStatus()) ? 0 : 1)
                .thenComparing(submittedAtDesc());
    }

    /** 申请时间倒序，二级创建时间倒序 */
    private static Comparator<MiniProgramLinkSubmission> submittedAtDesc() {
        return Comparator
                .comparing((MiniProgramLinkSubmission s) -> s.getSpec().getSubmittedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(s -> s.getMetadata().getCreationTimestamp(),
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    @Override
    public Mono<MiniProgramLinkSubmission> submit(MiniProgramLinkSubmission submission) {
        return validate(submission)
                .then(Mono.defer(() -> {
                    var spec = submission.getSpec();
                    spec.setStatus(STATUS_PENDING);
                    spec.setSubmittedAt(Instant.now().toString());
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    submission.setMetadata(metadata);
                    return client.create(submission);
                }));
    }

    @Override
    public Mono<MiniProgramLinkSubmission> approve(String name, String reason,
            String groupName) {
        return client.fetch(MiniProgramLinkSubmission.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("申请不存在")))
                .flatMap(existing -> {
                    var spec = existing.getSpec();
                    if (spec == null) {
                        return Mono.error(new IllegalArgumentException("申请数据不完整"));
                    }
                    if (groupName != null) {
                        spec.setGroupName(groupName);
                    }
                    if (STATUS_APPROVED.equals(spec.getStatus())
                            && !isBlank(spec.getLinkName())) {
                        // 幂等：已审核通过且已生成链接，仅同步分组后返回（D7）
                        return client.update(existing);
                    }
                    return createLinkFrom(existing)
                            .flatMap(created -> {
                                spec.setLinkName(created.getMetadata().getName());
                                spec.setStatus(STATUS_APPROVED);
                                spec.setReason(trimToNull(reason));
                                spec.setReviewedAt(Instant.now().toString());
                                return client.update(existing);
                            });
                });
    }

    @Override
    public Mono<MiniProgramLinkSubmission> reject(String name, String reason,
            String groupName) {
        if (isBlank(reason)) {
            return Mono.error(new IllegalArgumentException("审核拒绝必须填写原因"));
        }
        return client.fetch(MiniProgramLinkSubmission.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("申请不存在")))
                .flatMap(existing -> {
                    var spec = existing.getSpec();
                    if (spec == null) {
                        return Mono.error(new IllegalArgumentException("申请数据不完整"));
                    }
                    if (groupName != null) {
                        spec.setGroupName(groupName);
                    }
                    spec.setStatus(STATUS_REJECTED);
                    spec.setReason(reason.trim());
                    spec.setReviewedAt(Instant.now().toString());
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(MiniProgramLinkSubmission.class, name)
                .flatMap(client::delete)
                .then();
    }

    /**
     * 审核通过时自动生成链接（D7）：拷贝业务字段，visible=true、priority=0。
     */
    private Mono<MiniProgramLink> createLinkFrom(MiniProgramLinkSubmission submission) {
        MiniProgramLink link = new MiniProgramLink();
        MiniProgramLink.MiniProgramLinkSpec spec = new MiniProgramLink.MiniProgramLinkSpec();
        var source = submission.getSpec();
        spec.setDisplayName(source.getDisplayName());
        spec.setMiniProgramCode(source.getMiniProgramCode());
        spec.setLink(source.getLink());
        spec.setAuthorName(source.getAuthorName());
        spec.setAvatar(source.getAvatar());
        spec.setWebsite(source.getWebsite());
        spec.setGroupName(source.getGroupName());
        spec.setDescription(source.getDescription());
        spec.setScreenshots(copyList(source.getScreenshots()));
        spec.setVisible(true);
        spec.setPriority(0);
        spec.setSource("submitted");
        link.setSpec(spec);
        Metadata metadata = new Metadata();
        metadata.setName("mini-program-link-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000)));
        metadata.setCreationTimestamp(Instant.now());
        link.setMetadata(metadata);
        return client.create(link);
    }

    /**
     * 校验：displayName、miniProgramCode 必填；email 非空时校验格式（D5/D13）。
     */
    private Mono<Void> validate(MiniProgramLinkSubmission submission) {
        var spec = submission.getSpec();
        if (spec == null) {
            return Mono.error(new IllegalArgumentException("申请数据不能为空"));
        }
        if (isBlank(spec.getDisplayName())) {
            return Mono.error(new IllegalArgumentException("小程序名称不能为空"));
        }
        if (isBlank(spec.getMiniProgramCode())) {
            return Mono.error(new IllegalArgumentException("太阳码不能为空"));
        }
        if (!isBlank(spec.getEmail()) && !EMAIL_PATTERN.matcher(spec.getEmail().trim()).matches()) {
            return Mono.error(new IllegalArgumentException("邮箱格式不正确"));
        }
        spec.setEmail(trimToNull(spec.getEmail()));
        spec.setScreenshots(distinctNonBlank(spec.getScreenshots()));
        return Mono.empty();
    }

    private static boolean matches(MiniProgramLinkSubmission submission, String keyword) {
        if (isBlank(keyword) || submission.getSpec() == null) {
            return isBlank(keyword);
        }
        var spec = submission.getSpec();
        return contains(spec.getDisplayName(), keyword)
                || contains(spec.getAuthorName(), keyword)
                || contains(spec.getEmail(), keyword);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static List<String> copyList(List<String> values) {
        return values == null ? null : new ArrayList<>(values);
    }

    private static List<String> distinctNonBlank(List<String> values) {
        if (values == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank() && !result.contains(value.trim())) {
                result.add(value.trim());
            }
        }
        return result;
    }

    private static <T> List<T> slice(List<T> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    private static String generateName() {
        return "mini-program-link-submission-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }
}

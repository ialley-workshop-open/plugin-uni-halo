package cn.ialley.unihalo.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import cn.ialley.unihalo.exception.NotFoundException;
import cn.ialley.unihalo.scheme.MiniProgramLink;
import cn.ialley.unihalo.scheme.MiniProgramLinkGroup;
import cn.ialley.unihalo.services.MiniProgramLinkService;
import cn.ialley.unihalo.vo.GroupOption;
import cn.ialley.unihalo.vo.MiniProgramLinkGroupVo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;

/**
 * 友情链接-小程序链接服务实现（决策 D2/D5/D8）。
 *
 * @author 小莫唐尼
 */
@Service
@RequiredArgsConstructor
public class MiniProgramLinkServiceImpl implements MiniProgramLinkService {

    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<MiniProgramLink>> list(String group, Boolean visible,
            String keyword, int page, int size) {
        var builder = ListOptions.builder();
        if (visible != null) {
            builder.fieldQuery(equal("spec.visible", visible));
        }
        ListOptions listOptions = builder.build();
        return client.listAll(MiniProgramLink.class, listOptions,
                        Sort.by(Sort.Direction.DESC, "spec.priority", "metadata.creationTimestamp"))
                .filter(link -> matches(link, group, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<ListResult<MiniProgramLink>> listPublic(String group, String keyword,
            int page, int size) {
        // 公开读路径：visible=true 且排除删除中对象（决策 D7）
        return client.listAll(MiniProgramLink.class,
                        ListOptions.builder()
                                .fieldQuery(and(
                                        equal("spec.visible", true),
                                        isNull("metadata.deletionTimestamp")))
                                .build(),
                        Sort.by(Sort.Direction.DESC, "spec.priority", "metadata.creationTimestamp"))
                .filter(link -> matches(link, group, keyword))
                .collectList()
                .map(list -> new ListResult<>(page, size, list.size(), slice(list, page, size)));
    }

    @Override
    public Mono<MiniProgramLink> getByName(String name) {
        return client.fetch(MiniProgramLink.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("链接不存在")));
    }

    @Override
    public Mono<MiniProgramLink> create(MiniProgramLink link) {
        return validate(link)
                .then(Mono.defer(() -> {
                    if (link.getSpec().getSource() == null) {
                        // 手动添加（D26）：来源由操作自动设置，不手动填写
                        link.getSpec().setSource("manual");
                    }
                    Metadata metadata = new Metadata();
                    metadata.setName(generateName());
                    metadata.setCreationTimestamp(Instant.now());
                    link.setMetadata(metadata);
                    return client.create(link);
                }));
    }

    @Override
    public Mono<MiniProgramLink> update(MiniProgramLink link) {
        String name = link.getMetadata().getName();
        return client.fetch(MiniProgramLink.class, name)
                .switchIfEmpty(Mono.error(new NotFoundException("链接不存在")))
                .flatMap(existing -> validate(link).thenReturn(existing))
                .flatMap(existing -> {
                    // 来源由操作决定，编辑不修改（D26）
                    String source = existing.getSpec() == null
                            ? null : existing.getSpec().getSource();
                    existing.setSpec(link.getSpec());
                    if (existing.getSpec() != null) {
                        existing.getSpec().setSource(source);
                    }
                    return client.update(existing);
                });
    }

    @Override
    public Mono<Void> delete(String name) {
        return client.fetch(MiniProgramLink.class, name)
                .flatMap(client::delete)
                .then();
    }

    @Override
    public Mono<List<GroupOption>> listGroups(Boolean visible) {
        return listAllFiltered(visible, null)
                .collectList()
                .flatMap(this::toGroupOptions);
    }

    @Override
    public Mono<List<MiniProgramLinkGroupVo>> listGrouped(Boolean visible, String keyword) {
        return listAllFiltered(visible, keyword)
                .collectList()
                .flatMap(links -> client.listAll(MiniProgramLinkGroup.class,
                                // 公开读路径：排除删除中对象（决策 D7）
                                ListOptions.builder()
                                        .fieldQuery(isNull("metadata.deletionTimestamp"))
                                        .build(),
                                Sort.by(Sort.Direction.DESC, "spec.priority",
                                        "metadata.creationTimestamp"))
                        .collectList()
                        .map(groups -> toGroupedVo(links, toDisplayNameMap(groups))));
    }

    /**
     * 按可见性与关键字过滤后的全量流（组内/组间排序在分组阶段完成）。
     * 供公开 listGroups/listGrouped 聚合使用：排除删除中对象（决策 D7）。
     */
    private Flux<MiniProgramLink> listAllFiltered(Boolean visible, String keyword) {
        var builder = ListOptions.builder();
        if (visible != null) {
            builder.fieldQuery(equal("spec.visible", visible));
        }
        builder.fieldQuery(isNull("metadata.deletionTimestamp"));
        ListOptions listOptions = builder.build();
        return client.listAll(MiniProgramLink.class, listOptions,
                Sort.by(Sort.Direction.DESC, "spec.priority", "metadata.creationTimestamp"))
                .filter(link -> matches(link, null, keyword));
    }

    /**
     * 分组选项：遍历已排序链接去重（组序与分组返回一致），displayName 缺失时用 name 兜底。
     */
    private Mono<List<GroupOption>> toGroupOptions(List<MiniProgramLink> links) {
        return client.listAll(MiniProgramLinkGroup.class, ListOptions.builder().build(),
                        Sort.by(Sort.Direction.DESC, "spec.priority",
                                "metadata.creationTimestamp"))
                .collectList()
                .map(groups -> {
                    Map<String, String> displayNames = toDisplayNameMap(groups);
                    List<GroupOption> options = new ArrayList<>();
                    for (MiniProgramLink link : links) {
                        if (link.getSpec() == null) {
                            continue;
                        }
                        String groupName = blankToEmpty(link.getSpec().getGroupName());
                        if (!groupName.isEmpty()
                                && options.stream().noneMatch(o -> o.name().equals(groupName))) {
                            options.add(new GroupOption(groupName,
                                    displayNames.getOrDefault(groupName, groupName)));
                        }
                    }
                    return options;
                });
    }

    /**
     * 分组聚合：组间按输入顺序（链接流已按 priority/创建时间倒序，故组按组内最大
     * priority 倒序），组内顺序天然正确；displayName 缺失时为空（消费端兜底「未分组」）。
     */
    private List<MiniProgramLinkGroupVo> toGroupedVo(List<MiniProgramLink> links,
            Map<String, String> displayNames) {
        Map<String, List<MiniProgramLink>> grouped = new LinkedHashMap<>();
        for (MiniProgramLink link : links) {
            String groupName = link.getSpec() == null ? ""
                    : blankToEmpty(link.getSpec().getGroupName());
            grouped.computeIfAbsent(groupName, k -> new ArrayList<>()).add(link);
        }
        List<MiniProgramLinkGroupVo> result = new ArrayList<>(grouped.size());
        grouped.forEach((groupName, groupLinks) -> result.add(
                MiniProgramLinkGroupVo.of(groupName,
                        displayNames.getOrDefault(groupName, ""), groupLinks)));
        return result;
    }

    private Map<String, String> toDisplayNameMap(List<MiniProgramLinkGroup> groups) {
        Map<String, String> map = new LinkedHashMap<>();
        for (MiniProgramLinkGroup group : groups) {
            if (group.getMetadata() != null && group.getSpec() != null) {
                map.put(group.getMetadata().getName(), group.getSpec().getDisplayName());
            }
        }
        return map;
    }

    /**
     * 校验与规整：displayName、miniProgramCode 必填（D5）；visible 默认 true（D2）；
     * priority 默认 0；screenshots 去空去重。
     */
    private Mono<Void> validate(MiniProgramLink link) {
        var spec = link.getSpec();
        if (spec == null) {
            return Mono.error(new IllegalArgumentException("链接数据不能为空"));
        }
        if (isBlank(spec.getDisplayName())) {
            return Mono.error(new IllegalArgumentException("小程序名称不能为空"));
        }
        if (isBlank(spec.getMiniProgramCode())) {
            return Mono.error(new IllegalArgumentException("太阳码不能为空"));
        }
        if (spec.getVisible() == null) {
            spec.setVisible(true);
        }
        if (spec.getPriority() == null) {
            spec.setPriority(0);
        }
        spec.setScreenshots(distinctNonBlank(spec.getScreenshots()));
        return Mono.empty();
    }

    private static boolean matches(MiniProgramLink link, String group, String keyword) {
        if (link.getSpec() == null) {
            return false;
        }
        var spec = link.getSpec();
        if (!isBlank(group) && !group.equals(spec.getGroupName())) {
            return false;
        }
        if (!isBlank(keyword) && !(contains(spec.getDisplayName(), keyword)
                || contains(spec.getDescription(), keyword)
                || contains(spec.getAuthorName(), keyword)
                || contains(spec.getGroupName(), keyword))) {
            return false;
        }
        return true;
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
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
        return "mini-program-link-" + System.currentTimeMillis() + "-"
                + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x10000));
    }
}

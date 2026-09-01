package cn.ialley.unihalo.services;

import java.util.List;

import cn.ialley.unihalo.scheme.MiniProgramLink;
import cn.ialley.unihalo.vo.GroupOption;
import cn.ialley.unihalo.vo.MiniProgramLinkGroupVo;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 友情链接-小程序链接服务（决策 D1/D2/D8）。
 *
 * @author 小莫唐尼
 */
public interface MiniProgramLinkService {

    /**
     * 分页列表；group/visible/keyword 非空时分别过滤，keyword 模糊匹配
     * 名称/描述/作者/分组，排序 spec.priority 倒序 + 创建时间倒序。
     */
    Mono<ListResult<MiniProgramLink>> list(String group, Boolean visible,
            String keyword, int page, int size);

    /**
     * 按 name 查询；不存在时抛 {@link cn.ialley.unihalo.exception.NotFoundException}。
     */
    Mono<MiniProgramLink> getByName(String name);

    /**
     * 新建（displayName、miniProgramCode 必填；visible 默认 true、priority 默认 0）。
     */
    Mono<MiniProgramLink> create(MiniProgramLink link);

    /**
     * 更新（校验同上，完整覆盖 spec）。
     */
    Mono<MiniProgramLink> update(MiniProgramLink link);

    Mono<Void> delete(String name);

    /**
     * 分组选项（公开 /types）：聚合可见链接引用的分组，返回 [{name, displayName}]，
     * 顺序与分组返回一致（按组内最大 priority 倒序）；不包含未分组。
     */
    Mono<List<GroupOption>> listGroups(Boolean visible);

    /**
     * 分组聚合（对标 plugin-links LinkGroupVo）：按 spec.groupName 分组（空=未分组），
     * 组内嵌 displayName；组间按组内最大 priority 倒序，组内按 priority 倒序 +
     * 创建时间倒序；keyword 非空时先过滤。
     */
    Mono<List<MiniProgramLinkGroupVo>> listGrouped(Boolean visible, String keyword);
}

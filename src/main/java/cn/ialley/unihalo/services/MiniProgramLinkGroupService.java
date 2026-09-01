package cn.ialley.unihalo.services;

import java.util.List;

import cn.ialley.unihalo.scheme.MiniProgramLinkGroup;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 友情链接-分组服务（对标 plugin-links LinkGroup；删除不影响已关联链接）。
 *
 * @author 小莫唐尼
 */
public interface MiniProgramLinkGroupService {

    /**
     * 分页列表；keyword 模糊匹配分组名称；排序 priority 倒序 + 创建时间倒序。
     */
    Mono<ListResult<MiniProgramLinkGroup>> list(String keyword, int page, int size);

    /**
     * 全量列表（分组数量少，供控制台标签条/下拉使用），排序同上。
     */
    Mono<List<MiniProgramLinkGroup>> listAll();

    /**
     * 新建（displayName 必填；priority 默认 0）。
     */
    Mono<MiniProgramLinkGroup> create(MiniProgramLinkGroup group);

    /**
     * 更新（校验同上，完整覆盖 spec）。
     */
    Mono<MiniProgramLinkGroup> update(MiniProgramLinkGroup group);

    Mono<Void> delete(String name);
}

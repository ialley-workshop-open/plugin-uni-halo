package cn.ialley.unihalo.services;

import java.util.List;

import cn.ialley.unihalo.scheme.NoticeType;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 公告类型服务（决策 D9/D11）。
 *
 * @author 小莫唐尼
 */
public interface NoticeTypeService {

    /**
     * 分页列表；keyword 模糊匹配名称，排序 spec.priority 倒序 + 创建时间倒序。
     */
    Mono<ListResult<NoticeType>> list(String keyword, int page, int size);

    /**
     * 按 name 查询；不存在返回 empty（供公告展示内嵌类型信息时使用）。
     */
    Mono<NoticeType> getByName(String name);

    /**
     * 新建（displayName 必填）。
     */
    Mono<NoticeType> create(NoticeType noticeType);

    /**
     * 更新（displayName 必填）。
     */
    Mono<NoticeType> update(NoticeType noticeType);

    Mono<Void> delete(String name);

    /**
     * 按 names 顺序保存排序（拖拽后调用；names[0] 最靠前，priority 从大到小赋值）。
     */
    Mono<Void> sort(List<String> names);
}

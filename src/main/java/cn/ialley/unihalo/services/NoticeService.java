package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.Notice;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 通知公告服务（多条，决策 D1/D2/D12）。
 *
 * @author 小莫唐尼
 */
public interface NoticeService {

    /**
     * 分页列表；status/type 非空时分别按状态、类型过滤，keyword 模糊匹配标题/摘要。
     * sort：date_desc 最新在前（默认）/ date_asc 最早在前 / type 按类型分组。
     */
    Mono<ListResult<Notice>> list(String status, String type, String keyword,
            int page, int size, String sort);

    /**
     * 按 name 查询；不存在时抛 {@link cn.ialley.unihalo.exception.NotFoundException}。
     */
    Mono<Notice> getByName(String name);

    /**
     * 新建（title 必填；summary 空时自动生成；publishTime 发布时记录）。
     */
    Mono<Notice> create(Notice notice);

    /**
     * 更新（title 必填；summary 空时自动重新生成；publishTime 保留已有发布时间）。
     */
    Mono<Notice> update(Notice notice);

    Mono<Void> delete(String name);

    /**
     * 最新一条已发布公告（priority 最大、发布时间最新）；无已发布公告返回 empty。
     */
    Mono<Notice> getLatestPublished();
}

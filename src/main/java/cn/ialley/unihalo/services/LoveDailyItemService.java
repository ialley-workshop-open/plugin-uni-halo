package cn.ialley.unihalo.services;

import cn.ialley.unihalo.scheme.LoveDailyItem;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 恋爱清单服务（原 loveDaily.list[] 抽离为独立模型）
 *
 * @author 小莫唐尼
 */
public interface LoveDailyItemService {

    /**
     * 分页列表；status 精确筛选，keyword 模糊匹配标题。
     * 管理端使用，保留删除中对象（决策 D7）。
     */
    Mono<ListResult<LoveDailyItem>> list(String status, String keyword, int page, int size);

    /**
     * 公开列表（keyword 恒空，排除删除中对象，决策 D7）。公开读路径专用。
     */
    Mono<ListResult<LoveDailyItem>> listPublic(String status, int page, int size);

    Mono<LoveDailyItem> getByName(String name);

    /**
     * 新建（title 必填；status=complete 时 completeDate 必填）。
     */
    Mono<LoveDailyItem> create(LoveDailyItem item);

    /**
     * 更新（校验同上）。
     */
    Mono<LoveDailyItem> update(LoveDailyItem item);

    Mono<Void> delete(String name);
}

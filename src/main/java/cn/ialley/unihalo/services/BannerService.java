package cn.ialley.unihalo.services;

import java.util.List;

import cn.ialley.unihalo.scheme.Banner;
import cn.ialley.unihalo.vo.BannerCandidateVo;
import cn.ialley.unihalo.vo.BannerDetailVo;
import cn.ialley.unihalo.vo.BannerListVo;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;

/**
 * 首页轮播图服务（归一化条目模型，设计见 .docs/banner-design.md）。
 *
 * <p>source 由服务端按 postId 非空自动判定（决策 D2）；文章模式服务端拉取
 * Post/User 回填快照字段（决策 D3）；公开列表脱敏不含 content/remark（决策 D5）。</p>
 *
 * @author 小莫唐尼
 */
public interface BannerService {

    /**
     * 分页列表；source 非空时按来源（post/custom）过滤，keyword 模糊匹配标题/备注。
     * sort：manual 手动排序（priority 降序，默认）/ date_desc 日期最新在前 / date_asc 日期最早在前。
     */
    Mono<ListResult<Banner>> list(String source, String keyword, int page, int size, String sort);

    /**
     * 按 name 查询；不存在时抛 {@link cn.ialley.unihalo.exception.NotFoundException}。
     */
    Mono<Banner> getByName(String name);

    /**
     * 新建（title 必填；source 自动判定；文章模式服务端快照）。
     */
    Mono<Banner> create(Banner banner);

    /**
     * 更新（规则同 create，source 重新判定）。
     */
    Mono<Banner> update(Banner banner);

    Mono<Void> delete(String name);

    /**
     * 同步文章快照：重新拉取 Post/User 覆盖 title/cover/date/author 字段（仅文章来源）。
     * 非文章来源或文章已删除时抛 IllegalArgumentException。
     */
    Mono<Banner> syncSnapshot(String name);

    /**
     * 按 names 顺序保存排序（拖拽后调用；names[0] 最靠前，priority 从大到小赋值）。
     */
    Mono<Void> sort(List<String> names);

    /**
     * 文章候选（仅已发布，Post 按创建时间倒序，内存关键字过滤 + 手动分页）。
     */
    Mono<ListResult<BannerCandidateVo>> listCandidates(String keyword, int page, int size);

    /**
     * 公开有序列表（全量，脱敏：不含 content/remark）。
     */
    Mono<List<BannerListVo>> listPublic();

    /**
     * 公开详情（含 content 富文本正文）；不存在时抛 NotFoundException。
     */
    Mono<BannerDetailVo> getPublicByName(String name);
}

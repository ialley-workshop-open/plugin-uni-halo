package cn.ialley.unihalo.services;

import cn.ialley.unihalo.enums.CandidateType;
import cn.ialley.unihalo.scheme.AuditDataConfig;
import cn.ialley.unihalo.vo.AuditDataCandidateResult;
import cn.ialley.unihalo.vo.AuditDataConfigDetail;
import reactor.core.publisher.Mono;

/**
 * 审核配置服务（单例模型 audit-data-config）。
 *
 * <p>承载「审核模式模拟数据」的选中引用列表：审核模式开启后小程序端仅展示
 * 此处挑选的站内真实数据。保存时服务端校验并剔除失效引用；候选数据按类型
 * 映射到外部扩展 GVK 查询（插件未安装时容错返回 pluginMissing）。</p>
 *
 * @author 小莫唐尼
 */
public interface AuditDataService {

    /**
     * 读取单例配置（不存在时返回默认空结构，不落库）。
     */
    Mono<AuditDataConfig> get();

    /**
     * 读取配置详情（管理端使用）：原始配置 + 各类型已选条目的展示信息。
     * 已选但详情缺失的 name 即「已失效」（引用对象已删除），由前端标记。
     */
    Mono<AuditDataConfigDetail> getDetail();

    /**
     * 读取剔除失效引用后的有效配置（公开接口使用：小程序端拿到的一定是存在的引用）。
     */
    Mono<AuditDataConfig> getEffective();

    /**
     * 保存单例配置：校验并剔除失效引用后整体替换（不存在则创建）。
     */
    Mono<AuditDataConfig> save(AuditDataConfig config);

    /**
     * 候选数据查询（选择器数据源），按类型映射 GVK，内存关键字过滤 + 分页。
     *
     * @param type    候选类型
     * @param keyword 关键字（空则不过滤）
     * @param page    页码（从 1 开始）
     * @param size    每页大小
     */
    Mono<AuditDataCandidateResult> listCandidates(CandidateType type, String keyword,
            int page, int size);
}

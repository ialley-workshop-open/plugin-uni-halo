package cn.ialley.unihalo.vo;

import java.util.List;
import java.util.Map;

import cn.ialley.unihalo.enums.CandidateType;
import cn.ialley.unihalo.scheme.AuditDataConfig;

/**
 * 审核配置详情（管理端 GET /audit-data 返回）。
 *
 * <p>除原始配置（含选中引用的展示字段快照）外，附带各类型已选条目的最新详情，
 * 供控制台已选列表刷新展示；已选但详情中缺失的 name 即「已失效」（引用对象已删除）。</p>
 *
 * @param config     原始单例配置（含选中引用的快照）
 * @param selections 各类型已选条目的最新详情（key 为候选类型枚举名）
 * @author 小莫唐尼
 */
public record AuditDataConfigDetail(AuditDataConfig config,
        Map<CandidateType, List<AuditDataConfig.AuditDataRef>> selections) {
}

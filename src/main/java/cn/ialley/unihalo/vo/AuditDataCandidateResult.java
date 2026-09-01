package cn.ialley.unihalo.vo;

import java.util.List;

import cn.ialley.unihalo.scheme.AuditDataConfig;

/**
 * 审核配置-候选数据分页结果。
 *
 * @param items        当前页候选项（name + 展示字段快照，可直接用于已选列表渲染）
 * @param page         当前页码（从 1 开始）
 * @param size         每页大小
 * @param total        过滤后总数
 * @param pluginMissing 数据源插件未安装/扩展未注册（UI 提示「请先安装 XX 插件」）
 * @author 小莫唐尼
 */
public record AuditDataCandidateResult(List<AuditDataConfig.AuditDataRef> items, int page,
        int size, long total, boolean pluginMissing) {
}

import { Dialog, Toast } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";

/**
 * 统一删除流程收口（设计见 .docs/deletion-finalizer-design.md 决策 D8）。
 *
 * 将各列表页重复的「Dialog.warning 确认 → 调删除接口 → Toast → 失效刷新」样板
 * 收敛为一处：Dialog.warning 确认后并发调用 doDelete，成功 Toast「删除成功」，
 * 失败 Toast 后端 message（业务校验 400，如 AppInfo「已有已发布版本不可删除」），
 * 无论成败 finally 中 invalidateQueries 触发列表刷新（删除中对象由
 * deletingRefetchInterval 条件轮询兜底收敛）。
 *
 * @param queryKey 与列表 useQuery 相同的 queryKey（前缀即可，命中所有分页变体）
 */
export function useDeletionFlow(queryKey: unknown[]) {
  const queryClient = useQueryClient();

  const confirmDelete = (opts: {
    title: string;
    names: string[];
    doDelete: (name: string) => Promise<unknown>;
    description?: string;
    onSuccess?: () => void;
  }) => {
    Dialog.warning({
      title: opts.title,
      description: opts.description ?? "该操作不可恢复。",
      confirmType: "danger",
      confirmText: "确定",
      cancelText: "取消",
      onConfirm: async () => {
        try {
          await Promise.all(opts.names.map(opts.doDelete));
          opts.onSuccess?.();
          Toast.success("删除成功");
        } catch (error) {
          Toast.error((error as Error).message);
        } finally {
          queryClient.invalidateQueries({ queryKey });
        }
      },
    });
  };

  return { confirmDelete };
}

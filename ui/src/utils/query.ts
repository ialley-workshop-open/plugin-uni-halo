import type { Metadata } from "@halo-dev/api-client";

/**
 * 列表 useQuery 条件轮询函数（设计见 .docs/deletion-finalizer-design.md 决策 D8）。
 *
 * 存在「删除中」对象（metadata.deletionTimestamp 非空）时每 1000ms 自动重取列表，
 * 所有删除中对象消失后返回 false 自动停止轮询。对齐 plugin-vote 管理列表删除体验：
 * 删除成功后对象先短暂停留在列表（显示「删除中」），随后自动刷新收敛。
 *
 * 用法：useQuery({ ..., refetchInterval: deletingRefetchInterval })
 *
 * 注意：不要写成泛型函数，否则 TypeScript 会从回调参数反推 useQuery 的
 * queryFn 返回类型，导致列表数据推断被污染（items 丢失真实元素类型）。
 */
export function deletingRefetchInterval(data?: {
  items?: Array<{ metadata?: Metadata }>;
} | null): number | false {
  return data?.items?.some((item) => item.metadata?.deletionTimestamp) ? 1000 : false;
}

package cn.ialley.unihalo.vo;

/**
 * 分组选项（公开 /types 接口返回，供小程序端筛选/分组标题映射）。
 *
 * @param name        分组 metadata.name
 * @param displayName 分组显示名
 * @author 小莫唐尼
 */
public record GroupOption(String name, String displayName) {
}

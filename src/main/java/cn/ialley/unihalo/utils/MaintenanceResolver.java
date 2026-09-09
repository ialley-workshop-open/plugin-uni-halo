package cn.ialley.unihalo.utils;

import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * 维护模式状态判定工具（纯函数，无副作用）。
 *
 * <p>维护状态由「enabled 安排开关 + startTime/endTime 时间窗口（RFC3339 UTC 字符串，
 * 均可空）」与当前时间计算得出（stateless：无定时任务、不物理清库，
 * 「到点自动结束」即输出端判定为 {@link MaintenanceStatus#NONE}）。规则：</p>
 * <ul>
 *   <li>{@code enabled != true} → NONE（未维护）；</li>
 *   <li>{@code endTime} 已到（now ≥ endTime）→ NONE（本次维护已按计划自动结束）；</li>
 *   <li>{@code startTime} 在未来（now &lt; startTime）→ SCHEDULED（维护预告，倒计时至开始）；</li>
 *   <li>其余（startTime 为空/已到，endTime 为空/未到）→ ACTIVE（维护中）。</li>
 * </ul>
 *
 * <p>边界语义：到 {@code startTime} 整点即视为开始、到 {@code endTime} 整点即视为结束（含等号）。
 * 时间字符串非法（非 RFC3339）时按未设置处理，公开输出不因脏数据抛错。
 * 设计见 {@code .docs/maintenance-config-design.md} §4。</p>
 *
 * @author 小莫唐尼
 */
public final class MaintenanceResolver {

    private MaintenanceResolver() {
    }

    /**
     * 计算维护状态。
     *
     * @param enabled   安排开关（可为 null，视为 false）
     * @param startTime 维护开始时间 RFC3339 UTC 字符串（可为 null/空/非法）
     * @param endTime   预计恢复时间 RFC3339 UTC 字符串（可为 null/空/非法）
     * @param now       当前时刻（判定权威时钟，便于测试注入）
     */
    public static MaintenanceStatus resolve(Boolean enabled, String startTime,
            String endTime, Instant now) {
        if (!Boolean.TRUE.equals(enabled)) {
            return MaintenanceStatus.NONE;
        }
        Instant start = parseTime(startTime);
        Instant end = parseTime(endTime);
        if (end != null && !end.isAfter(now)) {
            // endTime 已到（含等号）→ 本次维护已按计划自动结束
            return MaintenanceStatus.NONE;
        }
        if (start != null && start.isAfter(now)) {
            // startTime 在未来 → 维护预告
            return MaintenanceStatus.SCHEDULED;
        }
        return MaintenanceStatus.ACTIVE;
    }

    /**
     * 解析 RFC3339 UTC 时间字符串为 Instant；空/非法返回 null（按未设置处理，
     * 公开输出不因脏数据抛错）。
     */
    public static Instant parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 维护状态：NONE 未维护（含已到点自动结束）/ SCHEDULED 维护预告（倒计时至 startTime）/
     * ACTIVE 维护中（倒计时至 endTime）。
     */
    public enum MaintenanceStatus {
        NONE,
        SCHEDULED,
        ACTIVE
    }
}

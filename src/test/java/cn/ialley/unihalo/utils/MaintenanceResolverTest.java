package cn.ialley.unihalo.utils;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import cn.ialley.unihalo.utils.MaintenanceResolver.MaintenanceStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 维护状态判定纯函数单测：§4/§10.1 状态矩阵 + 边界语义（startTime 整点即开始、
 * endTime 整点即结束）+ 非法时间兜底。
 *
 * @author 小莫唐尼
 */
class MaintenanceResolverTest {

    private static final Instant NOW = Instant.parse("2026-09-04T12:00:00Z");
    private static final String START_FUTURE = "2026-09-05T12:00:00Z";
    private static final String START_PAST = "2026-09-03T12:00:00Z";
    private static final String END_FUTURE = "2026-09-06T12:00:00Z";
    private static final String END_PAST = "2026-09-03T12:00:00Z";

    @Test
    void disabledAlwaysNone() {
        assertEquals(MaintenanceStatus.NONE, resolve(false, null, null));
        assertEquals(MaintenanceStatus.NONE, resolve(null, START_FUTURE, END_FUTURE));
    }

    @Test
    void noWindowMeansImmediateActive() {
        // enabled=true 且 startTime/endTime 均空 → 立即维护中，持续至手动关闭
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, null, null));
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, "", ""));
    }

    @Test
    void startedWithoutEndTimeStaysActive() {
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, null, END_FUTURE));
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, START_PAST, null));
    }

    @Test
    void autoEndedWhenEndTimeReached() {
        // endTime 已到（含等号）→ NONE（本次维护已按计划自动结束）
        assertEquals(MaintenanceStatus.NONE, resolve(true, null, END_PAST));
        assertEquals(MaintenanceStatus.NONE, resolve(true, START_PAST, END_PAST));
        assertEquals(MaintenanceStatus.NONE, resolve(true, START_PAST, "2026-09-04T12:00:00Z"));
    }

    @Test
    void scheduledWhenStartTimeInFuture() {
        assertEquals(MaintenanceStatus.SCHEDULED, resolve(true, START_FUTURE, null));
        assertEquals(MaintenanceStatus.SCHEDULED, resolve(true, START_FUTURE, END_FUTURE));
    }

    @Test
    void activeBetweenStartAndEnd() {
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, START_PAST, END_FUTURE));
        // startTime 整点时刻即视为开始（含等号）
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, "2026-09-04T12:00:00Z", END_FUTURE));
    }

    @Test
    void malformedTimesTreatedAsUnset() {
        // 脏数据不抛错：非法时间按未设置处理（此时无窗口 → 立即维护中）
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, "not-a-time", null));
        assertEquals(MaintenanceStatus.ACTIVE, resolve(true, "2026-99-99T00:00:00Z", null));
        // enabled=false 时非法时间同样安全返回 NONE
        assertEquals(MaintenanceStatus.NONE, resolve(false, "not-a-time", "not-a-time"));
    }

    private MaintenanceStatus resolve(Boolean enabled, String startTime, String endTime) {
        return MaintenanceResolver.resolve(enabled, startTime, endTime, NOW);
    }
}

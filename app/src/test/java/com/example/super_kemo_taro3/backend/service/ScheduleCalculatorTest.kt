package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.service.LessonTime
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/** FRI→日曜日、過去時刻の翌週化、日曜日当日・月曜日からの停止期限、不正キー、旧アラーム識別子の互換性を6件で検証する。 */
class ScheduleCalculatorTest {
    // 実行日とは無関係に、固定した2026年10月の日付で曜日と境界を確認する。
    private fun date(day: Int, hour: Int, minute: Int = 0): Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, day, hour, minute)
    }.timeInMillis

    @Test fun fridayKeyRunsOnSunday() {
        val slot = requireNotNull(TimetableRules.parseSlot("FRI_1"))
        assertEquals(Calendar.SUNDAY, slot.calendarDay)
        val scheduled = ScheduleCalculator.nextOccurrence(slot.calendarDay, LessonTime(8, 40), date(10, 12))
        assertEquals(date(11, 8, 40), scheduled)
    }

    @Test fun elapsedSundayTimeMovesToNextWeek() {
        // 同じ曜日でも開始を過ぎていれば翌週へ送る。
        val scheduled = ScheduleCalculator.nextOccurrence(Calendar.SUNDAY, LessonTime(8, 40), date(11, 9))
        assertEquals(date(18, 8, 40), scheduled)
    }

    @Test fun pauseOnSundayEndsTheSameDay() {
        val end = Calendar.getInstance().apply { timeInMillis = ScheduleCalculator.sundayEnd(date(11, 9)) }
        assertEquals(11, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, end.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, end.get(Calendar.MINUTE))
        assertEquals(59, end.get(Calendar.SECOND))
        assertEquals(999, end.get(Calendar.MILLISECOND))
    }

    @Test fun pauseOnMondayEndsTheFollowingSunday() {
        val end = Calendar.getInstance().apply { timeInMillis = ScheduleCalculator.sundayEnd(date(12, 9)) }
        assertEquals(18, end.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SUNDAY, end.get(Calendar.DAY_OF_WEEK))
    }

    @Test fun malformedAndOutOfRangeSlotsAreIgnored() {
        listOf("", "MON", "SUN_1", "MON_0", "MON_9", "MON_invalid", "MON_1_extra")
            .forEach { assertNull(TimetableRules.parseSlot(it)) }
        assertEquals(Calendar.MONDAY, TimetableRules.parseSlot("MON_8")?.calendarDay)
    }

    @Test fun alarmIdentifiersStayCompatible() {
        assertEquals("FRI_1".hashCode(), AlarmContract.requestCode("FRI_1", true))
        assertEquals("FRI_1".hashCode() + 1000, AlarmContract.requestCode("FRI_1", false))
        assertEquals(604800000L, ScheduleCalculator.WEEK_IN_MILLIS)
    }
}


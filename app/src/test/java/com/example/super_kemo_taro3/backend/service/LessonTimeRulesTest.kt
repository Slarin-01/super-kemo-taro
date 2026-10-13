package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.service.LessonTimeDefaults
import com.example.super_kemo_taro3.backend.service.LessonTimeRules.formatTime
import com.example.super_kemo_taro3.backend.service.LessonTimeRules.timeToMinutes
import com.example.super_kemo_taro3.backend.service.LessonTimeRules.invalidTimeIndices

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** 授業時刻の規則を6件で確認する。既定値、逆転、重複、隣接、無効値、日付の境界を検証する。 */
class LessonTimeRulesTest {
    @Test
    fun defaultsAreValid() {
        assertTrue(invalidTimeIndices(LessonTimeDefaults.editorFields.map { formatTime(it.defaultTime) }).isEmpty())
        assertEquals("time_start_1", LessonTimeDefaults.editorFields.first().saveKey)
        assertEquals("time_end_8", LessonTimeDefaults.editorFields.last().saveKey)
    }

    @Test
    fun reversedAndZeroLengthRangesMarkEndField() {
        // 開始・終了の2要素では、終了欄の添字1にエラーが付く。
        assertEquals(setOf(1), invalidTimeIndices(listOf("09:30", "08:40")))
        assertEquals(setOf(1), invalidTimeIndices(listOf("09:30", "09:30")))
    }

    @Test
    fun overlappingPeriodsMarkFollowingStartField() {
        // 次の時限の開始欄は添字2。重なった場合はこちらを修正対象にする。
        assertEquals(setOf(2), invalidTimeIndices(listOf("08:40", "09:30", "09:20", "10:30")))
    }

    @Test
    fun adjacentPeriodsAreAllowed() {
        assertTrue(invalidTimeIndices(listOf("08:40", "09:30", "09:30", "10:30")).isEmpty())
    }

    @Test
    fun invalidSavedValuesCannotBeSavedAgain() {
        listOf("", "invalid", "25:00", "08:60", "-1:30", "08:40:00").forEach {
            assertNull(timeToMinutes(it))
            assertEquals(setOf(0), invalidTimeIndices(listOf(it, "09:30")))
        }
    }

    @Test
    fun timeFormattingAndDayBoundaries() {
        assertEquals("08:05", formatTime(8, 5))
        assertEquals(0, timeToMinutes("00:00"))
        assertEquals(1439, timeToMinutes("23:59"))
    }
}


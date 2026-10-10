package com.example.super_kemo_taro3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeSettingsTest {
    @Test
    fun defaultsAreValid() {
        assertTrue(invalidTimeIndices(timeSettings.map { it.defaultTime }).isEmpty())
        assertEquals("time_start_1", timeSettings.first().saveKey)
        assertEquals("time_end_8", timeSettings.last().saveKey)
    }

    @Test
    fun reversedAndZeroLengthRangesMarkEndField() {
        assertEquals(setOf(1), invalidTimeIndices(listOf("09:30", "08:40")))
        assertEquals(setOf(1), invalidTimeIndices(listOf("09:30", "09:30")))
    }

    @Test
    fun overlappingPeriodsMarkFollowingStartField() {
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

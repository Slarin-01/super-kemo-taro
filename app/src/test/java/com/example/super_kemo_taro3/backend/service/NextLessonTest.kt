package com.example.super_kemo_taro3.backend.service

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class NextLessonTest {
    private val settings = MemorySettingsRepository()
    private val alarms = RecordingAlarmScheduler()
    private val timetable = TimetableService(settings, alarms)

    private fun date(day: Int, hour: Int, minute: Int = 0): Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, day, hour, minute)
    }.timeInMillis

    @Test fun selectsTheNearestLessonRegardlessOfStoredOrder() {
        settings.selected = linkedSetOf("TUE_1", "MON_3", "MON_1", "MON_2")

        val next = requireNotNull(timetable.readNextLesson(date(19, 8)))

        assertEquals("MON_1", next.slot.key)
        assertEquals(date(19, 8, 40), next.startsAt)
        assertEquals(LessonTime(9, 30), next.endTime)
        assertNull(next.subject)
    }

    @Test fun skipsLessonsThatHaveAlreadyStarted() {
        settings.selected = setOf("MON_1", "MON_2")

        val next = requireNotNull(timetable.readNextLesson(date(19, 8, 41)))

        assertEquals("MON_2", next.slot.key)
        assertEquals(date(19, 9, 40), next.startsAt)
    }

    @Test fun fridayEveningAdvancesAcrossTheWeekend() {
        settings.selected = setOf("FRI_8", "MON_1")

        val next = requireNotNull(timetable.readNextLesson(date(23, 18)))

        assertEquals("MON_1", next.slot.key)
        assertEquals(date(26, 8, 40), next.startsAt)
    }

    @Test fun anElapsedWeeklyLessonMovesToTheFollowingWeek() {
        settings.selected = setOf("FRI_1")

        assertEquals(date(30, 8, 40), timetable.readNextLesson(date(23, 9))?.startsAt)
    }

    @Test fun savedTimesDetermineBothTheOrderAndTheDisplayedTimeRange() {
        settings.selected = setOf("MON_1", "MON_2")
        settings.times["time_start_1"] = "11:00"
        settings.times["time_end_1"] = "11:50"
        settings.times["time_start_2"] = "12:00"
        settings.times["time_end_2"] = "12:50"

        val next = requireNotNull(timetable.readNextLesson(date(19, 10)))

        assertEquals("MON_1", next.slot.key)
        assertEquals(date(19, 11), next.startsAt)
        assertEquals(LessonTime(11, 50), next.endTime)
        settings.times["time_start_2"] = "10:30"
        assertEquals("MON_2", timetable.readNextLesson(date(19, 10))?.slot?.key)
    }

    @Test fun includesTheExactStartAndAdvancesImmediatelyAfterIt() {
        settings.selected = setOf("MON_1", "MON_2")
        val start = date(19, 8, 40)

        assertEquals("MON_1", timetable.readNextLesson(start)?.slot?.key)
        assertEquals("MON_2", timetable.readNextLesson(start + 1L)?.slot?.key)
    }

    @Test fun missingOrInvalidSlotsHaveNoNextLesson() {
        assertNull(timetable.readNextLesson(date(19, 8)))

        settings.selected = setOf("invalid", "MON_0", "MON_9", "SUN_1")
        assertNull(timetable.readNextLesson(date(19, 8)))
    }

    @Test fun defaultsMatchAlarmsWithoutRequiringPermissionOrChangingSettings() {
        settings.selected = setOf("FRI_5")
        alarms.allowed = false

        val next = requireNotNull(timetable.readNextLesson(date(23, 8)))

        assertEquals(date(23, 13, 25), next.startsAt)
        assertEquals(LessonTime(14, 15), next.endTime)
        assertTrue(settings.times.isEmpty())
        assertTrue(alarms.registrations.isEmpty())
    }

    @Test fun replacementPersistsSubjectsForSelectedSlotsEvenWithoutPermission() {
        settings.subjects = mapOf("FRI_5" to "以前の教科")
        alarms.allowed = false

        assertTrue(timetable.replaceSelectedSlots(
            setOf("MON_1", "MON_2", "BAD"),
            mapOf("MON_1" to " 情報処理 ", "MON_2" to " ", "FRI_5" to "以前の教科", "BAD" to "無効")
        ))

        assertEquals(mapOf("MON_1" to "情報処理"), settings.subjects)
        assertEquals("情報処理", timetable.readNextLesson(date(19, 8))?.subject)
        assertNull(timetable.readNextLesson(date(19, 9))?.subject)

        timetable.replaceSelectedSlots(emptySet())
        assertTrue(settings.subjects.isEmpty())
        assertNull(timetable.readNextLesson(date(19, 8)))
    }
}

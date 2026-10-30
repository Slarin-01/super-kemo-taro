package com.example.super_kemo_taro3.frontend.widget

import com.example.super_kemo_taro3.backend.repository.CourseLinks
import com.example.super_kemo_taro3.backend.repository.Note
import com.example.super_kemo_taro3.backend.service.LessonTime
import com.example.super_kemo_taro3.backend.service.MemorySettingsRepository
import com.example.super_kemo_taro3.backend.service.RecordingAlarmScheduler
import com.example.super_kemo_taro3.backend.service.TimetableService
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class TimetableWidgetStateTest {
    private val settings = MemorySettingsRepository()
    private val alarms = RecordingAlarmScheduler().apply { allowed = false }
    private val timetable = TimetableService(settings, alarms)

    private fun date(day: Int, hour: Int = 0, minute: Int = 0): Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, day, hour, minute)
    }.timeInMillis

    @Test fun eightSquaresShowOnlyTodayIncludingElapsedLessonsAndGaps() {
        settings.selected = setOf("MON_1", "MON_3", "MON_8", "TUE_2", "BAD")

        val state = TimetableWidgetState.read(timetable, date(26, 10))

        assertEquals(listOf(1, null, 3, null, null, null, null, 8), state.periods.map { it?.slot?.lesson })
        assertEquals(date(26), state.periodsDate)
        assertEquals("MON_3", state.nextLesson?.slot?.key)
        assertEquals(date(26, 10, 45) + 1, state.refreshAt)
        assertTrue(alarms.registrations.isEmpty())
    }

    @Test fun nextLessonAdvancesImmediatelyAfterItsStartButTodaySquaresRemain() {
        settings.selected = setOf("MON_1", "MON_2")
        val start = date(26, 8, 40)
        val before = TimetableWidgetState.read(timetable, start)
        val after = TimetableWidgetState.read(timetable, before.refreshAt)

        assertEquals("MON_1", before.nextLesson?.slot?.key)
        assertEquals(start + 1, before.refreshAt)
        assertEquals("MON_2", after.nextLesson?.slot?.key)
        assertEquals(before.periods, after.periods)
    }

    @Test fun squaresSwitchToTomorrowExactlyWhenTodaysLastLessonEnds() {
        settings.selected = setOf("MON_1", "MON_3", "TUE_2")
        val end = date(26, 11, 35)

        val before = TimetableWidgetState.read(timetable, end - 1)

        assertEquals("TUE_2", before.nextLesson?.slot?.key)
        assertEquals(listOf(1, null, 3, null, null, null, null, null), before.periods.map { it?.slot?.lesson })
        assertEquals(date(26), before.periodsDate)
        assertEquals(end, before.refreshAt)

        val after = TimetableWidgetState.read(timetable, before.refreshAt)

        assertEquals(date(27), after.periodsDate)
        assertEquals(listOf(null, "TUE_2", null, null, null, null, null, null), after.periods.map { it?.slot?.key })
        assertEquals(date(27), after.refreshAt)
        assertEquals(after.periods, TimetableWidgetState.read(timetable, date(26, 18)).periods)

        val midnight = TimetableWidgetState.read(timetable, after.refreshAt)
        assertEquals(date(27), midnight.periodsDate)
        assertEquals(after.periods, midnight.periods)
        assertEquals(date(27, 9, 40) + 1, midnight.refreshAt)
    }

    @Test fun fridaySwitchesToMondayExactlyAtTheLastLessonEnd() {
        settings.selected = setOf("FRI_8", "MON_1")
        val end = date(30, 17, 20)
        val before = TimetableWidgetState.read(timetable, end - 1)

        assertEquals("FRI_8", before.periods[7]?.slot?.key)
        assertEquals(date(30), before.periodsDate)
        assertEquals(end, before.refreshAt)

        val after = TimetableWidgetState.read(timetable, before.refreshAt)
        assertEquals(date(33), after.periodsDate)
        assertEquals(listOf("MON_1") + List(7) { null }, after.periods.map { it?.slot?.key })
        assertEquals("MON_1", after.nextLesson?.slot?.key)
        assertEquals(date(31), after.refreshAt)
    }

    @Test fun dayWithoutLessonsKeepsTodaysEmptySquaresUntilMidnight() {
        settings.selected = setOf("TUE_2")

        val state = TimetableWidgetState.read(timetable, date(26, 18))

        assertEquals(date(26), state.periodsDate)
        assertEquals(List(8) { null }, state.periods)
        assertEquals(date(27), state.refreshAt)
    }

    @Test fun mondaySquaresRemainThroughTheWeekendAndIntoMondayAcrossAMonthBoundary() {
        settings.selected = setOf("FRI_8", "MON_1")

        for (day in listOf(31, 32)) {
            val state = TimetableWidgetState.read(timetable, date(day, 12))

            assertEquals(date(33), state.periodsDate)
            assertEquals(listOf("MON_1") + List(7) { null }, state.periods.map { it?.slot?.key })
            assertEquals("MON_1", state.nextLesson?.slot?.key)
            assertEquals(date(33, 8, 40), state.nextLesson?.startsAt)
            assertEquals(date(day + 1), state.refreshAt)
        }
        val monday = TimetableWidgetState.read(timetable, date(33))
        assertEquals(date(33), monday.periodsDate)
        assertEquals("MON_1", monday.periods[0]?.slot?.key)
        assertEquals(date(33, 8, 40) + 1, monday.refreshAt)
    }

    @Test fun mondayWithoutLessonsIsNotSkippedForTuesdaysNextLesson() {
        settings.selected = setOf("FRI_8", "TUE_2")

        val state = TimetableWidgetState.read(timetable, date(30, 18))

        assertEquals(date(33), state.periodsDate)
        assertEquals(List(8) { null }, state.periods)
        assertEquals("TUE_2", state.nextLesson?.slot?.key)
        assertEquals(date(31), state.refreshAt)
    }

    @Test fun emptyTimetableStillRefreshesAtMidnight() {
        val state = TimetableWidgetState.read(timetable, date(31, 23, 59))

        assertNull(state.nextLesson)
        assertEquals(List(8) { null }, state.periods)
        assertEquals(date(32), state.refreshAt)
    }

    @Test fun editedTimesSubjectsAndClassroomsAreReadWithoutAlarmPermission() {
        settings.selected = setOf("MON_1")
        settings.subjects = mapOf("MON_1" to "情報処理")
        settings.courseLinks = mapOf("MON_1" to listOf(CourseLinks("情報処理", classroom = "A21教室")))
        settings.times["time_start_1"] = "11:00"
        settings.times["time_end_1"] = "11:50"

        val state = TimetableWidgetState.read(timetable, date(26, 10))

        assertEquals("情報処理", state.nextLesson?.subject)
        assertEquals("A21教室", state.nextLesson?.courseLinks?.single()?.classroom)
        assertEquals(LessonTime(11, 0), state.periods[0]?.startTime)
        assertEquals(LessonTime(11, 50), state.periods[0]?.endTime)
        assertEquals(date(26, 11) + 1, state.refreshAt)
        val during = TimetableWidgetState.read(timetable, state.refreshAt)
        assertEquals(date(26), during.periodsDate)
        assertEquals(date(26, 11, 50), during.refreshAt)
        val after = TimetableWidgetState.read(timetable, during.refreshAt)
        assertEquals(date(27), after.periodsDate)
        assertEquals(List(8) { null }, after.periods)
        assertTrue(alarms.registrations.isEmpty())
    }

    @Test fun noteIndicatorRequiresAMatchingSubjectAndAnUnexpiredNote() {
        settings.selected = setOf("MON_1")
        settings.subjects = mapOf("MON_1" to "情報処理")
        val now = date(26, 8)
        val ignored = listOf(
            Note("other", "課題", "本文", now, "数学"),
            Note("expired", "課題", "本文", now, "情報処理", now),
            Note("unassigned", "課題", "本文", now)
        )
        assertFalse(TimetableWidgetState.read(timetable, now, ignored).hasNextLessonNote)

        val active = Note("active", "持ち物", "ノート", now, " 情報処理 ")
        assertTrue(TimetableWidgetState.read(timetable, now, ignored + active).hasNextLessonNote)
        assertFalse(TimetableWidgetState.read(timetable, now, ignored).hasNextLessonNote)

        settings.selected = emptySet()
        assertFalse(TimetableWidgetState.read(timetable, now, listOf(active)).hasNextLessonNote)
    }

    @Test fun noteIndicatorMatchesEachCourseInASharedPeriod() {
        settings.selected = setOf("MON_1")
        settings.subjects = mapOf("MON_1" to "情報処理 / 数学")
        settings.courseLinks = mapOf("MON_1" to listOf(CourseLinks("情報処理"), CourseLinks("数学")))
        val now = date(26, 8)
        for (subject in listOf("情報処理", "数学")) {
            val note = Note("note", "課題", "本文", now, subject)
            assertTrue(TimetableWidgetState.read(timetable, now, listOf(note)).hasNextLessonNote)
        }
        val unrelated = Note("note", "課題", "本文", now, "英語")
        assertFalse(TimetableWidgetState.read(timetable, now, listOf(unrelated)).hasNextLessonNote)
    }

    @Test fun widgetRefreshesAtRelevantDeadlinesUntilTheLastActiveNoteExpires() {
        settings.selected = setOf("MON_1")
        settings.subjects = mapOf("MON_1" to "情報処理")
        settings.times["time_start_1"] = "11:00"
        settings.times["time_end_1"] = "11:50"
        val now = date(26, 10)
        val firstDeadline = date(26, 10, 10)
        val lastDeadline = date(26, 10, 20)
        val notes = listOf(
            Note("first", "課題", "本文", now, "情報処理", firstDeadline),
            Note("last", "持ち物", "本文", now, "情報処理", lastDeadline),
            Note("other", "課題", "本文", now, "数学", date(26, 10, 1))
        )
        val before = TimetableWidgetState.read(timetable, now, notes)
        assertTrue(before.hasNextLessonNote)
        assertEquals(firstDeadline, before.refreshAt)

        val between = TimetableWidgetState.read(timetable, before.refreshAt, notes)
        assertTrue(between.hasNextLessonNote)
        assertEquals(lastDeadline, between.refreshAt)

        val after = TimetableWidgetState.read(timetable, between.refreshAt, notes)
        assertFalse(after.hasNextLessonNote)
        assertEquals(date(26, 11) + 1, after.refreshAt)
        assertTrue(alarms.registrations.isEmpty())
    }
}

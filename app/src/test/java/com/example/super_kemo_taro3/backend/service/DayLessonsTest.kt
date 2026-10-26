package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.CourseLinks
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class DayLessonsTest {
    private val settings = MemorySettingsRepository()
    private val alarms = RecordingAlarmScheduler()
    private val timetable = TimetableService(settings, alarms)

    @Test fun returnsEveryLessonOfTheChosenDayInPeriodOrder() {
        settings.selected = (8 downTo 1).map { "MON_$it" }.toSet() +
            setOf("TUE_2", "FRI_1", "MON_9", "invalid")
        settings.subjects = mapOf("MON_1" to "情報処理", "MON_8" to "実験", "TUE_2" to "英語")

        val lessons = timetable.readLessonsForDay(Calendar.MONDAY)

        assertEquals((1..8).map { "MON_$it" }, lessons.map { it.slot.key })
        assertEquals("情報処理", lessons.first().subject)
        assertEquals("実験", lessons.last().subject)
        assertEquals(LessonTime(8, 40), lessons.first().startTime)
        assertEquals(LessonTime(9, 30), lessons.first().endTime)
        assertEquals(LessonTime(13, 25), lessons[4].startTime)
    }

    @Test fun usesSavedTimesAndKeepsLessonsWithNoSubjectWithoutSideEffects() {
        settings.selected = setOf("FRI_3")
        settings.times["time_start_3"] = "11:00"
        settings.times["time_end_3"] = "11:50"
        alarms.allowed = false

        val lesson = timetable.readLessonsForDay(Calendar.FRIDAY).single()

        assertEquals(LessonTime(11, 0), lesson.startTime)
        assertEquals(LessonTime(11, 50), lesson.endTime)
        assertNull(lesson.subject)
        assertEquals(setOf("FRI_3"), settings.selected)
        assertEquals(mapOf("time_start_3" to "11:00", "time_end_3" to "11:50"), settings.times)
        assertTrue(alarms.registrations.isEmpty())
        assertTrue(alarms.cancellations.isEmpty())
    }

    @Test fun daysWithNoRegisteredLessonsReturnAnEmptyList() {
        assertTrue(timetable.readLessonsForDay(Calendar.MONDAY).isEmpty())
        settings.selected = setOf("MON_1", "SUN_1", "WED_9")
        assertTrue(timetable.readLessonsForDay(Calendar.WEDNESDAY).isEmpty())
        assertTrue(timetable.readLessonsForDay(Calendar.SUNDAY).isEmpty())
    }

    @Test fun replacementKeepsLinksForTheirOwnSlotsAndClearsRemovedCourses() {
        val info = CourseLinks("情報処理", CourseLinkRules.moodleUrl(101), classroom = "A21教室")
        val experiment = CourseLinks("実験", CourseLinkRules.moodleUrl(409), classroom = "実験室")
        settings.courseLinks = mapOf("FRI_1" to listOf(experiment))
        alarms.allowed = false

        timetable.replaceSelectedSlots(
            setOf("MON_1", "TUE_2"),
            courseLinks = mapOf("MON_1" to listOf(info, info), "TUE_2" to listOf(experiment), "BAD" to listOf(info))
        )

        assertEquals(listOf(info), timetable.readLessonsForDay(Calendar.MONDAY).single().courseLinks)
        assertEquals(listOf(experiment), timetable.readLessonsForDay(Calendar.TUESDAY).single().courseLinks)
        assertFalse(settings.courseLinks.containsKey("FRI_1"))
        assertFalse(settings.courseLinks.containsKey("BAD"))
        timetable.replaceSelectedSlots(emptySet())
        assertTrue(settings.courseLinks.isEmpty())
    }
}

package com.example.super_kemo_taro3.frontend.viewmodel

import com.example.super_kemo_taro3.backend.service.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class HomeViewModelTest {
    private val settings = MemorySettingsRepository().apply { migrated = true }
    private val alarms = RecordingAlarmScheduler()
    private val notifications = NotificationModeService(RecordingNotificationGateway())
    private val model = HomeViewModel(
        TimetableService(settings, alarms), PauseService(settings, notifications), notifications
    )

    @Test fun tappingAnEmptySlotShowsEveryRegisteredLessonOfThatWeekday() {
        settings.selected = setOf("MON_3", "MON_1", "TUE_2")
        model.refresh()
        assertNull(model.uiState.selectedDay)

        model.selectDay("MON_8")

        assertEquals(Calendar.MONDAY, model.uiState.selectedDay)
        assertEquals(listOf("MON_1", "MON_3"), model.uiState.dayLessons.map { it.slot.key })
        assertEquals(setOf("MON_3", "MON_1", "TUE_2"), settings.selected)
        assertTrue(alarms.registrations.isEmpty())
        assertTrue(alarms.cancellations.isEmpty())
    }

    @Test fun changingWeekdaysReplacesTheListAndInvalidSlotsKeepTheCurrentDay() {
        settings.selected = setOf("MON_1", "FRI_8")
        model.refresh()
        model.selectDay("MON_1")
        model.selectDay("FRI_2")
        assertEquals(listOf("FRI_8"), model.uiState.dayLessons.map { it.slot.key })

        model.selectDay("WED_1")
        assertEquals(Calendar.WEDNESDAY, model.uiState.selectedDay)
        assertTrue(model.uiState.dayLessons.isEmpty())
        val before = model.uiState
        model.selectDay("invalid")
        model.selectDay("MON_9")
        assertEquals(before, model.uiState)
    }

    @Test fun refreshKeepsTheChosenDayAndReloadsLessonsTimesAndSubjects() {
        settings.selected = setOf("MON_1")
        model.refresh()
        model.selectDay("MON_8")
        settings.selected = setOf("MON_3", "TUE_2")
        settings.subjects = mapOf("MON_3" to "数学")
        settings.times["time_start_3"] = "11:00"
        settings.times["time_end_3"] = "11:50"

        model.refresh()

        assertEquals(Calendar.MONDAY, model.uiState.selectedDay)
        val lesson = model.uiState.dayLessons.single()
        assertEquals("MON_3", lesson.slot.key)
        assertEquals("数学", lesson.subject)
        assertEquals(LessonTime(11, 0), lesson.startTime)
        assertEquals(LessonTime(11, 50), lesson.endTime)
    }
}

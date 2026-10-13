package com.example.super_kemo_taro3.frontend.viewmodel

import com.example.super_kemo_taro3.backend.service.*
import org.junit.Assert.*
import org.junit.Test

/** 編集と初期化の未保存状態、エラー表示、保存、権限不足、復元、破棄の6ケースを検証する。 */
class LessonTimeEditorViewModelTest {
    private val settings = MemorySettingsRepository()
    private val alarms = RecordingAlarmScheduler()
    private val service = LessonTimeService(settings, TimetableService(settings, alarms))

    // 画面上の編集・初期化と、端末へ保存する操作を区別して確認する。
    @Test fun editingAndResetDoNotPersistUntilSave() {
        val model = LessonTimeEditorViewModel(service)
        model.openTime(0)
        model.confirmTime(8, 45)
        assertEquals("08:45", model.uiState.times[0])
        assertTrue(settings.times.isEmpty())
        model.requestConfirmation(TimeEditorConfirmation.RESET)
        assertEquals(TimeEditorEffect.NONE, model.confirmAction())
        assertEquals("08:40", model.uiState.times[0])
        assertTrue(settings.times.isEmpty())
    }

    @Test fun invalidInputOpensErrorInsteadOfSave() {
        val model = LessonTimeEditorViewModel(service)
        model.openTime(0)
        model.confirmTime(10, 0)
        model.requestConfirmation(TimeEditorConfirmation.SAVE)
        assertEquals(TimeEditorConfirmation.ERROR, model.uiState.confirmation)
        assertEquals(TimeEditorEffect.NONE, model.confirmAction())
        assertTrue(settings.times.isEmpty())
    }

    @Test fun savePersistsAndRequestsReturnHome() {
        val model = LessonTimeEditorViewModel(service)
        model.requestConfirmation(TimeEditorConfirmation.SAVE)
        assertEquals(TimeEditorEffect.RETURN_HOME, model.confirmAction())
        assertEquals(16, settings.times.size)
    }

    @Test fun saveWithoutPermissionRequestsSettingsAndReturnHome() {
        alarms.allowed = false
        val model = LessonTimeEditorViewModel(service)
        model.requestConfirmation(TimeEditorConfirmation.SAVE)
        assertEquals(TimeEditorEffect.RETURN_HOME_NEEDS_ALARM_PERMISSION, model.confirmAction())
        assertEquals(16, settings.times.size)
    }

    // 再作成前の未保存入力と開いていた確認画面を優先して戻す。
    @Test fun restoredEditsAndOpenConfirmationSurviveRecreation() {
        val restored = service.defaultTimes().toMutableList().also { it[0] = "08:45" }
        val model = LessonTimeEditorViewModel(service, restored, 0, "RESET")
        assertEquals("08:45", model.uiState.times[0])
        assertEquals(0, model.uiState.selectedIndex)
        assertEquals(8, model.uiState.initialHour)
        assertEquals(45, model.uiState.initialMinute)
        assertEquals(TimeEditorConfirmation.RESET, model.uiState.confirmation)
        assertTrue(settings.times.isEmpty())
    }

    @Test fun discardReturnsHomeWithoutPersisting() {
        val model = LessonTimeEditorViewModel(service)
        model.openTime(0)
        model.confirmTime(8, 45)
        model.requestConfirmation(TimeEditorConfirmation.DISCARD)
        assertEquals(TimeEditorEffect.RETURN_HOME, model.confirmAction())
        assertTrue(settings.times.isEmpty())
    }
}


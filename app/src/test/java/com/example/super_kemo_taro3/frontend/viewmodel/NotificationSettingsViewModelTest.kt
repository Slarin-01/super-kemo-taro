package com.example.super_kemo_taro3.frontend.viewmodel

import com.example.super_kemo_taro3.backend.repository.LessonEndNotificationMode
import com.example.super_kemo_taro3.backend.service.MemorySettingsRepository
import com.example.super_kemo_taro3.backend.service.NotificationModeService
import com.example.super_kemo_taro3.backend.service.RecordingNotificationGateway
import org.junit.Assert.*
import org.junit.Test

class NotificationSettingsViewModelTest {
    private val gateway = RecordingNotificationGateway()
    private val notifications = NotificationModeService(gateway)

    @Test fun savedSelectionIsRestoredWhenTheScreenIsReopened() {
        val settings = MemorySettingsRepository()
        val model = NotificationSettingsViewModel(settings, notifications)
        assertEquals(LessonEndNotificationMode.KEEP_MUTED, model.selectedMode)

        model.selectMode(LessonEndNotificationMode.UNMUTE_ALL)
        assertEquals(LessonEndNotificationMode.UNMUTE_ALL, model.selectedMode)
        assertEquals(LessonEndNotificationMode.UNMUTE_ALL, NotificationSettingsViewModel(settings, notifications).selectedMode)

        model.selectMode(LessonEndNotificationMode.KEEP_MUTED)
        assertEquals(LessonEndNotificationMode.KEEP_MUTED, NotificationSettingsViewModel(settings, notifications).selectedMode)
    }

    @Test fun disablingSavesTheSelectionAndReleasesAppDndWithoutChangingSoundSettings() {
        val settings = MemorySettingsRepository()
        val model = NotificationSettingsViewModel(settings, notifications)
        assertTrue(model.automaticMutingEnabled)
        gateway.currentMuted = true

        model.setAutomaticMuting(false)

        assertFalse(model.automaticMutingEnabled)
        assertFalse(NotificationSettingsViewModel(settings, notifications).automaticMutingEnabled)
        assertFalse(gateway.currentMuted)
        assertEquals(listOf(false), gateway.changes)
        assertTrue(gateway.soundChanges.isEmpty())
    }

    @Test fun reenablingWaitsForTheNextAlarmAndPreservesTheEndMode() {
        val settings = MemorySettingsRepository().apply {
            automaticMutingEnabled = false
            lessonEndMode = LessonEndNotificationMode.UNMUTE_ALL
        }
        val model = NotificationSettingsViewModel(settings, notifications)

        model.setAutomaticMuting(true)

        assertTrue(NotificationSettingsViewModel(settings, notifications).automaticMutingEnabled)
        assertEquals(LessonEndNotificationMode.UNMUTE_ALL, model.selectedMode)
        assertTrue(gateway.operations.isEmpty())
    }

    @Test fun disablingWithoutNotificationPermissionStillSavesTheSetting() {
        val settings = MemorySettingsRepository()
        gateway.allowed = false
        gateway.currentMuted = true
        val model = NotificationSettingsViewModel(settings, notifications)

        model.setAutomaticMuting(false)

        assertFalse(settings.readAutomaticMutingEnabled())
        assertTrue(gateway.operations.isEmpty())
    }

    @Test fun disablingWhenAppDndIsOffDoesNotChangeNotificationSettings() {
        val model = NotificationSettingsViewModel(MemorySettingsRepository(), notifications)

        model.setAutomaticMuting(false)

        assertTrue(gateway.operations.isEmpty())
    }
}

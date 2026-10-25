package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.LessonEndNotificationMode
import org.junit.Assert.*
import org.junit.Test

class LessonEndNotificationTest {
    private val settings = MemorySettingsRepository()
    private val gateway = RecordingNotificationGateway()
    private val notifications = NotificationModeService(gateway)
    private val alarms = RecordingAlarmScheduler()
    private val pause = PauseService(settings, notifications) { 1_000L }
    private val delivery = AlarmDeliveryService(notifications, pause, alarms, settings)

    @Test fun defaultEndModeSilencesSoundsAndThenReleasesDnd() {
        gateway.currentMuted = true
        delivery.handle(AlarmContract.ACTION_UNMUTE, 7, 2_000L)

        assertFalse(gateway.currentMuted)
        assertEquals(listOf("sounds:true", "dnd:false"), gateway.operations)
        assertEquals(RecordedRepeat(AlarmContract.ACTION_UNMUTE, 7, 2_000L), alarms.repeats.single())
    }

    @Test fun endUsesLatestSavedModeWithoutReschedulingTheExistingAlarm() {
        settings.writeLessonEndNotificationMode(LessonEndNotificationMode.UNMUTE_ALL)
        delivery.handle(AlarmContract.ACTION_UNMUTE, 7, 2_000L)
        settings.writeLessonEndNotificationMode(LessonEndNotificationMode.KEEP_MUTED)
        delivery.handle(AlarmContract.ACTION_UNMUTE, 7, 3_000L)

        assertEquals(listOf(false, true), gateway.soundChanges)
        assertEquals(listOf(false, false), gateway.changes)
        assertEquals(2, alarms.repeats.size)
    }

    @Test fun lessonStartDoesNotApplyTheEndMode() {
        settings.writeLessonEndNotificationMode(LessonEndNotificationMode.UNMUTE_ALL)
        delivery.handle(AlarmContract.ACTION_MUTE, 7, 2_000L)

        assertEquals(listOf(true), gateway.changes)
        assertTrue(gateway.soundChanges.isEmpty())
    }

    @Test fun pausedLessonEndLeavesBothSoundAndDndAloneButRepeats() {
        settings.pauseUntil = 3_000L
        delivery.handle(AlarmContract.ACTION_UNMUTE, 7, 2_000L)

        assertTrue(gateway.operations.isEmpty())
        assertEquals(1, alarms.repeats.size)
    }

    @Test fun missingPolicyAccessChangesNeitherSoundNorDnd() {
        gateway.allowed = false
        assertFalse(notifications.finishLesson(LessonEndNotificationMode.UNMUTE_ALL))
        delivery.handle(AlarmContract.ACTION_UNMUTE, 7, 2_000L)

        assertTrue(gateway.operations.isEmpty())
        assertTrue(alarms.repeats.isEmpty())
    }

    @Test fun missingAlarmAccessStillAppliesTheEndMode() {
        alarms.allowed = false
        settings.writeLessonEndNotificationMode(LessonEndNotificationMode.UNMUTE_ALL)
        delivery.handle(AlarmContract.ACTION_UNMUTE, 7, 2_000L)

        assertEquals(listOf("sounds:false", "dnd:false"), gateway.operations)
        assertTrue(alarms.repeats.isEmpty())
    }

    @Test fun disabledAutomationSkipsStartAndEndForBothEndModesButKeepsWeeklyAlarms() {
        settings.writeAutomaticMutingEnabled(false)
        for (mode in LessonEndNotificationMode.entries) {
            settings.writeLessonEndNotificationMode(mode)
            delivery.handle(AlarmContract.ACTION_MUTE, 7, 2_000L)
            delivery.handle(AlarmContract.ACTION_UNMUTE, 8, 3_000L)
        }

        assertTrue(gateway.operations.isEmpty())
        assertEquals(4, alarms.repeats.size)
    }

    @Test fun reenablingAutomationAppliesToTheAlreadyScheduledAlarms() {
        settings.writeAutomaticMutingEnabled(false)
        delivery.handle(AlarmContract.ACTION_MUTE, 7, 2_000L)
        assertTrue(gateway.operations.isEmpty())

        settings.writeAutomaticMutingEnabled(true)
        delivery.handle(AlarmContract.ACTION_MUTE, 7, 3_000L)
        delivery.handle(AlarmContract.ACTION_UNMUTE, 8, 4_000L)

        assertEquals(listOf("dnd:true", "sounds:true", "dnd:false"), gateway.operations)
        assertEquals(3, alarms.repeats.size)
    }
}

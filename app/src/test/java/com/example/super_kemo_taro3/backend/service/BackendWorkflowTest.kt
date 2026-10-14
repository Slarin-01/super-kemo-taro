package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.service.LessonTime
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/** 選択・取消、権限不足でも保存、保存後の再予約、不正入力、旧受信先の一度だけの移行、再起動、一時停止・権限による受信処理を14件で検証する。 */
class BackendWorkflowTest {
    private val settings = MemorySettingsRepository()
    private val alarms = RecordingAlarmScheduler()
    private val gateway = RecordingNotificationGateway()
    private val notifications = NotificationModeService(gateway)
    private val timetable = TimetableService(settings, alarms)
    private val lessonTimes = LessonTimeService(settings, timetable)
    // 時計を固定し、実行時刻に左右されず停止中・期限切れを検証する。
    private var now = 1000L
    private val pause = PauseService(settings, notifications) { now }
    private val delivery = AlarmDeliveryService(notifications, pause, alarms)

    // 選択・解除と、時刻保存前後の予約内容を確認する。
    @Test fun selectionPersistsAndRegistersFridayAlarm() {
        assertFalse(timetable.toggleSlot("FRI_5"))
        assertEquals(setOf("FRI_5"), settings.selected)
        val registered = alarms.registrations.single()
        assertEquals(Calendar.FRIDAY, registered.slot.calendarDay)
        assertEquals(LessonTime(13, 25), registered.start)
        assertEquals(LessonTime(14, 15), registered.end)
    }

    @Test fun deselectionCancelsAndRemovesTheSlot() {
        settings.selected = setOf("MON_1")
        assertFalse(timetable.toggleSlot("MON_1"))
        assertTrue(settings.selected.isEmpty())
        assertEquals(listOf("MON_1"), alarms.cancellations)
    }

    @Test fun selectionWithoutPermissionStillPersists() {
        alarms.allowed = false
        assertTrue(timetable.toggleSlot("FRI_1"))
        assertEquals(setOf("FRI_1"), settings.selected)
        assertTrue(alarms.registrations.isEmpty())
    }

    @Test fun savedEditorTimesRescheduleTheSameFridaySlot() {
        settings.selected = setOf("FRI_5")
        assertEquals(SaveTimesResult.SAVED, lessonTimes.save(lessonTimes.defaultTimes()))
        val registered = alarms.registrations.single()
        assertEquals(Calendar.FRIDAY, registered.slot.calendarDay)
        assertEquals(LessonTime(13, 15), registered.start)
        assertEquals("13:15", settings.times["time_start_5"])
        assertEquals(16, settings.times.size)
    }

    @Test fun invalidOrIncompleteTimesAreNotPersisted() {
        val invalid = lessonTimes.defaultTimes().toMutableList().also { it[0] = "25:00" }
        assertEquals(SaveTimesResult.INVALID, lessonTimes.save(invalid))
        assertEquals(SaveTimesResult.INVALID, lessonTimes.save(emptyList()))
        assertTrue(settings.times.isEmpty())
        assertTrue(alarms.registrations.isEmpty())
    }

    @Test fun savingWithoutAlarmPermissionKeepsTimes() {
        alarms.allowed = false
        assertEquals(SaveTimesResult.SAVED_NEEDS_ALARM_PERMISSION, lessonTimes.save(lessonTimes.defaultTimes()))
        assertEquals(16, settings.times.size)
        assertTrue(alarms.registrations.isEmpty())
    }

    // 旧予約の移行は一度だけ行い、権限がない間は移行完了にしない。
    @Test fun receiverMigrationCancelsOldAlarmsAndRunsOnce() {
        settings.selected = setOf("FRI_1")
        assertTrue(timetable.migrateReceiverIfNeeded())
        assertFalse(timetable.migrateReceiverIfNeeded())
        assertEquals(listOf("FRI_1"), alarms.legacyCancellations)
        assertEquals(1, alarms.registrations.size)
        assertEquals(Calendar.FRIDAY, alarms.registrations.single().slot.calendarDay)
        assertTrue(settings.migrated)
    }

    @Test fun migrationWaitsUntilAlarmPermissionIsGranted() {
        settings.selected = setOf("FRI_1")
        alarms.allowed = false
        assertFalse(timetable.migrateReceiverIfNeeded())
        assertFalse(settings.migrated)
        alarms.allowed = true
        assertTrue(timetable.migrateReceiverIfNeeded())
        assertEquals(1, alarms.registrations.size)
    }

    @Test fun bootRestoreUsesTheSameFridayMapping() {
        settings.selected = setOf("FRI_8", "invalid")
        assertTrue(timetable.restoreAfterBoot())
        assertEquals(Calendar.FRIDAY, alarms.registrations.single().slot.calendarDay)
        assertEquals(LessonTime(16, 30), alarms.registrations.single().start)
    }

    // 停止期限と2種類の権限を分け、通知変更と翌週予約の違いを確認する。
    @Test fun pauseSuppressesModeChangeButKeepsWeeklyRegistration() {
        settings.pauseUntil = 2000L
        delivery.handle(AlarmContract.ACTION_MUTE, 55, 1234L)
        assertTrue(gateway.changes.isEmpty())
        assertEquals(RecordedRepeat(AlarmContract.ACTION_MUTE, 55, 1234L), alarms.repeats.single())
    }

    @Test fun expiredPauseAllowsModeChangeAndRepeat() {
        settings.pauseUntil = now
        delivery.handle(AlarmContract.ACTION_MUTE, 55, 1234L)
        assertEquals(listOf(true), gateway.changes)
        assertEquals(1, alarms.repeats.size)
    }

    @Test fun missingNotificationPermissionStopsModeChangeAndRepeat() {
        gateway.allowed = false
        delivery.handle(AlarmContract.ACTION_MUTE, 55, 1234L)
        assertTrue(gateway.changes.isEmpty())
        assertTrue(alarms.repeats.isEmpty())
    }

    @Test fun missingAlarmPermissionStillAllowsCurrentModeChange() {
        alarms.allowed = false
        delivery.handle(AlarmContract.ACTION_UNMUTE, 55, 1234L)
        assertEquals(listOf(false), gateway.changes)
        assertTrue(alarms.repeats.isEmpty())
    }

    @Test fun enablingPauseUnmutesAndDisablingPauseClearsDeadline() {
        gateway.currentMuted = true
        pause.toggle()
        assertTrue(pause.isPaused())
        assertFalse(gateway.currentMuted)
        pause.toggle()
        assertFalse(pause.isPaused())
        assertEquals(0L, settings.pauseUntil)
    }
}


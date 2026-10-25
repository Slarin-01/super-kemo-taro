package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.service.LessonTime
import com.example.super_kemo_taro3.backend.repository.MoodleLogin
import com.example.super_kemo_taro3.backend.repository.CourseLinks
import com.example.super_kemo_taro3.backend.repository.SettingsRepository
import com.example.super_kemo_taro3.backend.repository.LessonEndNotificationMode

/** MemorySettingsRepository、RecordingAlarmScheduler、RecordingNotificationGatewayが設定保存と端末操作の依頼をメモリー上で記録する。 */
class MemorySettingsRepository : SettingsRepository {
    // 本番と同じ読み書きの形で、設定値だけをメモリーに保持する。
    var selected: Set<String> = emptySet()
    var subjects: Map<String, String> = emptyMap()
    var courseLinks: Map<String, List<CourseLinks>> = emptyMap()
    var pauseUntil = 0L
    var automaticMutingEnabled = true
    var lessonEndMode = LessonEndNotificationMode.KEEP_MUTED
    val times = mutableMapOf<String, String>()
    var darkMode: Boolean? = null
    var themeColor: Int? = null
    var migrated = false
    var moodleToken: String? = null
    var moodleLogin: MoodleLogin? = null
    override fun readBlockedSlots() = selected.toSet()
    override fun writeBlockedSlots(slots: Set<String>) { selected = slots.toSet() }
    override fun readSlotSubjects() = subjects.toMap()
    override fun writeSlotSubjects(subjects: Map<String, String>) { this.subjects = subjects.toMap() }
    override fun readSlotCourseLinks() = courseLinks.mapValues { it.value.toList() }
    override fun writeSlotCourseLinks(links: Map<String, List<CourseLinks>>) {
        courseLinks = links.mapValues { it.value.toList() }
    }
    override fun readMoodleToken() = moodleToken
    override fun writeMoodleToken(token: String?) { moodleToken = token }
    override fun readMoodleLogin() = moodleLogin
    override fun writeMoodleLogin(login: MoodleLogin?) { moodleLogin = login }
    override fun readPauseUntil() = pauseUntil
    override fun writePauseUntil(deadline: Long?) { pauseUntil = deadline ?: 0L }
    override fun readAutomaticMutingEnabled() = automaticMutingEnabled
    override fun writeAutomaticMutingEnabled(enabled: Boolean) { automaticMutingEnabled = enabled }
    override fun readLessonEndNotificationMode() = lessonEndMode
    override fun writeLessonEndNotificationMode(mode: LessonEndNotificationMode) { lessonEndMode = mode }
    override fun readTime(key: String) = times[key]
    override fun writeTimes(times: Map<String, String>) { this.times.putAll(times) }
    override fun readDarkMode() = darkMode
    override fun writeDarkMode(isDark: Boolean?) { darkMode = isDark }
    override fun readThemeColor() = themeColor
    override fun writeThemeColor(color: Int) { themeColor = color }
    override fun isAlarmReceiverMigrated() = migrated
    override fun markAlarmReceiverMigrated() { migrated = true }
}

data class RecordedSlot(val slot: TimetableSlot, val start: LessonTime, val end: LessonTime)
data class RecordedRepeat(val action: String, val requestCode: Int, val scheduledTime: Long)

/** 実際の予約を作らず登録・取消・翌週予約を記録する。allowedで権限不足を再現する。 */
class RecordingAlarmScheduler : AlarmScheduler {
    var allowed = true
    val registrations = mutableListOf<RecordedSlot>()
    val cancellations = mutableListOf<String>()
    val legacyCancellations = mutableListOf<String>()
    val repeats = mutableListOf<RecordedRepeat>()
    override fun canScheduleExactAlarms() = allowed
    override fun canRestoreAfterBoot() = allowed
    override fun scheduleSlot(slot: TimetableSlot, start: LessonTime, end: LessonTime) {
        registrations += RecordedSlot(slot, start, end)
    }
    override fun cancelSlot(slotKey: String) { cancellations += slotKey }
    override fun cancelLegacySlot(slotKey: String) { legacyCancellations += slotKey }
    override fun scheduleNext(action: String, requestCode: Int, scheduledTime: Long) {
        repeats += RecordedRepeat(action, requestCode, scheduledTime)
    }
}

/** 実端末の通知状態を変えず、変更依頼と模擬の現在状態を記録する。 */
class RecordingNotificationGateway : NotificationGateway {
    var allowed = true
    var currentMuted = false
    val changes = mutableListOf<Boolean>()
    val soundChanges = mutableListOf<Boolean>()
    val operations = mutableListOf<String>()
    override fun hasPolicyAccess() = allowed
    override fun isMuted() = currentMuted
    override fun setMuted(muted: Boolean) {
        this.currentMuted = muted
        changes += muted
        operations += "dnd:$muted"
    }
    override fun setNotificationSoundsMuted(muted: Boolean) {
        soundChanges += muted
        operations += "sounds:$muted"
    }
}


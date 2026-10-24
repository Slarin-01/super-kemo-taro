/*
 * バックエンド・処理: 曜日・時限の解釈、次回日時・停止期限の計算、選択枠とアラームの連携。
 * 同じ機能の定義・判断・端末操作をまとめる。画面描画やActivityへの遷移を含めない。
 */
package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.SettingsRepository
import com.example.super_kemo_taro3.backend.repository.CourseLinks
import java.util.Calendar

// 1. 表示・保存キーと内部曜日の対応
/** 選択枠の保存キー、実際に動かす曜日、1〜8限の番号をまとめる。 */
data class TimetableSlot(val key: String, val calendarDay: Int, val lesson: Int)

/** ホームに表示する次の授業。開始日時は端末のタイムゾーンで計算する。 */
data class NextLesson(
    val slot: TimetableSlot,
    val startsAt: Long,
    val endTime: LessonTime,
    val subject: String? = null
)

/** 曜日ごとの一覧に表示する授業。開始済みの授業も含めて扱う。 */
data class TimetableLesson(
    val slot: TimetableSlot,
    val startTime: LessonTime,
    val endTime: LessonTime,
    val subject: String? = null,
    val courseLinks: List<CourseLinks> = emptyList()
)

/** MON_1などの保存キーを解釈する。表示曜日と動作曜日の対応はここだけで管理する。 */
object TimetableRules {
    private val calendarDays = mapOf(
        "MON" to Calendar.MONDAY,
        "TUE" to Calendar.TUESDAY,
        "WED" to Calendar.WEDNESDAY,
        "THU" to Calendar.THURSDAY,
        // 表示・保存キーと同じ金曜日に動かす。すべての登録経路でこの対応を使う。
        "FRI" to Calendar.FRIDAY
    )

    fun parseSlot(key: String): TimetableSlot? {
        val parts = key.split("_")
        if (parts.size != 2) return null
        val day = calendarDays[parts[0]] ?: return null
        val lesson = parts[1].toIntOrNull()?.takeIf { it in 1..8 } ?: return null
        return TimetableSlot(key, day, lesson)
    }
}

// 2. 次回予約日時と停止期限の計算
/** Calendarで指定曜日・時刻の次回予定と、直近の日曜日末の停止期限を計算する。日時を引数で受け取り、端末APIなしでテストできる。 */
object ScheduleCalculator {
    const val WEEK_IN_MILLIS = 7L * 24 * 60 * 60 * 1000

    // 端末のタイムゾーンで候補を作り、すでに過ぎた予定だけを翌週へ送る。
    fun nextOccurrence(day: Int, time: LessonTime, now: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_WEEK, day)
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis < now) add(Calendar.WEEK_OF_YEAR, 1)
        }.timeInMillis

    // 日曜日当日は当日末、それ以外は次の日曜日末を停止期限にする。
    fun sundayEnd(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        val day = get(Calendar.DAY_OF_WEEK)
        val days = if (day == Calendar.SUNDAY) 0 else Calendar.SATURDAY - day + 1
        add(Calendar.DAY_OF_YEAR, days)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}

// 3. 選択枠、設定保存、アラーム予約の連携
/** 設定とアラーム操作を組み合わせる中心処理。選択時に1枠を登録、解除時に取消、時刻保存時に全選択枠を再登録する。 */
class TimetableService(
    private val settings: SettingsRepository,
    private val alarms: AlarmScheduler
) {
    fun readSelectedSlots(): Set<String> = settings.readBlockedSlots()

    /** 指定曜日の登録済み授業を時限順で返す。表示する時刻はアラームと共通の読み取り方を使う。 */
    fun readLessonsForDay(day: Int): List<TimetableLesson> {
        val subjects = settings.readSlotSubjects()
        val links = settings.readSlotCourseLinks()
        return settings.readBlockedSlots().mapNotNull { key ->
            val slot = TimetableRules.parseSlot(key)?.takeIf { it.calendarDay == day }
                ?: return@mapNotNull null
            val (start, end) = readSlotTimes(slot, useDefaults = true) ?: return@mapNotNull null
            TimetableLesson(slot, start, end, subjects[key], links[key].orEmpty())
        }.sortedBy { it.slot.lesson }
    }

    /** 開始前の選択枠から最も近い授業を返す。未保存時刻はアラームと同じ既定値を使う。 */
    fun readNextLesson(now: Long = System.currentTimeMillis()): NextLesson? =
        settings.readBlockedSlots().mapNotNull { key ->
            val slot = TimetableRules.parseSlot(key) ?: return@mapNotNull null
            val (start, end) = readSlotTimes(slot, useDefaults = true) ?: return@mapNotNull null
            NextLesson(slot, ScheduleCalculator.nextOccurrence(slot.calendarDay, start, now), end)
        }.minByOrNull { it.startsAt }?.let { next ->
            next.copy(subject = settings.readSlotSubjects()[next.slot.key])
        }

    fun canScheduleAlarms(): Boolean = alarms.canScheduleExactAlarms()

    /** 選択を反転する。trueは新規予約に必要な許可が不足し、画面で設定案内が必要なことを表す。 */
    fun toggleSlot(key: String): Boolean {
        val selected = settings.readBlockedSlots()
        val needsPermission: Boolean
        if (key in selected) {
            alarms.cancelSlot(key)
            settings.writeBlockedSlots(selected - key)
            needsPermission = false
        } else {
            needsPermission = !alarms.canScheduleExactAlarms()
            if (!needsPermission) registerSlot(key, useDefaults = true)
            // アラーム権限がなくても、選択した枠は従来どおり保存する。
            settings.writeBlockedSlots(selected + key)
        }
        return needsPermission
    }

    /** 選択枠を置き換える。外した枠の予約は消し、権限があれば新しい枠を予約する。trueは予約の許可不足。 */
    fun replaceSelectedSlots(
        keys: Set<String>,
        subjects: Map<String, String> = emptyMap(),
        courseLinks: Map<String, List<CourseLinks>> = emptyMap()
    ): Boolean {
        val next = keys.filter { TimetableRules.parseSlot(it) != null }.toSet()
        val previous = settings.readBlockedSlots()
        (previous - next).forEach { alarms.cancelSlot(it) }
        val needsPermission = !alarms.canScheduleExactAlarms()
        if (!needsPermission) next.forEach { registerSlot(it, useDefaults = true) }
        settings.writeBlockedSlots(next)
        settings.writeSlotSubjects(subjects.filterKeys { it in next }
            .mapValues { it.value.trim() }.filterValues { it.isNotEmpty() })
        settings.writeSlotCourseLinks(courseLinks.filterKeys { it in next }
            .mapValues { it.value.distinct() }.filterValues { it.isNotEmpty() })
        return needsPermission
    }

    // 時刻保存後は保存値だけで再登録する。falseは予約権限不足であり、時刻保存の失敗ではない。
    fun rescheduleSelectedSlots(): Boolean {
        if (!alarms.canScheduleExactAlarms()) return false
        settings.readBlockedSlots().forEach { registerSlot(it, useDefaults = false) }
        return true
    }

    // 再起動とアプリ更新の両方で、保存済みの枠から新しい受信先へ復元する。
    fun restoreAfterBoot(): Boolean {
        if (!alarms.canRestoreAfterBoot()) return false
        settings.readBlockedSlots().forEach { key ->
            alarms.cancelLegacySlot(key)
            registerSlot(key, useDefaults = true)
        }
        settings.markAlarmReceiverMigrated()
        return true
    }

    // Receiverのクラス名変更で予約が途切れないよう、移行を一度だけ行う。
    // 権限がない間は完了にせず、権限設定から戻った後に再試行できるようにする。
    fun migrateReceiverIfNeeded(): Boolean {
        if (settings.isAlarmReceiverMigrated() || !alarms.canScheduleExactAlarms()) return false
        settings.readBlockedSlots().forEach { key ->
            alarms.cancelLegacySlot(key)
            registerSlot(key, useDefaults = true)
        }
        settings.markAlarmReceiverMigrated()
        return true
    }

    private fun registerSlot(key: String, useDefaults: Boolean) {
        // 不正な枠は予約しない。選択・復元時は未保存や読み取れない時刻部分を既定値で補い、保存後は保存値だけを使う。
        val slot = TimetableRules.parseSlot(key) ?: return
        val (start, end) = readSlotTimes(slot, useDefaults) ?: return
        alarms.scheduleSlot(slot, start, end)
    }

    private fun readSlotTimes(slot: TimetableSlot, useDefaults: Boolean): Pair<LessonTime, LessonTime>? {
        val index = (slot.lesson - 1) * 2
        val startField = LessonTimeDefaults.alarmFields[index]
        val endField = LessonTimeDefaults.alarmFields[index + 1]
        val start = readAlarmTime(startField.saveKey, startField.defaultTime, useDefaults) ?: return null
        val end = readAlarmTime(endField.saveKey, endField.defaultTime, useDefaults) ?: return null
        return start to end
    }

    // 既定値を使う経路では、時刻文字列の時・分をそれぞれ補い、従来の読み取り方を引き継ぐ。
    private fun readAlarmTime(key: String, fallback: LessonTime, useDefaults: Boolean): LessonTime? {
        val saved = settings.readTime(key)
        val parts = saved?.split(":")
        if (useDefaults) {
            if (parts == null || parts.size < 2) return fallback
            return LessonTime(
                parts[0].toIntOrNull() ?: fallback.hour,
                parts[1].toIntOrNull() ?: fallback.minute
            )
        }
        if (parts == null || parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return LessonTime(hour, minute)
    }
}


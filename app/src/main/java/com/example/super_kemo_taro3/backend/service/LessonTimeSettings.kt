/*
 * バックエンド・処理: 授業時刻のデータ、既定値、入力検証、読み込み・保存と再予約の依頼。
 * 同じ機能の定義・判断・端末操作をまとめる。画面描画やActivityへの遷移を含めない。
 */
package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.SettingsRepository
import java.util.Locale

// 1. 時刻と保存欄のデータ
/** 予約に渡す時・分。保存用文字列の解析はLessonTimeRulesなどの呼び出し側で行う。 */
data class LessonTime(val hour: Int, val minute: Int)

/** 開始・終了の各入力欄を、保存キーと初期時刻に対応付ける。 */
data class LessonTimeField(val saveKey: String, val defaultTime: LessonTime)

// 2. 編集・未保存アラーム用の既定値
/** editorFieldsに1〜8限の開始・終了を交互に並べた16欄を定義する。alarmFieldsは未保存時のアラーム用の既定値。 */
object LessonTimeDefaults {
    val editorFields = listOf(
        LessonTimeField("time_start_1", LessonTime(8, 40)), LessonTimeField("time_end_1", LessonTime(9, 30)),
        LessonTimeField("time_start_2", LessonTime(9, 40)), LessonTimeField("time_end_2", LessonTime(10, 30)),
        LessonTimeField("time_start_3", LessonTime(10, 45)), LessonTimeField("time_end_3", LessonTime(11, 35)),
        LessonTimeField("time_start_4", LessonTime(11, 45)), LessonTimeField("time_end_4", LessonTime(12, 35)),
        LessonTimeField("time_start_5", LessonTime(13, 15)), LessonTimeField("time_end_5", LessonTime(14, 25)),
        LessonTimeField("time_start_6", LessonTime(14, 25)), LessonTimeField("time_end_6", LessonTime(15, 15)),
        LessonTimeField("time_start_7", LessonTime(15, 30)), LessonTimeField("time_end_7", LessonTime(16, 20)),
        LessonTimeField("time_start_8", LessonTime(16, 30)), LessonTimeField("time_end_8", LessonTime(17, 20))
    )

    // 従来の未保存時の5限と編集画面の初期値を区別し、今回の構成変更では時刻を変えない。
    val alarmFields = editorFields.map { field ->
        when (field.saveKey) {
            "time_start_5" -> field.copy(defaultTime = LessonTime(13, 25))
            "time_end_5" -> field.copy(defaultTime = LessonTime(14, 15))
            else -> field
        }
    }
}

// 3. 表示形式、時刻解析、順序・重複の検証
/** Locale.ROOTによるHH:mmへの整形、0時からの分数への解析、開始・終了の順序と直前時限との重複検証を行う。 */
object LessonTimeRules {
    fun formatTime(hour: Int, minute: Int): String = "%02d:%02d".format(Locale.ROOT, hour, minute)

    fun formatTime(time: LessonTime): String = formatTime(time.hour, time.minute)

    fun timeToMinutes(value: String): Int? {
        val parts = value.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    /** 開始・終了が交互に並ぶ時刻から、画面でエラー表示する欄の番号を返す。 */
    fun invalidTimeIndices(times: List<String>): Set<Int> {
        val errors = mutableSetOf<Int>()
        var previousEnd: Int? = null
        for (index in times.indices step 2) {
            val start = timeToMinutes(times[index])
            val end = times.getOrNull(index + 1)?.let(::timeToMinutes)
            if (start == null) errors.add(index)
            if (end == null) errors.add(index + 1)
            // 開始以前になっている終了欄、直前の終了より早い開始欄をエラーにする。時限同士の境目が同時刻なら許可する。
            if (start != null && end != null && start >= end) errors.add(index + 1)
            if (start != null && previousEnd != null && previousEnd > start) errors.add(index)
            previousEnd = end
        }
        return errors
    }
}

// 4. 設定の読み込み、保存、再予約の依頼
/** INVALIDは未保存。SAVED_NEEDS_ALARM_PERMISSIONは保存済みだが予約権限の設定が必要な状態。 */
enum class SaveTimesResult { INVALID, SAVED, SAVED_NEEDS_ALARM_PERMISSION }

/** 16欄の読み込み・保存を担当し、時刻変更後の再予約をTimetableServiceへ依頼する。 */
class LessonTimeService(
    private val settings: SettingsRepository,
    private val timetable: TimetableService
) {
    fun defaultTimes(): List<String> =
        LessonTimeDefaults.editorFields.map { LessonTimeRules.formatTime(it.defaultTime) }

    fun loadTimes(): List<String> = LessonTimeDefaults.editorFields.map {
        settings.readTime(it.saveKey) ?: LessonTimeRules.formatTime(it.defaultTime)
    }

    fun save(times: List<String>): SaveTimesResult {
        if (times.size != LessonTimeDefaults.editorFields.size ||
            LessonTimeRules.invalidTimeIndices(times).isNotEmpty()
        ) return SaveTimesResult.INVALID
        // 許可不足でも入力を失わないよう、時刻を保存してから再予約の可否を確認する。
        settings.writeTimes(LessonTimeDefaults.editorFields.mapIndexed { index, field ->
            field.saveKey to times[index]
        }.toMap())
        return if (timetable.rescheduleSelectedSlots()) SaveTimesResult.SAVED
        else SaveTimesResult.SAVED_NEEDS_ALARM_PERMISSION
    }
}


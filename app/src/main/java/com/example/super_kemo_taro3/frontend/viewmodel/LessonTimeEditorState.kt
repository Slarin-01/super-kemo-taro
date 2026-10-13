/*
 * フロントエンド・状態管理: 未保存時刻、入力エラー、選択欄、保存・初期化・破棄の確認状態。
 * 表示状態と未保存の入力を保持する。端末APIの直接操作を含めず、サービスや設定の契約へ依頼する。
 */
package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.super_kemo_taro3.backend.service.LessonTimeDefaults
import com.example.super_kemo_taro3.backend.service.LessonTimeRules
import com.example.super_kemo_taro3.backend.service.LessonTimeService
import com.example.super_kemo_taro3.backend.service.SaveTimesResult

// 1. 時刻入力と確認の状態管理
/** どの確認画面を開くかを表す。ERRORは保存できない入力を知らせる画面。 */
enum class TimeEditorConfirmation { SAVE, RESET, DISCARD, ERROR }
/** 確認後に画面側が行う操作。保存結果に応じ、許可の案内を伴ってホームへ戻る。 */
enum class TimeEditorEffect { NONE, RETURN_HOME, RETURN_HOME_NEEDS_ALARM_PERMISSION }

/** timesは開始・終了を交互に並べた16時刻。errorsも同じ添字で示し、selectedIndexの-1は入力画面を閉じた状態。 */
data class LessonTimeUiState(
    val times: List<String>,
    val errors: Set<Int>,
    val selectedIndex: Int = -1,
    val initialHour: Int = 0,
    val initialMinute: Int = 0,
    val confirmation: TimeEditorConfirmation? = null
)

/** 未保存の時刻と確認画面を管理する。保存が確定するまで、時刻設定は書き換えない。 */
class LessonTimeEditorViewModel(
    private val lessonTimes: LessonTimeService,
    restoredTimes: List<String>? = null,
    restoredSelectedIndex: Int = -1,
    restoredConfirmation: String? = null
) : ViewModel() {
    // 未保存の復元値を優先する。保存はユーザーが確認したときだけ行う。
    private val initialTimes = restoredTimes?.takeIf { it.size == LessonTimeDefaults.editorFields.size }
        ?: lessonTimes.loadTimes()

    var uiState by mutableStateOf(
        LessonTimeUiState(initialTimes, LessonTimeRules.invalidTimeIndices(initialTimes))
    )
        private set

    init {
        // 復元された選択欄を開き直す。時刻選択の初期値は、その欄の編集値から作る。
        if (restoredSelectedIndex in initialTimes.indices) openTime(restoredSelectedIndex)
        uiState = uiState.copy(
            confirmation = TimeEditorConfirmation.entries.firstOrNull { it.name == restoredConfirmation }
        )
    }

    fun openTime(index: Int) {
        if (index !in uiState.times.indices) return
        val default = LessonTimeDefaults.editorFields[index].defaultTime
        // 値を解釈できない欄でも入力画面を開けるよう、その欄の初期時刻を使う。
        val minutes = LessonTimeRules.timeToMinutes(uiState.times[index]) ?: (default.hour * 60 + default.minute)
        uiState = uiState.copy(selectedIndex = index, initialHour = minutes / 60, initialMinute = minutes % 60)
    }

    fun closeTime() {
        uiState = uiState.copy(selectedIndex = -1)
    }

    fun confirmTime(hour: Int, minute: Int) {
        val index = uiState.selectedIndex
        if (index !in uiState.times.indices) return
        // 一覧を新しく作り直して画面へ反映し、変更後の時刻順を再確認する。
        val times = uiState.times.toMutableList().also {
            it[index] = LessonTimeRules.formatTime(hour, minute)
        }
        uiState = uiState.copy(
            times = times, errors = LessonTimeRules.invalidTimeIndices(times), selectedIndex = -1
        )
    }

    fun requestConfirmation(confirmation: TimeEditorConfirmation) {
        // 矛盾がある入力のまま保存確認へ進ませず、修正を案内する。
        uiState = uiState.copy(
            confirmation = if (confirmation == TimeEditorConfirmation.SAVE && uiState.errors.isNotEmpty())
                TimeEditorConfirmation.ERROR else confirmation
        )
    }

    fun dismissConfirmation() {
        uiState = uiState.copy(confirmation = null)
    }

    fun confirmAction(): TimeEditorEffect {
        // 確認した操作を退避してから閉じ、必要な画面遷移をActivityへ返す。
        val action = uiState.confirmation
        dismissConfirmation()
        return when (action) {
            TimeEditorConfirmation.SAVE -> when (lessonTimes.save(uiState.times)) {
                SaveTimesResult.SAVED -> TimeEditorEffect.RETURN_HOME
                SaveTimesResult.SAVED_NEEDS_ALARM_PERMISSION -> TimeEditorEffect.RETURN_HOME_NEEDS_ALARM_PERMISSION
                SaveTimesResult.INVALID -> {
                    uiState = uiState.copy(confirmation = TimeEditorConfirmation.ERROR)
                    TimeEditorEffect.NONE
                }
            }
            TimeEditorConfirmation.RESET -> {
                // 初期化は編集中の一覧だけを戻す。永続保存には、別途「保存する」の確認が必要。
                val defaults = lessonTimes.defaultTimes()
                uiState = uiState.copy(times = defaults, errors = LessonTimeRules.invalidTimeIndices(defaults))
                TimeEditorEffect.NONE
            }
            TimeEditorConfirmation.DISCARD -> TimeEditorEffect.RETURN_HOME
            else -> TimeEditorEffect.NONE
        }
    }
}


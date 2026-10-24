/*
 * フロントエンド・状態管理: ホームと時間割編集の選択枠・一時停止の状態管理。
 * 表示状態と未保存の入力を保持する。端末APIの直接操作を含めず、サービスや設定の契約へ依頼する。
 */
package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.super_kemo_taro3.backend.service.NextLesson
import com.example.super_kemo_taro3.backend.service.NotificationModeService
import com.example.super_kemo_taro3.backend.service.PauseService
import com.example.super_kemo_taro3.backend.service.TimetableLesson
import com.example.super_kemo_taro3.backend.service.TimetableRules
import com.example.super_kemo_taro3.backend.service.TimetableService

// 1. ホームの選択枠・一時停止
/** ホーム・設定が共用する選択枠、一時停止、次の授業の状態。枠名はMON_1などの保存用の名前。 */
data class HomeUiState(
    val blockedSlots: Set<String> = emptySet(),
    val paused: Boolean = false,
    val nextLesson: NextLesson? = null,
    val selectedDay: Int? = null,
    val dayLessons: List<TimetableLesson> = emptyList()
)

/** ホームの表示状態を管理し、停止・許可確認を担当する処理へ依頼する。 */
class HomeViewModel(
    private val timetable: TimetableService,
    private val pause: PauseService,
    private val notifications: NotificationModeService
) : ViewModel() {
    var uiState by mutableStateOf(HomeUiState())
        private set

    fun refresh() {
        // 古い受信先の予約が残っていれば処理側で移し、その後に表示用の最新状態を作る。
        timetable.migrateReceiverIfNeeded()
        val selectedDay = uiState.selectedDay
        uiState = HomeUiState(
            blockedSlots = timetable.readSelectedSlots(),
            paused = pause.isPaused(),
            nextLesson = timetable.readNextLesson(),
            selectedDay = selectedDay,
            dayLessons = selectedDay?.let(timetable::readLessonsForDay) ?: emptyList()
        )
    }

    /** ホームの枠を押したときは、その曜日の授業一覧を読み込む。 */
    fun selectDay(slotKey: String) {
        val day = TimetableRules.parseSlot(slotKey)?.calendarDay ?: return
        uiState = uiState.copy(selectedDay = day, dayLessons = timetable.readLessonsForDay(day))
    }

    fun togglePause() {
        pause.toggle()
        refresh()
    }

    fun hasNotificationAccess(): Boolean = notifications.hasAccess()

    fun canScheduleAlarms(): Boolean = timetable.canScheduleAlarms()
}

// 2. 時間割編集の選択枠
/** 選択済みのblockedSlotsを保持し、TimetableServiceへ枠の操作を依頼してから表示を再読み込みする。 */
class TimetableEditorViewModel(private val timetable: TimetableService) : ViewModel() {
    var blockedSlots by mutableStateOf<Set<String>>(emptySet())
        private set

    fun refresh() {
        blockedSlots = timetable.readSelectedSlots()
    }

    fun toggleSlot(key: String): Boolean {
        // クリック時点で選択を保存する。trueは予約用の許可が必要なことを画面へ伝える。
        val needsPermission = timetable.toggleSlot(key)
        refresh()
        return needsPermission
    }
}


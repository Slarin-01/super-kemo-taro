/*
 * フロントエンド・画面: 時限カスタム画面の起動、時刻欄、確認ダイアログ、時計・キーボード入力。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.frontend.viewmodel.LessonTimeEditorViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.LessonTimeUiState
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory
import com.example.super_kemo_taro3.frontend.viewmodel.TimeEditorConfirmation
import com.example.super_kemo_taro3.frontend.viewmodel.TimeEditorEffect

// 1. 時刻編集の起動、復元、保存後の遷移
/** LessonTimeEditorViewModelを生成し、LessonTimeEditorScreenへ状態と操作を渡す。保存・破棄後はHomeActivityへ戻る。 */
class LessonTimeEditorActivity : AppCompatActivity() {
    private lateinit var model: LessonTimeEditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 保存前の編集値・開いている欄・確認画面を、Androidの復元データから渡す。
        model = ViewModelProvider(this, ScreenViewModelFactory {
            LessonTimeEditorViewModel(
                appServices.lessonTimes,
                savedInstanceState?.getStringArrayList("editing_times"),
                savedInstanceState?.getInt("selected_time_index", -1) ?: -1,
                savedInstanceState?.getString("time_confirmation")
            )
        })[LessonTimeEditorViewModel::class.java]
        setContent {
            AppScreen {
                LessonTimeEditorScreen(
                    state = model.uiState,
                    onOpenTime = model::openTime,
                    onConfirmTime = model::confirmTime,
                    onCloseTime = model::closeTime,
                    onRequestConfirmation = model::requestConfirmation,
                    onDismissConfirmation = model::dismissConfirmation,
                    onConfirmAction = {
                        when (model.confirmAction()) {
                            TimeEditorEffect.RETURN_HOME_NEEDS_ALARM_PERMISSION -> {
                                // 時刻の保存は完了済み。予約に必要な許可を案内してからホームへ戻る。
                                PermissionSettingsNavigator.openExactAlarmAccess(this)
                                returnHome()
                            }
                            TimeEditorEffect.RETURN_HOME -> returnHome()
                            TimeEditorEffect.NONE -> Unit
                        }
                    }
                )
            }
        }
    }

    // ViewModelの再利用だけでは復元できないプロセス終了にも備えてBundleへ残す。
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("editing_times", ArrayList(model.uiState.times))
        outState.putInt("selected_time_index", model.uiState.selectedIndex)
        outState.putString("time_confirmation", model.uiState.confirmation?.name)
        super.onSaveInstanceState(outState)
    }

    private fun returnHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

// 2. 時刻欄と確認ダイアログの表示
/** 16時刻を8行に表示し、赤字と「!」でエラー欄を示す。選択欄と確認種別に応じたダイアログを描画する。 */
@Composable
fun LessonTimeEditorScreen(
    state: LessonTimeUiState,
    onOpenTime: (Int) -> Unit,
    onConfirmTime: (Int, Int) -> Unit,
    onCloseTime: () -> Unit,
    onRequestConfirmation: (TimeEditorConfirmation) -> Unit,
    onDismissConfirmation: () -> Unit,
    onConfirmAction: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("時限カスタム", style = MaterialTheme.typography.headlineSmall)
        for (period in 1..8) {
            // 16時刻は「1限開始、1限終了、2限開始、2限終了…」の順。開始は偶数、終了は次の添字。
            val startIndex = (period - 1) * 2
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${period}限")
                TimeField(state.times[startIndex], startIndex in state.errors, Modifier.weight(1f)) {
                    onOpenTime(startIndex)
                }
                Text("〜")
                TimeField(state.times[startIndex + 1], startIndex + 1 in state.errors, Modifier.weight(1f)) {
                    onOpenTime(startIndex + 1)
                }
            }
        }
        if (state.errors.isNotEmpty()) {
            Text("開始・終了時刻の順番と、時限の重複を確認してください。", color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { onRequestConfirmation(TimeEditorConfirmation.SAVE) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("保存して戻る") }
        OutlinedButton(
            onClick = { onRequestConfirmation(TimeEditorConfirmation.RESET) }, modifier = Modifier.fillMaxWidth()
        ) { Text("初期化") }
        OutlinedButton(
            onClick = { onRequestConfirmation(TimeEditorConfirmation.DISCARD) }, modifier = Modifier.fillMaxWidth()
        ) { Text("保存せず戻る") }
    }

    if (state.selectedIndex >= 0) {
        // 別の欄を開くときは、直前の時刻選択の状態を使い回さない。
        key(state.selectedIndex) {
            LessonTimeDialog(
                initialHour = state.initialHour, initialMinute = state.initialMinute,
                onConfirm = onConfirmTime, onDismiss = onCloseTime
            )
        }
    }
    when (state.confirmation) {
        TimeEditorConfirmation.SAVE -> ConfirmationDialog(
            message = "変更した時間を保存しますか？", confirmText = "保存する",
            onConfirm = onConfirmAction, onDismiss = onDismissConfirmation
        )
        TimeEditorConfirmation.DISCARD -> ConfirmationDialog(
            message = "変更した時間を保存せず戻りますか？", confirmText = "保存せず戻る",
            onConfirm = onConfirmAction, onDismiss = onDismissConfirmation
        )
        TimeEditorConfirmation.RESET -> ConfirmationDialog(
            message = "初期設定である岡山大学の授業に合わせたタイムテーブルに戻しますか？",
            confirmText = "初期設定に戻す", onConfirm = onConfirmAction, onDismiss = onDismissConfirmation
        )
        TimeEditorConfirmation.ERROR -> ConfirmationDialog(
            title = "入力エラー",
            message = "時間の順番に矛盾があるか、他の時限と重複しています。赤いマークがついている場所を修正してください。",
            confirmText = "OK", onConfirm = onDismissConfirmation, onDismiss = onDismissConfirmation, showCancel = false
        )
        null -> Unit
    }
}

/** 時刻とエラー印を表示する入力欄。編集はクリック先の時刻選択画面で行う。 */
@Composable
private fun TimeField(value: String, error: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(
            if (error) "$value !" else value,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

// 3. 時計・キーボードによる時刻入力
/** TimePickerとTimeInputを切り替え、確定時に時・分をコールバックで返す。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 入力中はダイアログ内だけで値を保持し、「OK」の時点で編集状態へ渡す。
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    var keyboardInput by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("時刻を選択") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (keyboardInput) TimeInput(state) else TimePicker(state)
                TextButton(onClick = { keyboardInput = !keyboardInput }) {
                    Text(if (keyboardInput) "時計で選択" else "キーボードで入力")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

// 4. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "時限カスタム・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "時限カスタム・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LessonTimeEditorScreenPreview() {
    // プレビューの入力例。保存設定を読み込まず、画面が必要とする16時刻を渡す。
    val times = listOf(
        "08:40", "09:30", "09:40", "10:30", "10:45", "11:35", "11:45", "12:35",
        "13:15", "14:25", "14:25", "15:15", "15:30", "16:20", "16:30", "17:20"
    )
    AppScreen {
        LessonTimeEditorScreen(
            state = LessonTimeUiState(times = times, errors = emptySet()),
            onOpenTime = {},
            onConfirmTime = { _, _ -> },
            onCloseTime = {},
            onRequestConfirmation = {},
            onDismissConfirmation = {},
            onConfirmAction = {}
        )
    }
}


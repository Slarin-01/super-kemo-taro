package com.example.super_kemo_taro3

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 1〜8限の時刻を編集する画面。変更内容の保存・初期化はActivityへ通知する。 */
@Composable
fun TimeSettingsScreen(
    times: List<String>,
    onTimeChange: (Int, String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onDiscard: () -> Unit
) {
    // -1は時刻選択ダイアログを閉じている状態。添字と確認ダイアログの種類は回転後も復元する。
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var confirmation by rememberSaveable { mutableStateOf<String?>(null) }
    // timesが更新されるたびに再評価され、不正な入力欄を赤く表示する。
    val errors = invalidTimeIndices(times)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("時限カスタム", style = MaterialTheme.typography.headlineSmall)
        for (period in 1..8) {
            // 開始・終了の2要素で1時限を表すため、時限番号をリストの添字に変換する。
            val startIndex = (period - 1) * 2
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${period}限")
                TimeField(times[startIndex], startIndex in errors, Modifier.weight(1f)) {
                    selectedIndex = startIndex
                }
                Text("〜")
                TimeField(times[startIndex + 1], startIndex + 1 in errors, Modifier.weight(1f)) {
                    selectedIndex = startIndex + 1
                }
            }
        }
        if (errors.isNotEmpty()) {
            Text("開始・終了時刻の順番と、時限の重複を確認してください。", color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { confirmation = if (errors.isEmpty()) "save" else "error" },
            modifier = Modifier.fillMaxWidth()
        ) { Text("保存して戻る") }
        OutlinedButton(onClick = { confirmation = "reset" }, modifier = Modifier.fillMaxWidth()) {
            Text("初期化")
        }
        OutlinedButton(onClick = { confirmation = "discard" }, modifier = Modifier.fillMaxWidth()) {
            Text("保存せず戻る")
        }
    }

    if (selectedIndex >= 0) {
        // 別の入力欄を開いたとき、前の欄の時刻選択状態を使い回さないようにする。
        key(selectedIndex) {
            ComposeTimeDialog(
                value = times[selectedIndex],
                fallback = timeSettings[selectedIndex],
                onConfirm = { value ->
                    onTimeChange(selectedIndex, value)
                    selectedIndex = -1
                },
                onDismiss = { selectedIndex = -1 }
            )
        }
    }

    val dismiss = { confirmation = null }
    // 保存・破棄・初期化は確認後に実行し、不正な時刻の場合は修正を促す。
    when (confirmation) {
        "save" -> ConfirmationDialog(
            message = "変更した時間を保存しますか？", confirmText = "保存する",
            onConfirm = { dismiss(); onSave() }, onDismiss = dismiss
        )
        "discard" -> ConfirmationDialog(
            message = "変更した時間を保存せず戻りますか？", confirmText = "保存せず戻る",
            onConfirm = { dismiss(); onDiscard() }, onDismiss = dismiss
        )
        "reset" -> ConfirmationDialog(
            message = "初期設定である岡山大学の授業に合わせたタイムテーブルに戻しますか？",
            confirmText = "初期設定に戻す", onConfirm = { dismiss(); onReset() }, onDismiss = dismiss
        )
        "error" -> ConfirmationDialog(
            title = "入力エラー",
            message = "時間の順番に矛盾があるか、他の時限と重複しています。赤いマークがついている場所を修正してください。",
            confirmText = "OK", onConfirm = dismiss, onDismiss = dismiss, showCancel = false
        )
    }
}

/** 時刻選択を開く入力欄。不正な時刻は赤字と「!」で示す。 */
@Composable
private fun TimeField(value: String, error: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(
            if (error) "$value !" else value,
            color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

/** 時計とキーボードを切り替えられる24時間制の時刻選択ダイアログ。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComposeTimeDialog(
    value: String,
    fallback: TimeSetting,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 保存済みの時刻が不正でもダイアログを開けるよう、その入力欄の初期時刻に戻す。
    val minutes = timeToMinutes(value) ?: (fallback.defaultHour * 60 + fallback.defaultMinute)
    val state = rememberTimePickerState(initialHour = minutes / 60, initialMinute = minutes % 60, is24Hour = true)
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
            TextButton(onClick = { onConfirm(formatTime(state.hour, state.minute)) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

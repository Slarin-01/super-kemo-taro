package com.example.super_kemo_taro3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 各設定画面への入口となるメニュー。画面遷移は呼び出し元のActivityが行う。 */
@Composable
fun MenuScreen(
    creditMessage: String,
    onMain: () -> Unit,
    onTimetable: () -> Unit,
    onTimeSettings: () -> Unit,
    onNotificationTest: () -> Unit
) {
    // 画面回転などで再作成されても、クレジットを開いていた状態を復元する。
    var showCredits by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.End
    ) {
        Button(onClick = onMain, modifier = Modifier.fillMaxWidth()) { Text("時間割を見る") }
        Button(onClick = onTimetable, modifier = Modifier.fillMaxWidth()) { Text("時刻設定") }
        Button(onClick = onTimeSettings, modifier = Modifier.fillMaxWidth()) { Text("時限カスタム") }
        Button(onClick = onNotificationTest, modifier = Modifier.fillMaxWidth()) { Text("通知切り替えテスト") }
        Button(onClick = { showCredits = true }) { Text("クレジット") }
    }
    if (showCredits) {
        ConfirmationDialog(
            title = "クレジット", message = creditMessage, confirmText = "閉じる",
            onConfirm = { showCredits = false }, onDismiss = { showCredits = false }, showCancel = false
        )
    }
}

/** 通知モードの現在の状態と、手動切り替えボタンを表示するテスト画面。 */
@Composable
fun NotificationTestScreen(status: String, onToggle: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("通知切り替えテスト", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = onToggle) { Text("通知モード 切替") }
        Text(status, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onBack) { Text("メニューに戻る") }
    }
}

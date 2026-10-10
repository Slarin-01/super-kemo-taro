package com.example.super_kemo_taro3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Activityに適用されている明暗モードに合わせて、Compose全体の配色を設定する。 */
@Composable
fun KemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}

/** 各画面共通の背景と、システムバー・キーボードに重ならない表示領域を用意する。 */
@Composable
fun AppScreen(content: @Composable () -> Unit) {
    KemoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                content()
            }
        }
    }
}

/** 時間割の表示・編集で共用する表。onSlotClickを省略すると閲覧専用になる。 */
@Composable
fun TimetableGrid(blockedSlots: Set<String>, onSlotClick: ((String) -> Unit)? = null) {
    // 英字は保存・アラーム登録で使う曜日キー、日本語は画面に表示する曜日名。
    val days = listOf("MON" to "月", "TUE" to "火", "WED" to "水", "THU" to "木", "FRI" to "金")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            days.forEach { (_, label) ->
                Box(Modifier.weight(1f).padding(8.dp), contentAlignment = Alignment.Center) {
                    Text(label)
                }
            }
        }
        for (period in 1..8) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                days.forEach { (day, label) ->
                    // 例: MON_1は月曜日の1限。ミュート対象の枠を緑色で表示する。
                    val slotKey = "${day}_$period"
                    val isSelected = slotKey in blockedSlots
                    val clickModifier = if (onSlotClick != null) {
                        Modifier.clickable(role = Role.Button) { onSlotClick(slotKey) }
                    } else Modifier
                    Box(
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .background(if (isSelected) Color.Green else Color.LightGray)
                            .then(clickModifier)
                            // 読み上げ機能にも曜日・時限・選択状態を伝える。
                            .semantics {
                                contentDescription = "${label}曜日 ${period}限"
                                selected = isSelected
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(period.toString(), color = Color.Black)
                    }
                }
            }
        }
    }
}

/** 時間割と一時停止状態を表示するホーム画面。操作の処理はActivityへ渡す。 */
@Composable
fun MainScreen(
    blockedSlots: Set<String>,
    paused: Boolean,
    onTogglePause: () -> Unit,
    onToggleTheme: () -> Unit,
    onEdit: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onTogglePause, modifier = Modifier.fillMaxWidth()) {
            Text("日曜日まで機能を停止")
        }
        Button(onClick = onToggleTheme, modifier = Modifier.fillMaxWidth()) {
            Text(if (dark) "ホワイトモードへ" else "ダークモードへ")
        }
        Text(
            if (paused) "日曜日まで停止: ON" else "日曜日まで停止: OFF",
            color = if (paused) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        TimetableGrid(blockedSlots)
        Button(onClick = onEdit) { Text("編集") }
    }
}

/** 時間割の枠をタップして、ミュート対象を選択・解除する画面。 */
@Composable
fun TimetableScreen(blockedSlots: Set<String>, onSlotClick: (String) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimetableGrid(blockedSlots, onSlotClick)
        Button(onClick = onBack) { Text("メニューに戻る") }
    }
}

/** 保存・初期化・クレジット表示などで共用するダイアログ。長い本文はスクロールできる。 */
@Composable
fun ConfirmationDialog(
    title: String = "確認",
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showCancel: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message, Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = {
            if (showCancel) TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}

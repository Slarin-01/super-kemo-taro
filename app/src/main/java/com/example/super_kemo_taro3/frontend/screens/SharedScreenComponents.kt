/*
 * フロントエンド・画面: 共通テーマ・画面領域・時間割・確認ダイアログと表示に関係する端末操作。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// 1. 共通テーマと画面領域
/** BellThemeがlightColorScheme・darkColorSchemeを設定し、AppScreenがSurface・Boxで画面の共通背景を提供する。 */
@Composable
fun BellTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme(),
        content = content
    )
}


/** システムバーとキーボードを避ける余白を付け、各画面の内容を共通の背景へ載せる。 */
@Composable
fun AppScreen(content: @Composable () -> Unit) {
    BellTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                content()
            }
        }
    }
}

// 2. ホームと編集画面で共用する時間割
/** 40枠の表を表示し、選択済みを緑、未選択を灰色、文字を黒で描画する。クリック処理省略時は閲覧専用。 */
@Composable
fun TimetableGrid(blockedSlots: Set<String>, onSlotClick: ((String) -> Unit)? = null) {
    // ここは表示用の月〜金。FRIを実際に何曜日で動かすかはバックエンドのTimetableRulesで決める。
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
                    // 保存・処理側と共通の枠名（例: MON_1）を、押した枠の識別に使う。
                    val slotKey = "${day}_$period"
                    val isSelected = slotKey in blockedSlots
                    val clickModifier = if (onSlotClick != null) {
                        Modifier.clickable(role = Role.Button) { onSlotClick(slotKey) }
                    } else Modifier
                    Box(
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                            .background(if (isSelected) Color.Green else Color.LightGray)
                            .then(clickModifier)
                            // 読み上げにも曜日・時限・選択状態を伝える。
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

// 3. 共通の確認ダイアログ
/** タイトル、スクロール可能な本文、確定ボタン、任意のキャンセルボタンを表示する。 */
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

// 4. 表示テーマの端末への適用
/** Resourcesから現在の明暗を読み、AppCompatDelegateでモードを適用する。 */
object AppThemeController {
    fun isDark(resources: Resources): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun apply(isDark: Boolean?) {
        // 未設定なら明暗を強制せず、端末側の表示設定を使う。
        if (isDark == null) return
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}

// 5. 権限を許可する端末設定画面への案内
/** Activityを受け取り、通知ポリシーアクセス・正確なアラームのAndroid設定画面を開く。 */
object PermissionSettingsNavigator {
    fun openNotificationAccess(activity: Activity) {
        activity.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    fun openExactAlarmAccess(activity: Activity) {
        // 正確なアラーム用の許可画面があるAndroid 12以降で案内する。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            activity.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }
}

// 6. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "共通時間割・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "共通時間割・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimetableGridPreview() {
    AppScreen {
        TimetableGrid(blockedSlots = setOf("MON_1", "TUE_3", "FRI_5"))
    }
}


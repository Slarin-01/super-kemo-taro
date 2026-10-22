/*
 * フロントエンド・画面: 設定メニューの起動、画面遷移、クレジット表示。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// 1. メニューの起動と画面遷移
/** 既存のメニュー起動先は維持し、共通の親画面にある設定タブへ渡す。 */
class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, HomeActivity::class.java)
            .putExtra(HomeActivity.EXTRA_DESTINATION, MainDestination.Settings.name)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION))
        finish()
    }
}

// 2. メニューとクレジットの表示
/** 設定項目とクレジットダイアログを表示する。下部バーは共通の親画面が持つ。 */
@Composable
fun MenuScreen(
    creditMessage: String,
    showCredits: Boolean,
    onOpenCredits: () -> Unit,
    onCloseCredits: () -> Unit,
    onMoodle: () -> Unit,
    onTimetable: () -> Unit,
    onTimeSettings: () -> Unit,
    onNotificationTest: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.End
    ) {
        Button(onClick = onMoodle, modifier = Modifier.fillMaxWidth()) { Text("履修を取り込む") }
        Button(onClick = onTimetable, modifier = Modifier.fillMaxWidth()) { Text("時刻設定") }
        Button(onClick = onTimeSettings, modifier = Modifier.fillMaxWidth()) { Text("時限カスタム") }
        Button(onClick = onNotificationTest, modifier = Modifier.fillMaxWidth()) { Text("通知切り替えテスト") }
        Button(onClick = onOpenCredits) { Text("クレジット") }
    }
    if (showCredits) {
        ConfirmationDialog(
            title = "クレジット", message = creditMessage, confirmText = "閉じる",
            onConfirm = onCloseCredits, onDismiss = onCloseCredits, showCancel = false
        )
    }
}

// 3. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "設定メニュー・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "設定メニュー・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MenuScreenPreview() {
    AppScreen {
        MainScreenScaffold(destination = MainDestination.Settings, onHome = {}, onSettings = {}) {
            MenuScreen(
                creditMessage = "言十ベル",
                showCredits = false,
                onOpenCredits = {},
                onCloseCredits = {},
                onMoodle = {},
                onTimetable = {},
                onTimeSettings = {},
                onNotificationTest = {}
            )
        }
    }
}


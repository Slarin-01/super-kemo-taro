/*
 * フロントエンド・画面: 設定メニューの起動、画面遷移、クレジット表示。
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
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.frontend.viewmodel.MenuViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory

// 1. メニューの起動と画面遷移
/** MenuViewModelとMenuScreenを接続し、ホーム・時間割編集・時限カスタム・通知テストへ明示Intentで遷移する。 */
class MenuActivity : AppCompatActivity() {
    private lateinit var model: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Androidによる画面の作り直しでも、クレジットの開閉状態を引き継ぐ。
        model = ViewModelProvider(this, ScreenViewModelFactory {
            MenuViewModel(savedInstanceState?.getBoolean("show_credits", false) ?: false)
        })[MenuViewModel::class.java]
        setContent {
            AppScreen {
                MenuScreen(
                    creditMessage = model.creditMessage,
                    showCredits = model.showCredits,
                    onOpenCredits = model::openCredits,
                    onCloseCredits = model::closeCredits,
                    // メニューを残して開く。時間割編集・通知テストの「戻る」はここへ、時限カスタムの保存・破棄後はホームへ移る。
                    onMain = { startActivity(Intent(this, HomeActivity::class.java)) },
                    onMoodle = { startActivity(Intent(this, MoodleImportActivity::class.java)) },
                    onTimetable = { startActivity(Intent(this, TimetableEditorActivity::class.java)) },
                    onTimeSettings = { startActivity(Intent(this, LessonTimeEditorActivity::class.java)) },
                    onNotificationTest = { startActivity(Intent(this, NotificationTestActivity::class.java)) }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // プロセスが終了した後の画面復元にも使う。設定としての永続保存ではない。
        outState.putBoolean("show_credits", model.showCredits)
        super.onSaveInstanceState(outState)
    }
}

// 2. メニューとクレジットの表示
/** メニューのボタンとクレジットダイアログを表示し、それぞれのコールバックを呼ぶ。 */
@Composable
fun MenuScreen(
    creditMessage: String,
    showCredits: Boolean,
    onOpenCredits: () -> Unit,
    onCloseCredits: () -> Unit,
    onMain: () -> Unit,
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
        Button(onClick = onMain, modifier = Modifier.fillMaxWidth()) { Text("時間割を見る") }
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
        MenuScreen(
            creditMessage = "言十ベル",
            showCredits = false,
            onOpenCredits = {},
            onCloseCredits = {},
            onMain = {},
            onMoodle = {},
            onTimetable = {},
            onTimeSettings = {},
            onNotificationTest = {}
        )
    }
}


/*
 * フロントエンド・画面: 通知切り替えテスト画面の起動、表示、権限設定への案内。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.core.content.ContextCompat
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.frontend.viewmodel.NotificationTestViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory

// 1. 通知テストの起動と操作の接続
/** NotificationTestViewModelとNotificationTestScreenを接続する。起動時・再表示時に状態を更新し、必要時に通知アクセス設定を開く。 */
class NotificationTestActivity : AppCompatActivity() {
    private val notificationChanges = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = model.refresh()
    }
    private val model by lazy {
        ViewModelProvider(this, ScreenViewModelFactory {
            NotificationTestViewModel(appServices.notifications)
        })[NotificationTestViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        model.refresh()
        setContent {
            AppScreen {
                NotificationTestScreen(
                    model.status,
                    onToggle = {
                        // 戻り値のtrueは「許可の案内が必要」。ミュート状態そのものではない。
                        if (model.toggle()) PermissionSettingsNavigator.openNotificationAccess(this)
                    },
                    onBack = ::finish,
                    needsSystemSettings = model.needsSystemSettings,
                    onSystemSettings = { PermissionSettingsNavigator.openNotificationModeSettings(this) }
                )
            }
        }
        if (!model.hasAccess()) PermissionSettingsNavigator.openNotificationAccess(this)
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(this, notificationChanges, IntentFilter().apply {
            addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            addAction(NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                addAction(NotificationManager.ACTION_AUTOMATIC_ZEN_RULE_STATUS_CHANGED)
            }
        }, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStop() {
        unregisterReceiver(notificationChanges)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        // 端末設定で許可を変更して戻った後も、実際の通知状態を確認する。
        model.refresh()
    }
}

// 2. 通知テストの表示
/** タイトル、通知モード切替ボタン、status、戻るボタンを表示する。 */
@Composable
fun NotificationTestScreen(
    status: String,
    onToggle: () -> Unit,
    onBack: () -> Unit,
    needsSystemSettings: Boolean = false,
    onSystemSettings: () -> Unit = {}
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("通知切り替えテスト", style = MaterialTheme.typography.headlineSmall)
        Text("このアプリの通知モード（ON＝ミュート）")
        Button(onClick = onToggle) { Text("通知モード 切替") }
        Text(status, style = MaterialTheme.typography.bodyLarge)
        if (needsSystemSettings) {
            Text("本体設定や別のモードによるミュートが有効です。通知を戻すには、本体側の設定も解除してください。")
            Button(onClick = onSystemSettings) { Text("本体の通知モード設定を開く") }
        }
        Button(onClick = onBack) { Text("通知設定に戻る") }
    }
}

// 3. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "通知テスト・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "通知テスト・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationTestScreenPreview() {
    AppScreen {
        NotificationTestScreen(
            status = "通知モード：OFF",
            onToggle = {},
            onBack = {}
        )
    }
}


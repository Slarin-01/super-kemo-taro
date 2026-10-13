/*
 * フロントエンド・画面: ホーム画面の起動、状態の接続、一時停止・テーマ・編集ボタンの表示。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.frontend.viewmodel.HomeViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory

// 1. ホーム画面の起動とViewModelへの操作の接続
/** ホームを開き、表示する状態とボタン操作をHomeViewModelへつなぐ。許可不足は端末の設定画面で案内する。 */
class HomeActivity : AppCompatActivity() {
    private val model by lazy {
        val services = appServices
        ViewModelProvider(this, ScreenViewModelFactory {
            HomeViewModel(services.settings, services.timetable, services.pause, services.notifications)
        })[HomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        model.refresh()
        setContent {
            AppScreen {
                HomeScreen(
                    blockedSlots = model.uiState.blockedSlots,
                    paused = model.uiState.paused,
                    onTogglePause = model::togglePause,
                    onToggleTheme = {
                        AppThemeController.apply(model.toggleTheme(AppThemeController.isDark(resources)))
                    },
                    onEdit = { startActivity(Intent(this, MenuActivity::class.java)) }
                )
            }
        }
        // 許可の判定は処理側、設定画面を開く操作は画面側が担当する。
        if (!model.hasNotificationAccess()) PermissionSettingsNavigator.openNotificationAccess(this)
        if (!model.canScheduleAlarms()) PermissionSettingsNavigator.openExactAlarmAccess(this)
    }

    override fun onResume() {
        super.onResume()
        // 編集や端末設定から戻ったときに、最新の選択枠・一時停止を表示する。
        model.refresh()
    }
}

// 2. ホーム画面のCompose表示
/** 停止・明暗切替・編集ボタン、停止状態、閲覧専用時間割を表示する。 */
@Composable
fun HomeScreen(
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

// 3. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "ホーム・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "ホーム・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    AppScreen {
        HomeScreen(
            blockedSlots = setOf("MON_1", "TUE_3", "FRI_5"),
            paused = false,
            onTogglePause = {},
            onToggleTheme = {},
            onEdit = {}
        )
    }
}


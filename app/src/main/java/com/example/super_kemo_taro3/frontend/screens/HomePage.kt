/*
 * フロントエンド・画面: ホーム・設定の起動、状態の接続、下部ナビゲーションの表示。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.frontend.viewmodel.HomeViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.MenuViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory

// 1. ホーム画面の起動とViewModelへの操作の接続
/** ホームと設定を同じActivityで表示し、下部バーを保持したまま本文を切り替える。 */
class HomeActivity : AppCompatActivity() {
    private var destination by mutableStateOf(MainDestination.Home)
    private lateinit var menuModel: MenuViewModel

    private val model by lazy {
        val services = appServices
        ViewModelProvider(this, ScreenViewModelFactory {
            HomeViewModel(services.timetable, services.pause, services.notifications)
        })[HomeViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        destination = restoredDestination(
            savedInstanceState?.getString(EXTRA_DESTINATION) ?: intent.getStringExtra(EXTRA_DESTINATION)
        )
        menuModel = ViewModelProvider(this, ScreenViewModelFactory {
            MenuViewModel(savedInstanceState?.getBoolean("show_credits", false) ?: false)
        })[MenuViewModel::class.java]
        model.refresh()
        setContent {
            AppScreen {
                BackHandler(enabled = destination == MainDestination.Settings) {
                    selectDestination(MainDestination.Home)
                }
                MainScreenScaffold(
                    destination = destination,
                    onHome = { selectDestination(MainDestination.Home) },
                    onSettings = { selectDestination(MainDestination.Settings) }
                ) { page ->
                    when (page) {
                        MainDestination.Home -> HomeScreen(
                            blockedSlots = model.uiState.blockedSlots
                        )
                        MainDestination.Settings -> MenuScreen(
                            creditMessage = menuModel.creditMessage,
                            showCredits = menuModel.showCredits,
                            onOpenCredits = menuModel::openCredits,
                            onCloseCredits = menuModel::closeCredits,
                            onMoodle = { startActivity(Intent(this, MoodleImportActivity::class.java)) },
                            onTimetable = { startActivity(Intent(this, TimetableEditorActivity::class.java)) },
                            onTimeSettings = { startActivity(Intent(this, LessonTimeEditorActivity::class.java)) },
                            onNotificationTest = { startActivity(Intent(this, NotificationTestActivity::class.java)) },
                            onThemeSettings = { startActivity(Intent(this, ThemeEditorActivity::class.java)) },
                            paused = model.uiState.paused,
                            onTogglePause = model::togglePause
                        )
                    }
                }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        selectDestination(restoredDestination(intent.getStringExtra(EXTRA_DESTINATION)))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXTRA_DESTINATION, destination.name)
        outState.putBoolean("show_credits", menuModel.showCredits)
        super.onSaveInstanceState(outState)
    }

    private fun selectDestination(next: MainDestination) {
        // 設定タブへ移るときも、停止期限が過ぎていないか確認する。
        model.refresh()
        destination = next
    }

    private fun restoredDestination(name: String?): MainDestination =
        MainDestination.entries.firstOrNull { it.name == name } ?: MainDestination.Home

    companion object {
        const val EXTRA_DESTINATION = "main_destination"
    }
}

// 2. ホーム画面のCompose表示
/** 閲覧専用の時間割を表示する。下部バーは共通の親画面が持つ。 */
@Composable
fun HomeScreen(
    blockedSlots: Set<String>
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        TimetableGrid(blockedSlots)
    }
}

// 3. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "ホーム・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "ホーム・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    AppScreen {
        MainScreenScaffold(destination = MainDestination.Home, onHome = {}, onSettings = {}) {
            HomeScreen(
                blockedSlots = setOf("MON_1", "TUE_3", "FRI_5")
            )
        }
    }
}


/*
 * フロントエンド・画面: 設定メニューの起動、画面遷移、クレジット表示。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.super_kemo_taro3.R

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
/** 大きな見出しと、アイコン・説明文付きの設定リストを表示する。 */
@Composable
fun MenuScreen(
    creditMessage: String,
    showCredits: Boolean,
    onOpenCredits: () -> Unit,
    onCloseCredits: () -> Unit,
    onMoodle: () -> Unit,
    onTimetable: () -> Unit,
    onTimeSettings: () -> Unit,
    onNotificationSettings: () -> Unit,
    onNotificationTest: () -> Unit,
    onThemeSettings: () -> Unit,
    paused: Boolean,
    onTogglePause: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp).padding(top = 40.dp, bottom = 24.dp)
    ) {
        Text(
            "設定",
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 40.dp),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Normal
            )
        )
        val entries = listOf(
            SettingsMenuEntry(
                "日曜日まで機能を停止",
                if (paused) "日曜日まで停止: ON" else "日曜日まで停止: OFF",
                R.drawable.ic_menu_pause,
                onTogglePause,
                checked = paused
            ),
            SettingsMenuEntry("テーマを編集", "ライト・ダークやテーマカラーを変更します", R.drawable.ic_menu_palette, onThemeSettings),
            SettingsMenuEntry("履修を取り込む", "Moodleから履修情報を取り込みます", R.drawable.ic_menu_import, onMoodle),
            SettingsMenuEntry("時刻設定", "通知を抑える曜日と時限を選びます", R.drawable.ic_menu_timetable, onTimetable),
            SettingsMenuEntry("時限カスタム", "各時限の開始・終了時刻を変更します", R.drawable.ic_menu_clock, onTimeSettings),
            SettingsMenuEntry("通知設定", "授業終了後の通知ミュートの扱いを選びます", R.drawable.ic_menu_notifications, onNotificationSettings),
            SettingsMenuEntry("通知切り替えテスト", "通知のミュート・解除を確認します", R.drawable.ic_menu_notifications, onNotificationTest),
            SettingsMenuEntry("クレジット", "アプリの情報・制作メンバー・ライセンスを表示します", R.drawable.ic_menu_info, onOpenCredits)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            entries.forEachIndexed { index, entry ->
                SettingsMenuRow(
                    entry = entry,
                    first = index == 0,
                    last = index == entries.lastIndex
                )
            }
        }
    }
    if (showCredits) {
        val resources = LocalResources.current
        val apacheLicense = remember(resources) {
            resources.openRawResource(R.raw.apache_license_2_0)
                .bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
        }
        ConfirmationDialog(
            title = "クレジット",
            message = "$creditMessage\n\n■ Apache License 2.0（原文）\n\n$apacheLicense",
            confirmText = "閉じる",
            onConfirm = onCloseCredits, onDismiss = onCloseCredits, showCancel = false
        )
    }
}

private data class SettingsMenuEntry(
    val title: String,
    val description: String,
    val icon: Int,
    val onClick: () -> Unit,
    val checked: Boolean? = null
)

/** 行全体を押せる面にし、先頭と末尾に大きな角丸を付けてひとまとまりに見せる。 */
@Composable
private fun SettingsMenuRow(entry: SettingsMenuEntry, first: Boolean, last: Boolean) {
    val shape = RoundedCornerShape(
        topStart = if (first) 24.dp else 4.dp,
        topEnd = if (first) 24.dp else 4.dp,
        bottomStart = if (last) 24.dp else 4.dp,
        bottomEnd = if (last) 24.dp else 4.dp
    )
    val action = if (entry.checked != null) {
        Modifier.toggleable(value = entry.checked, role = Role.Switch, onValueChange = { entry.onClick() })
    } else {
        Modifier.clickable(role = Role.Button, onClick = entry.onClick)
    }
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp).clip(shape).then(action),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(entry.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    entry.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (entry.checked == true) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (entry.checked != null) {
                // 操作は行全体で受け取り、スイッチをタップした際の二重切り替えを防ぐ。
                Switch(checked = entry.checked, onCheckedChange = null)
            }
        }
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
                onNotificationSettings = {},
                onNotificationTest = {},
                onThemeSettings = {},
                paused = false,
                onTogglePause = {}
            )
        }
    }
}


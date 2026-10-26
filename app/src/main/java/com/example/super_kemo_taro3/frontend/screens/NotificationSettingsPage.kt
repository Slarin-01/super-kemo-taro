package com.example.super_kemo_taro3.frontend.screens

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.backend.repository.LessonEndNotificationMode
import com.example.super_kemo_taro3.frontend.viewmodel.NotificationSettingsViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory

/** 自動ミュートの切り替えと、授業終了時の通知設定を表示する。 */
class NotificationSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val model = ViewModelProvider(this, ScreenViewModelFactory {
            NotificationSettingsViewModel(appServices.settings, appServices.notifications)
        })[NotificationSettingsViewModel::class.java]
        setContent {
            AppScreen {
                NotificationSettingsScreen(
                    automaticMutingEnabled = model.automaticMutingEnabled,
                    onAutomaticMutingChange = model::setAutomaticMuting,
                    selectedMode = model.selectedMode,
                    onSelectMode = model::selectMode,
                    showSystemModeNotice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM,
                    onSystemSettings = { PermissionSettingsNavigator.openNotificationModeSettings(this) },
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    automaticMutingEnabled: Boolean,
    onAutomaticMutingChange: (Boolean) -> Unit,
    selectedMode: LessonEndNotificationMode,
    onSelectMode: (LessonEndNotificationMode) -> Unit,
    showSystemModeNotice: Boolean,
    onSystemSettings: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("設定に戻る") }
        Text("通知設定", style = MaterialTheme.typography.headlineSmall)
        AutomaticMutingSetting(automaticMutingEnabled, onAutomaticMutingChange)
        Text("授業が終わった後の動作", style = MaterialTheme.typography.titleMedium)
        Text(
            if (automaticMutingEnabled) {
                "登録した授業の終了時刻に適用します。選択すると自動で保存されます。"
            } else {
                "自動ミュートがOFFの間は、授業終了後の動作は実行しません。変更するには自動ミュートをオンにしてください。現在の選択は保持されます。"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NotificationModeOption(
                title = "お休みモードのみを解除(通知はミュート)",
                description = "このアプリのお休みモードを解除し、端末をサイレントにします。通知は受信できますが、通知音・着信音とバイブレーションは鳴りません。",
                selected = selectedMode == LessonEndNotificationMode.KEEP_MUTED,
                enabled = automaticMutingEnabled,
                onClick = { onSelectMode(LessonEndNotificationMode.KEEP_MUTED) }
            )
            NotificationModeOption(
                title = "通知ミュートすべてを解除",
                description = "このアプリのお休みモードと、端末のサイレント・バイブ設定を解除し、通常モードに戻します。通知音・着信音は端末の音量設定に従います。",
                selected = selectedMode == LessonEndNotificationMode.UNMUTE_ALL,
                enabled = automaticMutingEnabled,
                onClick = { onSelectMode(LessonEndNotificationMode.UNMUTE_ALL) }
            )
        }
        if (showSystemModeNotice) {
            Text(
                "Android 15以降では、本体設定や他のアプリで有効にしたお休みモードは自動解除できません。これらが有効な場合は、端末の設定から解除してください。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onSystemSettings) { Text("本体の通知モード設定を開く") }
        }
    }
}

@Composable
private fun AutomaticMutingSetting(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .toggleable(value = enabled, role = Role.Switch, onValueChange = onEnabledChange),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("授業時刻に自動でミュート", style = MaterialTheme.typography.titleMedium)
                Text(
                    "オンにすると授業開始時にミュートし、終了時に下の設定を適用します。オフにすると自動切り替えを停止し、このアプリのお休みモードを解除します。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(checked = enabled, onCheckedChange = null)
        }
    }
}

@Composable
private fun NotificationModeOption(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        shape = shape,
        color = if (selected && enabled) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainer,
        contentColor = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            selected -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }
    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(name = "通知設定・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "通知設定・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationSettingsScreenPreview() {
    AppScreen {
        NotificationSettingsScreen(
            automaticMutingEnabled = true,
            onAutomaticMutingChange = {},
            selectedMode = LessonEndNotificationMode.KEEP_MUTED,
            onSelectMode = {},
            showSystemModeNotice = true,
            onSystemSettings = {},
            onBack = {}
        )
    }
}

@Preview(name = "通知設定・自動ミュートOFF・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "通知設定・自動ミュートOFF・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationSettingsDisabledScreenPreview() {
    AppScreen {
        NotificationSettingsScreen(
            automaticMutingEnabled = false,
            onAutomaticMutingChange = {},
            selectedMode = LessonEndNotificationMode.KEEP_MUTED,
            onSelectMode = {},
            showSystemModeNotice = true,
            onSystemSettings = {},
            onBack = {}
        )
    }
}

package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar

/** 時間割を閲覧するホーム画面。保存済み設定の読み込みと、一時停止・テーマ切り替えを行う。 */
class MainActivity : AppCompatActivity() {
    // Composeが変更を検知できる状態として保持し、設定を読み直すと画面にも反映する。
    private var blockedSlots by mutableStateOf<Set<String>>(emptySet())
    private var paused by mutableStateOf(false)
    private val preferences by lazy { getSharedPreferences("timetable_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshSettings()
        // XMLの代わりにComposeで画面を構築し、ボタン操作をActivityの処理へ結び付ける。
        setContent {
            AppScreen {
                MainScreen(
                    blockedSlots = blockedSlots,
                    paused = paused,
                    onTogglePause = {
                        togglePauseUntilSunday()
                        refreshSettings()
                    },
                    onToggleTheme = ::toggleTheme,
                    onEdit = { startActivity(Intent(this, MainActivity3::class.java)) }
                )
            }
        }
        authchk()
    }

    // 編集画面や権限設定から戻ったとき、時間割と一時停止状態を最新の保存内容に合わせる。
    override fun onResume() {
        super.onResume()
        refreshSettings()
    }

    /** ミュート対象の曜日・時限と、一時停止の期限を保存設定から読み直す。 */
    private fun refreshSettings() {
        blockedSlots = preferences.getStringSet("blocked_slots", emptySet())?.toSet() ?: emptySet()
        paused = isPausedUntilSunday()
    }

    /** 明暗モードの選択を保存し、ActivityとComposeに適用する。 */
    private fun toggleTheme() {
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        getSharedPreferences("theme_prefs", MODE_PRIVATE).edit()
            .putBoolean("is_dark_mode", !isDark).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
        )
    }

    /** 直近の日曜日の23:59:59.999を求める。今日が日曜日の場合は当日の終わりになる。 */
    private fun getNextSundayEndTime(): Long {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysUntilSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else Calendar.SATURDAY - dayOfWeek + 1
        cal.add(Calendar.DAY_OF_YEAR, daysUntilSunday)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    // 保存された期限を過ぎると、設定値が残っていても一時停止は解除扱いになる。
    private fun isPausedUntilSunday(): Boolean =
        System.currentTimeMillis() < preferences.getLong("pause_until_sunday_time", 0L)

    /** 一時停止を切り替える。有効にすると現在のミュートを解除し、期限をReceiverへ共有する。 */
    private fun togglePauseUntilSunday() {
        if (isPausedUntilSunday()) {
            preferences.edit().remove("pause_until_sunday_time").apply()
        } else {
            preferences.edit().putLong("pause_until_sunday_time", getNextSundayEndTime()).apply()
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if (nm.isNotificationPolicyAccessGranted) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    /** 通知モードの変更権限と正確なアラームの権限を確認し、不足時は端末の設定を開く。 */
    private fun authchk() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }
    }
}

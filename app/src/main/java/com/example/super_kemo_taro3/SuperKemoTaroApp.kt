package com.example.super_kemo_taro3

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/** アプリ起動時に、前回保存した明暗モードを各Activityが表示される前に適用する。 */
class SuperKemoTaroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        // 初回起動などで設定がない場合は、ここでは明暗モードを上書きしない。
        if (prefs.contains("is_dark_mode")) {
            val isDark = prefs.getBoolean("is_dark_mode", false)
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}

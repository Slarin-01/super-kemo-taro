package com.example.super_kemo_taro3

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/** メニュー画面。Compose側のボタン操作を、それぞれのActivityへの画面遷移に結び付ける。 */
class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppScreen {
                MenuScreen(
                    creditMessage = creditMessage,
                    onMain = { startActivity(Intent(this, MainActivity::class.java)) },
                    onTimetable = { startActivity(Intent(this, TimetableActivity::class.java)) },
                    onTimeSettings = { startActivity(Intent(this, MainActivity2::class.java)) },
                    onNotificationTest = { startActivity(Intent(this, NotificationTestActivity::class.java)) }
                )
            }
        }
    }

    // メニューのクレジットダイアログに表示するアプリ情報。
    private val creditMessage = """
            ■ アプリ名
            言十ベル
            
            ■ バージョン
            1.0
            
            ■ グループ名
            スーパーKEMOたろう
            
            ■ メンバー(敬称略)
            スラリン
            ペパチキ
            くぬたろう
            
            ■ 使用ツール・環境
            Android Studio
            Android SDK
            Kotlin
            
            ■ 主な使用API
            NotificationManager（おやすみモード制御）
            AlarmManager（スケジュール管理）
            SharedPreferences（データ保存）
            BroadcastReceiver（アラーム受信・再起動対応）
            
            ■ 対応OS
            Android 7.0 以上
        """.trimIndent()
}

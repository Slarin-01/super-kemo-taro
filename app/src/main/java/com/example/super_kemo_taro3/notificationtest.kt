package com.example.super_kemo_taro3

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// 旧クラス名を参照するコードでも、現在のActivityを使えるようにする別名。
typealias notificationtest = NotificationTestActivity

/** おやすみモードの手動切り替えを試す画面。モードの変更と表示用状態の更新を行う。 */
class NotificationTestActivity : AppCompatActivity() {
    private var status by mutableStateOf("通知モード：確認中")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        status = getDndStatus()
        setContent {
            AppScreen {
                NotificationTestScreen(status, onToggle = { status = toggleDnd() }, onBack = ::finish)
            }
        }
        checkNotificationPolicyAccess()
    }

    // 端末の権限設定などから戻った場合も、実際の通知モードを読み直す。
    override fun onResume() {
        super.onResume()
        status = getDndStatus()
    }
    /** おやすみモードを変更する権限がなければ、ユーザーが許可できる設定画面を開く。 */
    private fun checkNotificationPolicyAccess() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    /** 通知許可（ALL）とミュート（NONE）を切り替え、操作結果の表示文字列を返す。 */
    private fun toggleDnd(): String {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            return "おやすみモードへのアクセスが許可されていません。設定画面で許可してください。"
        }
        return if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            "通知モード: ON"
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            "通知モード: OFF"
        }
    }

    /** 現在の通知フィルターを読み、ミュート有効をON、全通知許可をOFFとして表示する。 */
    private fun getDndStatus(): String {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            return "通知モード：権限が無いので確認できません"
        }
        return if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            "通知モード：OFF"
        } else {
            "通知モード：ON"
        }
    }
}

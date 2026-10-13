/*
 * フロントエンド・状態管理: クレジットの開閉状態と表示本文。
 * クレジットの開閉と本文だけを担当し、画面への移動はSettingsMenuPageに任せる。
 */
package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// 1. クレジットの表示状態
/** showCreditsを保持し、openCredits・closeCreditsで切り替える。既存のクレジット本文をcreditMessageとして提供する。 */
class MenuViewModel(restoredShowCredits: Boolean = false) : ViewModel() {
    // 画面復元時はActivityから渡された開閉状態を使い、再描画にも同じ状態を渡す。
    var showCredits by mutableStateOf(restoredShowCredits)
        private set

    fun openCredits() { showCredits = true }
    fun closeCredits() { showCredits = false }

    // クレジットは固定の表示文。ツールや対応OSを変えた場合は、本文も実態に合わせて更新する。
    val creditMessage = """
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


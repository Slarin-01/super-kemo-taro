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

    fun openCredits() {
        showCredits = true
    }

    fun closeCredits() {
        showCredits = false
    }

    // クレジットは固定の表示文。対応OSや使用ライブラリを変えた場合は、本文も更新する。
    val creditMessage = """
            ■ アプリ名
            言十ベル
            
            ■ バージョン
            1.1
            
            ■ 作成グループ名
            スーパーKEMOたろう
            
            ■ メンバー(敬称略)
            кемо
            スラリン
            ペパチキ
            くぬたろう
            
            ■ テスト環境
            Samsung Galaxy A53 SC-53C 
            
            ■ 対応OS
            Android 7.0 以上

            ■ オープンソースライセンス
            本アプリは、以下のオープンソースライブラリを使用しています。

            AndroidX / Jetpack Compose
            Activity / Activity Compose / AppCompat / Core KTX
            Compose UI / Material 3 / UI Tooling
            Copyright The Android Open Source Project
            https://github.com/androidx/androidx

            Material Components for Android
            Copyright The Android Open Source Project
            https://github.com/material-components/material-components-android

            上記のライブラリは Apache License, Version 2.0 の条件で提供されています。
            https://www.apache.org/licenses/LICENSE-2.0
            ライセンス全文を以下に記載します。
        """.trimIndent()
}


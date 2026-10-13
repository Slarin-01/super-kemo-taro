/*
 * フロントエンド・状態管理: 通知切り替えテストの状態表示と操作結果。
 * 表示状態と未保存の入力を保持する。端末APIの直接操作を含めず、サービスや設定の契約へ依頼する。
 */
package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.super_kemo_taro3.backend.service.NotificationModeService

// 1. 通知テストの表示状態
/** NotificationModeServiceから現在の状態・切替結果を受け取り、statusの日本語表示を更新する。 */
class NotificationTestViewModel(private val notifications: NotificationModeService) : ViewModel() {
    var status by mutableStateOf("通知モード：確認中")
        private set

    fun refresh() {
        // trueはミュート、falseは解除、nullは許可不足で状態を確認できないことを表す。
        status = when (notifications.currentMuted()) {
            null -> "通知モード：権限が無いので確認できません"
            false -> "通知モード：OFF"
            true -> "通知モード：ON"
        }
    }

    fun hasAccess(): Boolean = notifications.hasAccess()

    /** 通知切り替えの結果を表示文へ変換し、許可画面を開く必要がある場合だけtrueを返す。 */
    fun toggle(): Boolean {
        val muted = notifications.toggle()
        status = when (muted) {
            null -> "おやすみモードへのアクセスが許可されていません。設定画面で許可してください。"
            true -> "通知モード: ON"
            false -> "通知モード: OFF"
        }
        return muted == null
    }
}


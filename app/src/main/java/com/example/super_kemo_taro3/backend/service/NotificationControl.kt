/*
 * バックエンド・処理: 通知操作の契約、Android API実装、権限確認とミュート状態の切り替え。
 * 同じ機能の定義・判断・端末操作をまとめる。画面描画やActivityへの遷移を含めない。
 */
package com.example.super_kemo_taro3.backend.service

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.Condition
import androidx.annotation.RequiresApi

// 1. 通知状態を取得・変更する契約
/** 通知ポリシーアクセスの可否、ミュート状態の取得、ミュート状態の変更を定義する。表示文字列や画面遷移は含めない。 */
interface NotificationGateway {
    fun hasPolicyAccess(): Boolean
    fun isMuted(): Boolean
    fun setMuted(muted: Boolean)
    fun isDeviceMuted(): Boolean = isMuted()
    fun setMutedByUser(muted: Boolean) = setMuted(muted)
}

// 2. AndroidのNotificationManagerを使う実装
/** Android 15以降では、端末全体とアプリ自身の通知ルールを区別して扱う。 */
class AndroidNotificationGateway(context: Context) : NotificationGateway {
    private val notifications = context.applicationContext
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun hasPolicyAccess(): Boolean = notifications.isNotificationPolicyAccessGranted

    override fun isMuted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return isDeviceMuted()
        val (id, rule) = appRule() ?: return false
        return rule.isEnabled &&
            rule.interruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL &&
            notifications.getAutomaticZenRuleState(id) == Condition.STATE_TRUE
    }

    override fun isDeviceMuted(): Boolean =
        notifications.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

    override fun setMuted(muted: Boolean) {
        notifications.setInterruptionFilter(
            if (muted) NotificationManager.INTERRUPTION_FILTER_NONE
            else NotificationManager.INTERRUPTION_FILTER_ALL
        )
    }

    override fun setMutedByUser(muted: Boolean) {
        setMuted(muted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val (id, rule) = appRule() ?: return
            // 本体で一時解除されたルールも、明示的なボタン操作なら再び切り替えられる。
            notifications.setAutomaticZenRuleState(id, Condition(
                rule.conditionId,
                if (muted) "ON" else "OFF",
                if (muted) Condition.STATE_TRUE else Condition.STATE_FALSE,
                Condition.SOURCE_USER_ACTION
            ))
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun appRule() = notifications.automaticZenRules.entries.firstOrNull { (_, rule) ->
        // setInterruptionFilterが作る自アプリの暗黙ルール。内部のルールIDには依存しない。
        rule.owner == null && rule.configurationActivity == null
    }
}

// 3. 権限を確認して通知操作を依頼
/** NotificationGatewayへ依頼する前に権限を確認する。currentMuted・toggleは権限不足をnullで伝え、setMutedは変更可否をBooleanで返す。 */
class NotificationModeService(private val gateway: NotificationGateway) {
    fun hasAccess(): Boolean = gateway.hasPolicyAccess()

    /** true/falseは現在の状態、nullは通知を扱う許可がなく状態を読めないことを表す。 */
    fun currentMuted(): Boolean? = if (hasAccess()) gateway.isMuted() else null

    fun currentDeviceMuted(): Boolean? = if (hasAccess()) gateway.isDeviceMuted() else null

    /** trueは変更依頼済み、falseは許可不足。希望するミュート状態そのものは引数で指定する。 */
    fun setMuted(muted: Boolean): Boolean {
        if (!hasAccess()) return false
        gateway.setMuted(muted)
        return true
    }

    /** 切り替え後の状態を返す。nullなら画面側で許可設定の案内が必要。 */
    fun toggle(): Boolean? {
        val current = currentMuted() ?: return null
        gateway.setMutedByUser(!current)
        // 依頼した値ではなく反映された状態を表示する。
        return currentMuted()
    }
}


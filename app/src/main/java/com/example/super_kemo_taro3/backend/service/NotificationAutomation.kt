/*
 * バックエンド・処理: 一時停止期限の管理と、アラーム受信後の通知変更・翌週予約の判断。
 * 同じ機能の定義・判断・端末操作をまとめる。画面描画やActivityへの遷移を含めない。
 */
package com.example.super_kemo_taro3.backend.service

import com.example.super_kemo_taro3.backend.repository.SettingsRepository

// 1. 日曜日末までの一時停止の管理
/** 保存された停止期限と時計から停止中か判定する。停止有効時は日曜日末を保存し、権限があれば通知許可側へ変更する。 */
class PauseService(
    private val settings: SettingsRepository,
    private val notifications: NotificationModeService,
    private val now: () -> Long = System::currentTimeMillis
) {
    // 期限が過ぎた設定値は残っていても停止解除として扱う。
    fun isPaused(): Boolean = now() < settings.readPauseUntil()

    // 予約そのものは取り消さず、受信時にこの期限を見て通知変更だけを止める。
    fun toggle() {
        if (isPaused()) {
            settings.writePauseUntil(null)
        } else {
            settings.writePauseUntil(ScheduleCalculator.sundayEnd(now()))
            notifications.setMuted(false)
        }
    }
}

// 2. 受信後の通知変更と次週予約の判断
/** ON・OFF actionを判断し、通知権限と停止期限を確認して通知モードを変更する。正確なアラームの権限があれば次回を予約する。 */
class AlarmDeliveryService(
    private val notifications: NotificationModeService,
    private val pause: PauseService,
    private val alarms: AlarmScheduler
) {
    fun handle(action: String?, requestCode: Int, scheduledTime: Long) {
        val mute = when (action) {
            AlarmContract.ACTION_MUTE -> true
            AlarmContract.ACTION_UNMUTE -> false
            else -> return
        }
        // 通知の許可がない場合は、この受信を終了する。翌週の再予約も行わない現在の仕様。
        if (!notifications.hasAccess()) return
        // 一時停止中は通知の変更を抑止するが、翌週の予定は継続する。
        if (!pause.isPaused()) notifications.setMuted(mute)
        // 予約の許可だけがない場合、今回の通知変更は行い、翌週の登録を見送る。
        if (alarms.canScheduleExactAlarms()) {
            alarms.scheduleNext(requireNotNull(action), requestCode, scheduledTime)
        }
    }
}


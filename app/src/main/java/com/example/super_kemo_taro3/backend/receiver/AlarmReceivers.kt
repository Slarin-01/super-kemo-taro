/*
 * バックエンド・受信処理: アラーム、端末再起動、アプリ更新の受信入口。判断処理をサービスへ委譲。
 * 受信する種類と値を確認してサービスへ渡す。通知変更・停止判定・再予約の判断をここに置かない。
 */
package com.example.super_kemo_taro3.backend.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.backend.service.AlarmContract

// 1. 通知アラームの受信入口
/** アラームの受信入口。通知を変えるかどうかはAlarmDeliveryServiceへ判断を渡す。 */
class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DND", "onReceive: ${intent.action}")
        // 予約の種類・識別番号・予定日時を渡す。翌週の計算は実際の受信時刻ではなく予定日時を基準にする。
        context.appServices.alarmDelivery.handle(
            intent.action,
            intent.getIntExtra(AlarmContract.EXTRA_REQUEST_CODE, 0),
            intent.getLongExtra(AlarmContract.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        )
    }
}

// 2. 再起動・アプリ更新の受信入口
/** 端末再起動・アプリ更新後の受信入口。保存された選択枠からの予約復元をTimetableServiceへ依頼する。 */
class AlarmRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                if (!context.appServices.timetable.restoreAfterBoot()) {
                    Log.d("DND", "正確なアラームの権限なし")
                }
            }
        }
    }
}


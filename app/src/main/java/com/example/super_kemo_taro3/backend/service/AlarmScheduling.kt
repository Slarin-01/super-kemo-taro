/*
 * バックエンド・処理: アラームの識別子、操作契約、Android APIによる予約・取消・翌週予約。
 * 同じ機能の定義・判断・端末操作をまとめる。画面描画やActivityへの遷移を含めない。
 */
package com.example.super_kemo_taro3.backend.service

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.super_kemo_taro3.backend.receiver.NotificationAlarmReceiver

// 1. 受信action・extra・予約識別子
/** 既存のDND_ON・DND_OFF action、requestCode・scheduledTimeのextra名、曜日・時限からの識別子を集約する。 */
object AlarmContract {
    const val ACTION_MUTE = "com.example.super_kemo_taro3.DND_ON"
    const val ACTION_UNMUTE = "com.example.super_kemo_taro3.DND_OFF"
    const val EXTRA_REQUEST_CODE = "requestCode"
    const val EXTRA_SCHEDULED_TIME = "scheduledTime"

    // 枠の保存キーから同じ番号を再現し、開始・終了を別々に予約・取消できるようにする。
    fun requestCode(slotKey: String, mute: Boolean): Int =
        slotKey.hashCode() + if (mute) 0 else 1000
}

// 2. アラーム操作の契約
/** 権限確認、1枠の登録・取消、旧受信先の取消、次週予約を定義する。バックエンドの判断処理とAndroid APIを分離する。 */
interface AlarmScheduler {
    fun canScheduleExactAlarms(): Boolean
    fun canRestoreAfterBoot(): Boolean
    fun scheduleSlot(slot: TimetableSlot, start: LessonTime, end: LessonTime)
    fun cancelSlot(slotKey: String)
    fun cancelLegacySlot(slotKey: String)
    fun scheduleNext(action: String, requestCode: Int, scheduledTime: Long)
}

// 3. AndroidのAlarmManagerを使う実装
/** AlarmSchedulerをAlarmManagerとPendingIntentで実装する。受信先はNotificationAlarmReceiver。日時計算はScheduleCalculatorへ委譲する。 */
class AndroidAlarmScheduler(context: Context) : AlarmScheduler {
    private val appContext = context.applicationContext
    private val alarms = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()

    override fun canRestoreAfterBoot(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val hasExactPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            appContext.checkSelfPermission(Manifest.permission.USE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED
        return hasExactPermission || alarms.canScheduleExactAlarms()
    }

    override fun scheduleSlot(slot: TimetableSlot, start: LessonTime, end: LessonTime) {
        val now = System.currentTimeMillis()
        // 開始と終了それぞれの次回日時を求める。授業途中の登録では開始だけが翌週になる場合がある。
        val startTime = ScheduleCalculator.nextOccurrence(slot.calendarDay, start, now)
        val endTime = ScheduleCalculator.nextOccurrence(slot.calendarDay, end, now)
        register(AlarmContract.ACTION_MUTE, AlarmContract.requestCode(slot.key, true), startTime)
        register(AlarmContract.ACTION_UNMUTE, AlarmContract.requestCode(slot.key, false), endTime)
        Log.d("DND", "アラーム登録: ${slot.key} ON→${java.util.Date(startTime)} OFF→${java.util.Date(endTime)}")
    }

    override fun cancelSlot(slotKey: String) = cancel(slotKey, legacy = false)

    override fun cancelLegacySlot(slotKey: String) = cancel(slotKey, legacy = true)

    // NO_CREATEで登録済みの予約だけを取得し、解除操作で予約を新規作成しない。
    private fun cancel(slotKey: String, legacy: Boolean) {
        for (mute in listOf(true, false)) {
            val action = if (mute) AlarmContract.ACTION_MUTE else AlarmContract.ACTION_UNMUTE
            val intent = receiverIntent(action, legacy)
            val pending = PendingIntent.getBroadcast(
                appContext, AlarmContract.requestCode(slotKey, mute), intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) ?: continue
            alarms.cancel(pending)
            pending.cancel()
        }
    }

    // 実際の受信日時ではなく予定日時の7日後を使い、受信遅延の累積を避ける。
    override fun scheduleNext(action: String, requestCode: Int, scheduledTime: Long) {
        register(action, requestCode, scheduledTime + ScheduleCalculator.WEEK_IN_MILLIS)
    }

    private fun register(action: String, requestCode: Int, time: Long) {
        val intent = receiverIntent(action).apply {
            putExtra(AlarmContract.EXTRA_REQUEST_CODE, requestCode)
            putExtra(AlarmContract.EXTRA_SCHEDULED_TIME, time)
        }
        // 同じ受信先・action・番号の予約は再利用し、予定日時などの付加情報を更新する。
        val pending = PendingIntent.getBroadcast(
            appContext, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 呼び出し元が予約権限を確認済みであることを前提に、端末の待機中も届く予約を依頼する。
        alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
    }

    private fun receiverIntent(action: String, legacy: Boolean = false): Intent {
        val intent = if (legacy) Intent().setComponent(
            // 旧Receiver名は予約取消のためだけに参照する。旧クラスの実装は残さない。
            ComponentName(appContext.packageName, "com.example.super_kemo_taro3.DndReceiver")
        ) else Intent(appContext, NotificationAlarmReceiver::class.java)
        return intent.setAction(action)
    }
}


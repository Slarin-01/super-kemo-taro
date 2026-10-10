package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/** アラームを受け取り、通知モードを変更したあと、次回のアラームを登録する。 */
class DndReceiver : BroadcastReceiver() {
    companion object {
        // 時限開始はON（ミュート）、時限終了はOFF（全通知許可）として区別する。
        const val ACTION_DND_ON = "com.example.super_kemo_taro3.DND_ON"
        const val ACTION_DND_OFF = "com.example.super_kemo_taro3.DND_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DND", "onReceive: ${intent.action}")
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // 通知モードの変更権限がない場合は、切り替えも次回予約も行わない。
        if (!nm.isNotificationPolicyAccessGranted) return

        // ホーム画面で保存した一時停止の期限を、アラームを受け取るたびに確認する。
        val prefs = context.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        val pauseUntil = prefs.getLong("pause_until_sunday_time", 0L)
        val isPaused = System.currentTimeMillis() < pauseUntil

        when (intent.action) {
            ACTION_DND_ON -> {
                if (!isPaused) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                } else {
                    Log.d("DND", "日曜日まで停止中のためDND_ONをスキップ")
                }
                // 一時停止中も翌週分は登録し、停止期限を過ぎた後に予定を継続できるようにする。
                reschedule(context, intent, ACTION_DND_ON)
            }

            ACTION_DND_OFF -> {
                if (!isPaused) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                // 来週の同じ時刻に再登録
                reschedule(context, intent, ACTION_DND_OFF)
            }
        }
    }

    /** 開始・終了それぞれの識別子を引き継ぎ、保存された予定時刻の7日後を予約する。 */
    private fun reschedule(context: Context, intent: Intent, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        // 初回の登録時に渡された識別子を使い、同じ枠のアラームを更新する。
        val requestCode = intent.getIntExtra("requestCode", 0)

        // 本来の発火時刻を取得（なければ現在時刻）
        val scheduledTime = intent.getLongExtra("scheduledTime", System.currentTimeMillis())

        // 実際の受信時刻ではなく本来の予定時刻に7日分を加え、受信遅延の累積を防ぐ。
        val nextTime = scheduledTime + AlarmManager.INTERVAL_DAY * 7

        val newIntent = Intent(context, DndReceiver::class.java).apply {
            this.action = action
        }
        newIntent.putExtra("requestCode", requestCode)
        newIntent.putExtra("scheduledTime", nextTime)  // 次回の予定時刻を渡す

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTime,
            pendingIntent
        )

        Log.d("DND", "来週に再登録: $action → ${java.util.Date(nextTime)}")
    }
}

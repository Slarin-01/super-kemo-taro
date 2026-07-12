package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DndReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DND_ON  = "com.example.super_kemo_taro3.DND_ON"
        const val ACTION_DND_OFF = "com.example.super_kemo_taro3.DND_OFF"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DND", "onReceive: ${intent.action}")
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) return

        when (intent.action) {
            ACTION_DND_ON -> {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                // 来週の同じ時刻に再登録
                reschedule(context, intent, ACTION_DND_ON)
            }
            ACTION_DND_OFF -> {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                // 来週の同じ時刻に再登録
                reschedule(context, intent, ACTION_DND_OFF)
            }
        }
    }

    private fun reschedule(context: Context, intent: Intent, action: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return
        }

        // 来週の同じ時刻（7日後）
        val nextTime = System.currentTimeMillis() + AlarmManager.INTERVAL_DAY * 7

        val requestCode = intent.getIntExtra("requestCode", 0)

        val newIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, DndReceiver::class.java).apply {
                this.action = action
                putExtra("requestCode", requestCode)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTime,
            newIntent
        )

        Log.d("DND", "来週に再登録: $action → ${java.util.Date(nextTime)}")
    }
}
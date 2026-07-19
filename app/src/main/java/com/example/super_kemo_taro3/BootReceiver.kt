package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    private val slotTimes = mapOf(
        1 to Pair(Pair(8, 40),  Pair(9,  30)),
        2 to Pair(Pair(9, 40),  Pair(10, 30)),
        3 to Pair(Pair(10, 45), Pair(11, 35)),
        4 to Pair(Pair(11, 45), Pair(12, 35)),
        5 to Pair(Pair(13, 25), Pair(14, 15)),
        6 to Pair(Pair(14, 25), Pair(15, 15)),
        7 to Pair(Pair(15, 30), Pair(16, 20)),
        8 to Pair(Pair(16, 30), Pair(17, 20))
    )

    private val dayMap = mapOf(
        "MON" to Calendar.MONDAY,
        "TUE" to Calendar.TUESDAY,
        "WED" to Calendar.WEDNESDAY,
        "THU" to Calendar.THURSDAY,
        "FRI" to Calendar.FRIDAY
    )

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d("DND", "再起動検知 → アラーム再登録開始")

        // 保存済みのblockedSlotsを読み込む
        val prefs = context.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        val savedSlots = prefs.getStringSet("blocked_slots", emptySet()) ?: emptySet()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d("DND", "正確なアラームの権限なし")
                return
            }
        }

        for (slotKey in savedSlots) {
            val parts = slotKey.split("_")
            if (parts.size != 2) continue
            val day = parts[0]
            val slot = parts[1].toIntOrNull() ?: continue

            val calDay = dayMap[day] ?: continue
            val times = slotTimes[slot] ?: continue

            // 開始時刻（ミュートON）
            val startCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calDay)
                set(Calendar.HOUR_OF_DAY, times.first.first)
                set(Calendar.MINUTE, times.first.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val onIntent = Intent(context, DndReceiver::class.java).apply {
                action = DndReceiver.ACTION_DND_ON
            }
            onIntent.putExtra("requestCode", slotKey.hashCode())
            onIntent.putExtra("scheduledTime", startCal.timeInMillis)
            val onPending = PendingIntent.getBroadcast(
                context,
                slotKey.hashCode(),
                onIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                startCal.timeInMillis,
                onPending
            )

            // 終了時刻（ミュートOFF）
            val endCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calDay)
                set(Calendar.HOUR_OF_DAY, times.second.first)
                set(Calendar.MINUTE, times.second.second)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val offIntent = Intent(context, DndReceiver::class.java).apply {
                action = DndReceiver.ACTION_DND_OFF
            }
            offIntent.putExtra("requestCode", slotKey.hashCode() + 1000)
            offIntent.putExtra("scheduledTime", endCal.timeInMillis)
            val offPending = PendingIntent.getBroadcast(
                context,
                slotKey.hashCode() + 1000,
                offIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endCal.timeInMillis,
                offPending
            )

            Log.d("DND", "再登録: $slotKey ON→${java.util.Date(startCal.timeInMillis)} OFF→${java.util.Date(endCal.timeInMillis)}")
        }
    }
}
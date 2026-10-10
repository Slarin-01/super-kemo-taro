package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

/** 端末再起動で失われたアラームを、保存済みの時間割と時刻から復元する。 */
class BootReceiver : BroadcastReceiver() {

    // 時刻が未保存の時限に使う既定の開始・終了時刻。
    private val slotTimes = mapOf(
        1 to Pair(Pair(8, 40), Pair(9, 30)),
        2 to Pair(Pair(9, 40), Pair(10, 30)),
        3 to Pair(Pair(10, 45), Pair(11, 35)),
        4 to Pair(Pair(11, 45), Pair(12, 35)),
        5 to Pair(Pair(13, 25), Pair(14, 15)),
        6 to Pair(Pair(14, 25), Pair(15, 15)),
        7 to Pair(Pair(15, 30), Pair(16, 20)),
        8 to Pair(Pair(16, 30), Pair(17, 20))
    )

    // 時間割の曜日キーを、アラーム日時の計算に使うCalendarの曜日定数へ変換する。
    private val dayMap = mapOf(
        "MON" to Calendar.MONDAY,
        "TUE" to Calendar.TUESDAY,
        "WED" to Calendar.WEDNESDAY,
        "THU" to Calendar.THURSDAY,
        "FRI" to Calendar.FRIDAY
    )

    override fun onReceive(context: Context, intent: Intent) {
        // このReceiverでは起動完了の通知だけを処理する。
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d("DND", "再起動検知 → アラーム再登録開始")

        // 選択された枠とカスタム時刻は別々の保存領域から読み込む。
        val timetablePrefs =
            context.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        val timePrefs = context.getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)
        val savedSlots = timetablePrefs.getStringSet("blocked_slots", emptySet()) ?: emptySet()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 正確なアラームを利用できない場合は、復元処理を実行せず終了する。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val hasExact = context.checkSelfPermission(
                android.Manifest.permission.USE_EXACT_ALARM
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasExact && !alarmManager.canScheduleExactAlarms()) {
                Log.d("DND", "正確なアラームの権限なし")
                return
            }
        }

        // MON_1などの各キーから曜日と時限を取り出し、不正なキーは読み飛ばす。
        for (slotKey in savedSlots) {
            val parts = slotKey.split("_")
            if (parts.size != 2) continue
            val day = parts[0]
            val slot = parts[1].toIntOrNull() ?: continue

            val calDay = dayMap[day] ?: continue
            val defaultTimes = slotTimes[slot] ?: continue

            // SharedPreferencesから時刻を取得（なければ固定値を使う）
            val startStr = timePrefs.getString("time_start_$slot", null)
            val endStr = timePrefs.getString("time_end_$slot", null)

            val startHour: Int
            val startMin: Int
            val endHour: Int
            val endMin: Int

            if (startStr != null && startStr.contains(":")) {
                val p = startStr.split(":")
                startHour = p[0].toIntOrNull() ?: defaultTimes.first.first
                startMin = p[1].toIntOrNull() ?: defaultTimes.first.second
            } else {
                startHour = defaultTimes.first.first
                startMin = defaultTimes.first.second
            }

            if (endStr != null && endStr.contains(":")) {
                val p = endStr.split(":")
                endHour = p[0].toIntOrNull() ?: defaultTimes.second.first
                endMin = p[1].toIntOrNull() ?: defaultTimes.second.second
            } else {
                endHour = defaultTimes.second.first
                endMin = defaultTimes.second.second
            }

            // 開始時刻（ミュートON）。今週の予定が過ぎていれば翌週に進める。
            val startCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calDay)
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMin)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            // 通常の選択画面と同じ識別子・actionで、開始と終了のアラームを復元する。
            val onIntent = Intent(context, DndReceiver::class.java).apply {
                action = DndReceiver.ACTION_DND_ON
            }
            onIntent.putExtra("requestCode", slotKey.hashCode())
            onIntent.putExtra("scheduledTime", startCal.timeInMillis)
            val onPending = PendingIntent.getBroadcast(
                context, slotKey.hashCode(), onIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, startCal.timeInMillis, onPending
            )

            // 終了時刻（ミュートOFF）
            val endCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calDay)
                set(Calendar.HOUR_OF_DAY, endHour)
                set(Calendar.MINUTE, endMin)
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
                context, slotKey.hashCode() + 1000, offIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, endCal.timeInMillis, offPending
            )

            Log.d(
                "DND",
                "再登録: $slotKey ON→${java.util.Date(startCal.timeInMillis)} OFF→${
                    java.util.Date(endCal.timeInMillis)
                }"
            )
        }
    }
}

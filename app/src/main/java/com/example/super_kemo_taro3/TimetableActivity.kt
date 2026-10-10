package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar

/** ミュートする曜日・時限を選ぶ画面。選択時に保存とアラーム登録・取消を行う。 */
class TimetableActivity : AppCompatActivity() {

    // 時刻がまだカスタム保存されていない時限で使う、開始・終了の既定値。
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

    // MON〜FRIという保存用キーを、アラーム日時の計算に使う曜日定数へ対応付ける。
    private val dayMap = mapOf(
        "MON" to Calendar.MONDAY,
        "TUE" to Calendar.TUESDAY,
        "WED" to Calendar.WEDNESDAY,
        "THU" to Calendar.THURSDAY,
        "FRI" to Calendar.FRIDAY
    )

    // 「MON_1＝月曜1限」のようなキーの集合。集合を更新するとComposeの選択色も変わる。
    private var blockedSlots by mutableStateOf<Set<String>>(emptySet())
    private val preferences by lazy { getSharedPreferences("timetable_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loadBlockedSlots()
        setContent {
            AppScreen {
                TimetableScreen(blockedSlots, ::toggleSlot, ::finish)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadBlockedSlots()
    }

    /** 保存されたミュート対象を読み込む。toSetで保存データとは別の集合として扱う。 */
    private fun loadBlockedSlots() {
        blockedSlots = preferences.getStringSet("blocked_slots", emptySet())?.toSet() ?: emptySet()
    }

    /** タップした枠の選択を切り替え、アラームを更新したうえで選択状態を保存する。 */
    private fun toggleSlot(slotKey: String) {
        if (slotKey in blockedSlots) {
            blockedSlots = blockedSlots - slotKey
            cancelSlot(slotKey)
        } else {
            blockedSlots = blockedSlots + slotKey
            scheduleSlot(slotKey)
        }
        preferences.edit().putStringSet("blocked_slots", blockedSlots).apply()
    }

    /** 1つの曜日・時限について、開始時のミュートと終了時の解除を予約する。 */
    private fun scheduleSlot(slotKey: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Android 12以降では、指定時刻に正確に実行するアラームの権限も確認する。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        // MON_1のようなキーから曜日と時限番号を取り出し、曜日定数へ変換する。
        val parts = slotKey.split("_")
        val day = parts[0]
        val slot = parts[1].toInt()

        val calDay = dayMap[day] ?: return

        val sharedPref = getSharedPreferences("timetable_prefs", MODE_PRIVATE)
        val defaultTimes = slotTimes[slot] ?: return

        // カスタム保存された時刻を優先し、未設定・読み取れない数値は既定値で補う。
        val startStr = sharedPref.getString("time_start_$slot", null)
        val endStr = sharedPref.getString("time_end_$slot", null)

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

        // 今週の指定曜日・開始時刻を求め、過去になっている場合は翌週に進める。
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

        // アラーム発火時にDndReceiverへミュート開始を通知する。
        val onIntentBase = Intent(this, DndReceiver::class.java).apply {
            action = DndReceiver.ACTION_DND_ON
        }
        // 曜日・時限から作る識別子と予定時刻を渡し、Receiverが翌週分を登録できるようにする。
        onIntentBase.putExtra("requestCode", slotKey.hashCode())
        onIntentBase.putExtra("scheduledTime", startCal.timeInMillis)
        val onIntent = PendingIntent.getBroadcast(
            this, slotKey.hashCode(), onIntentBase,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startCal.timeInMillis,
            onIntent
        )

        // 終了時刻には解除を予約する。開始用とは別のactionとコードを使う。
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

        val offIntentBase = Intent(this, DndReceiver::class.java).apply {
            action = DndReceiver.ACTION_DND_OFF
        }
        offIntentBase.putExtra("requestCode", slotKey.hashCode() + 1000)
        offIntentBase.putExtra("scheduledTime", endCal.timeInMillis)
        val offIntent = PendingIntent.getBroadcast(
            this, slotKey.hashCode() + 1000, offIntentBase,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endCal.timeInMillis,
            offIntent
        )

        Log.d(
            "DND",
            "アラーム登録: $slotKey ON→${java.util.Date(startCal.timeInMillis)} OFF→${
                java.util.Date(endCal.timeInMillis)
            }"
        )
    }

    /** 登録時と同じrequestCode・actionのPendingIntentを指定し、開始・終了の両予約を取り消す。 */
    private fun cancelSlot(slotKey: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val onIntent = PendingIntent.getBroadcast(
            this, slotKey.hashCode(),
            Intent(this, DndReceiver::class.java).apply { action = DndReceiver.ACTION_DND_ON },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(onIntent)

        val offIntent = PendingIntent.getBroadcast(
            this, slotKey.hashCode() + 1000,
            Intent(this, DndReceiver::class.java).apply { action = DndReceiver.ACTION_DND_OFF },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(offIntent)
    }
}

package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
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

/** 時限カスタム画面。編集中の時刻を管理し、保存時にミュートの予定を登録し直す。 */
class MainActivity2 : AppCompatActivity() {
    // 開始・終了時刻を交互に保持する。保存ボタンを押すまではSharedPreferencesを書き換えない。
    private var times by mutableStateOf<List<String>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPref = getSharedPreferences("timetable_prefs", MODE_PRIVATE)
        // 画面回転などの復元値を優先し、なければ保存済み時刻、さらに未設定なら初期値を使う。
        times = savedInstanceState?.getStringArrayList("editing_times")
            ?.takeIf { it.size == timeSettings.size }
            ?: timeSettings.map { sharedPref.getString(it.saveKey, null) ?: it.defaultTime }
        setContent {
            AppScreen {
                TimeSettingsScreen(
                    times = times,
                    onTimeChange = { index, value ->
                        // 新しいリストを状態に代入し、Composeに入力値とエラー表示の更新を知らせる。
                        times = times.toMutableList().also { it[index] = value }
                    },
                    onSave = {
                        if (invalidTimeIndices(times).isEmpty()) {
                            saveAllTimes()
                            returnToMain()
                        }
                    },
                    onReset = { times = timeSettings.map { it.defaultTime } },
                    onDiscard = ::returnToMain
                )
            }
        }
    }

    // 保存前の編集内容も、Activityが再作成されたときに復元できるようにする。
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList("editing_times", ArrayList(times))
        super.onSaveInstanceState(outState)
    }

    private fun returnToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /** 16個の時刻を従来の保存キーで保存し、新しい時刻に合わせてアラームを更新する。 */
    private fun saveAllTimes() {
        val editor = getSharedPreferences("timetable_prefs", MODE_PRIVATE).edit()
        timeSettings.forEachIndexed { index, setting ->
            editor.putString(
                setting.saveKey,
                times[index]
            )
        }
        editor.apply()
        reschedulAllAlarms()
    }

    /** ミュート対象として選択済みの全枠について、開始・終了のアラームを登録し直す。 */
    private fun reschedulAllAlarms() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        val sharedPref = getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)
        val timetablePrefs = getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        val blockedSlots = timetablePrefs.getStringSet("blocked_slots", emptySet()) ?: emptySet()

        // 保存された曜日の英字キーを、Calendarが扱う曜日定数に変換する。
        val dayMap = mapOf(
            "MON" to Calendar.MONDAY,
            "TUE" to Calendar.TUESDAY,
            "WED" to Calendar.WEDNESDAY,
            "THU" to Calendar.THURSDAY,
            "FRI" to Calendar.FRIDAY
        )

        for (slotKey in blockedSlots) {
            // 例: MON_1を「月曜日」と「1限」に分解。不正な保存データは読み飛ばす。
            val parts = slotKey.split("_")
            if (parts.size != 2) continue
            val day = parts[0]
            val slot = parts[1].toIntOrNull() ?: continue
            val calDay = dayMap[day] ?: continue

            val startStr = sharedPref.getString("time_start_$slot", null) ?: continue
            val endStr = sharedPref.getString("time_end_$slot", null) ?: continue

            val startParts = startStr.split(":")
            val endParts = endStr.split(":")
            if (startParts.size != 2 || endParts.size != 2) continue

            val startHour = startParts[0].toIntOrNull() ?: continue
            val startMin = startParts[1].toIntOrNull() ?: continue
            val endHour = endParts[0].toIntOrNull() ?: continue
            val endMin = endParts[1].toIntOrNull() ?: continue

            // 指定曜日の開始時刻を求める。すでに過ぎていたら翌週に登録する。
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

            val onIntent = Intent(this, DndReceiver::class.java).apply {
                action = DndReceiver.ACTION_DND_ON
            }
            // 同じ曜日・時限・actionのPendingIntentを更新し、Receiverにも識別子と予定時刻を渡す。
            onIntent.putExtra("requestCode", slotKey.hashCode())
            onIntent.putExtra("scheduledTime", startCal.timeInMillis)
            val onPending = PendingIntent.getBroadcast(
                this, slotKey.hashCode(), onIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                startCal.timeInMillis,
                onPending
            )

            // 終了時刻にはミュート解除を予約する。開始と同様、過去の時刻なら翌週にずらす。
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

            val offIntent = Intent(this, DndReceiver::class.java).apply {
                action = DndReceiver.ACTION_DND_OFF
            }
            // 開始用コードに1000を加え、終了用のアラームとして区別する。
            offIntent.putExtra("requestCode", slotKey.hashCode() + 1000)
            offIntent.putExtra("scheduledTime", endCal.timeInMillis)
            val offPending = PendingIntent.getBroadcast(
                this, slotKey.hashCode() + 1000, offIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endCal.timeInMillis,
                offPending
            )

            Log.d(
                "DND",
                "時刻変更後再登録: $slotKey ON→${java.util.Date(startCal.timeInMillis)} OFF→${
                    java.util.Date(endCal.timeInMillis)
                }"
            )
        }
    }
}

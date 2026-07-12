package com.example.super_kemo_taro3

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

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

    private val blockedSlots = mutableSetOf<String>()

    private val preferences by lazy {
        getSharedPreferences("timetable_settings", MODE_PRIVATE)
    }

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authchk()
        loadBlockedSlots()
        setupTimetableButtons()

        tvStatus = findViewById(R.id.tvStatus)
        tvStatus.text = getDndStatus()

        findViewById<Button>(R.id.btnToggleDnd).setOnClickListener {
            tvStatus.text = toggleDnd()
        }
    }

    private fun saveBlockedSlots() {
        preferences.edit()
            .putStringSet("blocked_slots", blockedSlots.toSet())
            .apply()
    }

    private fun loadBlockedSlots() {
        val savedSlots =
            preferences.getStringSet("blocked_slots", emptySet()) ?: emptySet()
        blockedSlots.clear()
        blockedSlots.addAll(savedSlots)
    }

    private fun toggleDnd(): String {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            return "おやすみモードへのアクセスが許可されていません。設定画面で許可してください。"
        }
        return if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            "おやすみモード: ON"
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            "おやすみモード: OFF"
        }
    }

    private fun getDndStatus(): String {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            return "おやすみモード：権限が無いので確認できません"
        }
        return if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            "おやすみモード：OFF"
        } else {
            "おやすみモード：ON"
        }
    }

    private fun authchk() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }
    }

    private fun setupTimetableButtons() {
        bindTimetableButton(R.id.btn_mon_1, "MON_1")
        bindTimetableButton(R.id.btn_mon_2, "MON_2")
        bindTimetableButton(R.id.btn_mon_3, "MON_3")
        bindTimetableButton(R.id.btn_mon_4, "MON_4")
        bindTimetableButton(R.id.btn_mon_5, "MON_5")
        bindTimetableButton(R.id.btn_mon_6, "MON_6")
        bindTimetableButton(R.id.btn_mon_7, "MON_7")
        bindTimetableButton(R.id.btn_mon_8, "MON_8")
        bindTimetableButton(R.id.btn_tue_1, "TUE_1")
        bindTimetableButton(R.id.btn_tue_2, "TUE_2")
        bindTimetableButton(R.id.btn_tue_3, "TUE_3")
        bindTimetableButton(R.id.btn_tue_4, "TUE_4")
        bindTimetableButton(R.id.btn_tue_5, "TUE_5")
        bindTimetableButton(R.id.btn_tue_6, "TUE_6")
        bindTimetableButton(R.id.btn_tue_7, "TUE_7")
        bindTimetableButton(R.id.btn_tue_8, "TUE_8")
        bindTimetableButton(R.id.btn_wed_1, "WED_1")
        bindTimetableButton(R.id.btn_wed_2, "WED_2")
        bindTimetableButton(R.id.btn_wed_3, "WED_3")
        bindTimetableButton(R.id.btn_wed_4, "WED_4")
        bindTimetableButton(R.id.btn_wed_5, "WED_5")
        bindTimetableButton(R.id.btn_wed_6, "WED_6")
        bindTimetableButton(R.id.btn_wed_7, "WED_7")
        bindTimetableButton(R.id.btn_wed_8, "WED_8")
        bindTimetableButton(R.id.btn_thu_1, "THU_1")
        bindTimetableButton(R.id.btn_thu_2, "THU_2")
        bindTimetableButton(R.id.btn_thu_3, "THU_3")
        bindTimetableButton(R.id.btn_thu_4, "THU_4")
        bindTimetableButton(R.id.btn_thu_5, "THU_5")
        bindTimetableButton(R.id.btn_thu_6, "THU_6")
        bindTimetableButton(R.id.btn_thu_7, "THU_7")
        bindTimetableButton(R.id.btn_thu_8, "THU_8")
        bindTimetableButton(R.id.btn_fri_1, "FRI_1")
        bindTimetableButton(R.id.btn_fri_2, "FRI_2")
        bindTimetableButton(R.id.btn_fri_3, "FRI_3")
        bindTimetableButton(R.id.btn_fri_4, "FRI_4")
        bindTimetableButton(R.id.btn_fri_5, "FRI_5")
        bindTimetableButton(R.id.btn_fri_6, "FRI_6")
        bindTimetableButton(R.id.btn_fri_7, "FRI_7")
        bindTimetableButton(R.id.btn_fri_8, "FRI_8")
    }

    private fun bindTimetableButton(buttonId: Int, slotKey: String) {
        val button = findViewById<Button>(buttonId)

        if (slotKey in blockedSlots) {
            button.setBackgroundColor(android.graphics.Color.GREEN)
        } else {
            button.setBackgroundColor(android.graphics.Color.LTGRAY)
        }

        button.setOnClickListener {
            if (slotKey in blockedSlots) {
                blockedSlots.remove(slotKey)
                button.setBackgroundColor(android.graphics.Color.LTGRAY)
                cancelSlot(slotKey)
            } else {
                blockedSlots.add(slotKey)
                button.setBackgroundColor(android.graphics.Color.GREEN)
                scheduleSlot(slotKey)
            }
            saveBlockedSlots()
        }
    }

    private fun scheduleSlot(slotKey: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        val parts = slotKey.split("_")
        val day = parts[0]
        val slot = parts[1].toInt()

        val calDay = dayMap[day] ?: return
        val times = slotTimes[slot] ?: return

        // 開始時刻（ミュートON）
        val startCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, calDay)
            set(Calendar.HOUR_OF_DAY, times.first.first)
            set(Calendar.MINUTE, times.first.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)  // 過去なら来週にする
            }
        }

        val onIntent = PendingIntent.getBroadcast(
            this,
            slotKey.hashCode(),
            Intent(this, DndReceiver::class.java).apply { action = DndReceiver.ACTION_DND_ON },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            startCal.timeInMillis,
            onIntent
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

        val offIntent = PendingIntent.getBroadcast(
            this,
            slotKey.hashCode() + 1000,
            Intent(this, DndReceiver::class.java).apply { action = DndReceiver.ACTION_DND_OFF },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endCal.timeInMillis,
            offIntent
        )

        Log.d("DND", "アラーム登録: $slotKey ON→${java.util.Date(startCal.timeInMillis)} OFF→${java.util.Date(endCal.timeInMillis)}")
    }

    private fun cancelSlot(slotKey: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val onIntent = PendingIntent.getBroadcast(
            this,
            slotKey.hashCode(),
            Intent(this, DndReceiver::class.java).apply { action = DndReceiver.ACTION_DND_ON },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(onIntent)

        val offIntent = PendingIntent.getBroadcast(
            this,
            slotKey.hashCode() + 1000,
            Intent(this, DndReceiver::class.java).apply { action = DndReceiver.ACTION_DND_OFF },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(offIntent)
    }
}
package com.example.super_kemo_taro3

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar


class MainActivity : AppCompatActivity() {

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

        //シーン変更ボタン
        findViewById<Button>(R.id.btn_edit).setOnClickListener {
            startActivity(Intent(this, MainActivity3::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadBlockedSlots()
        setupTimetableButtons()
        tvStatus.text = getDndStatus()
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
            "通知モード: ON"
        } else {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            "通知モード: OFF"
        }
    }

    private fun getDndStatus(): String {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            return "通知モード：権限が無いので確認できません"
        }
        return if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            "通知モード：OFF"
        } else {
            "通知モード：ON"
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

        // 色だけ反映・タップ不可
        if (slotKey in blockedSlots) {
            button.setBackgroundColor(android.graphics.Color.GREEN)
        } else {
            button.setBackgroundColor(android.graphics.Color.LTGRAY)
        }
        button.isClickable = false
    }

}

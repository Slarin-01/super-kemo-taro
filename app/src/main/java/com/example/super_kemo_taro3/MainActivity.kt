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

class MainActivity : AppCompatActivity() {

    private val blockedSlots = mutableSetOf<String>()

    private val preferences by lazy {
        getSharedPreferences("timetable_settings", MODE_PRIVATE)
    }

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        loadBlockedSlots()
        setupTimetableButtons()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        authchk()
        tvStatus = findViewById(R.id.tvStatus)
        tvStatus.text = getDndStatus()

        findViewById<Button>(R.id.btnToggleDnd).setOnClickListener {
            tvStatus.text = toggleDnd()
        }

        //シーン変更ボタン
        val btn_change_scene = findViewById<Button>(R.id.btn_change_scene)
        btn_change_scene.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
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

    /*実際の通知管理の処理上から、権限なし・ミュートオフ・ミュートオンのときの処理*/
    private fun toggleDnd(): String {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted){
            return "おやすみモードへのアクセスが許可されていません。設定画面で許可してください。"
        }
        else if (notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            return "おやすみモード: ON"
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            return "おやすみモード: OFF"
        }
    }

    private fun getDndStatus(): String {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            return "おやすみモード：権限が無いので確認できません"
        }

        return if (
            notificationManager.currentInterruptionFilter ==
            NotificationManager.INTERRUPTION_FILTER_ALL
        ) {
            "おやすみモード：OFF"
        } else {
            "おやすみモード：ON"
        }
    }

/*通知をミュートにする権限があるかどうかのチェック*/
    private fun authchk(){
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
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

    private fun bindTimetableButton(
        buttonId: Int,
        slotKey: String
    ) {
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
            } else {
                blockedSlots.add(slotKey)
                button.setBackgroundColor(android.graphics.Color.GREEN)
            }

            saveBlockedSlots()
        }
    }
}
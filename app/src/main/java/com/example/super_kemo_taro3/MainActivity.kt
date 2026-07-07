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

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
    }

    private fun bindTimetableButton(
        buttonId: Int,
        slotKey: String
    ) {
        val button = findViewById<Button>(buttonId)

        button.setOnClickListener {
            if (slotKey in blockedSlots) {
                blockedSlots.remove(slotKey)
                button.setBackgroundColor(android.graphics.Color.LTGRAY)
            } else {
                blockedSlots.add(slotKey)
                button.setBackgroundColor(android.graphics.Color.GREEN)
            }
        }
    }
}
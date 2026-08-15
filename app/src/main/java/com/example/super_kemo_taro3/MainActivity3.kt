package com.example.super_kemo_taro3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btn_to_main).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.btn_to_timetable).setOnClickListener {
            startActivity(Intent(this, TimetableActivity::class.java))
        }

        findViewById<Button>(R.id.btn_to_main2).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }

        findViewById<Button>(R.id.btn_credit).setOnClickListener {
            showCreditDialog()
        }
    }

    private fun showCreditDialog() {
        val message = """
            ■ アプリ名
            言十ベル
            
            ■ バージョン
            1.0
            
            ■ グループ名
            スーパーKEMOたろう
            
            ■ メンバー(敬称略)
            スラリン
            ペパチキ
            くぬたろう
            
            ■ 使用ツール・環境
            Android Studio
            Android SDK
            Kotlin
            
            ■ 主な使用API
            NotificationManager（おやすみモード制御）
            AlarmManager（スケジュール管理）
            SharedPreferences（データ保存）
            BroadcastReceiver（アラーム受信・再起動対応）
            
            ■ 対応OS
            Android 8.0 以上
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("クレジット")
            .setMessage(message)
            .setPositiveButton("閉じる", null)
            .show()
    }
}
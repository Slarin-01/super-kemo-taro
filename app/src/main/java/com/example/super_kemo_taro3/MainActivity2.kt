package com.example.super_kemo_taro3

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    class TimePickerItem(
        val editTextId: Int,
        val saveKey: String,
        val defaultHour: Int,
        val defaultMinute: Int
    )
    val timePickerItems = listOf(
        TimePickerItem(R.id.time_start_1, "time_start_1", 8, 40),
        TimePickerItem(R.id.time_end_1, "time_end_1", 9, 30),
        TimePickerItem(R.id.time_start_2, "time_start_2", 9, 40),
        TimePickerItem(R.id.time_end_2, "time_end_2", 10, 30),
        TimePickerItem(R.id.time_start_3, "time_start_3", 10, 45),
        TimePickerItem(R.id.time_end_3, "time_end_3", 11, 35),
        TimePickerItem(R.id.time_start_4, "time_start_4", 11, 45),
        TimePickerItem(R.id.time_end_4, "time_end_4", 12, 35),
        TimePickerItem(R.id.time_start_5, "time_start_5", 13, 15),
        TimePickerItem(R.id.time_end_5, "time_end_5", 14, 25),
        TimePickerItem(R.id.time_start_6, "time_start_6", 14, 25),
        TimePickerItem(R.id.time_end_6, "time_end_6", 15, 15),
        TimePickerItem(R.id.time_start_7, "time_start_7", 15, 30),
        TimePickerItem(R.id.time_end_7, "time_end_7", 16, 20),
        TimePickerItem(R.id.time_start_8, "time_start_8", 16, 30),
        TimePickerItem(R.id.time_end_8, "time_end_8", 17, 20)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        for (item in timePickerItems) {
            setupTimePicker(item)
        }
        checkAllTimeRanges()

        //シーン変更ボタン
        val btn_change_scene2_save = findViewById<Button>(R.id.btn_change_scene2_save)
        btn_change_scene2_save.setOnClickListener {
            if (checkAllTimeRanges()) {
                showErrorDialog()
            } else {
                showSaveConfirmDialog()
            }
        }

        val btn_change_scene2_not_save = findViewById<Button>(R.id.btn_change_scene2_not_save)
        btn_change_scene2_not_save.setOnClickListener {
            showNotSaveConfirmDialog()
        }

        val btn_reset = findViewById<Button>(R.id.btn_reset)
        btn_reset.setOnClickListener {
            showResetConfirmDialog()
        }
    }
    //シーン変更確認
    private fun showSaveConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("変更した時間を保存しますか？")
            .setPositiveButton("保存する") { _, _ ->
                saveAllTimes()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
    private fun showErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("入力エラー")
            .setMessage("時間の順番に矛盾があるか、他の時限と重複しています。赤いマークがついている場所を修正してください。")
            .setPositiveButton("OK", null)
            .show()
    }
    private fun showNotSaveConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("変更した時間を保存せず戻りますか？")
            .setPositiveButton("保存せず戻る") { _, _ ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }
    private fun showResetConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("確認")
            .setMessage("初期設定である岡山大学の授業に合わせたタイムテーブルに戻しますか？")
            .setPositiveButton("初期設定に戻す"){ _, _ ->
                resetTimesToDefault()
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun resetTimesToDefault() {
        for (item in timePickerItems) {
            val editText = findViewById<EditText>(item.editTextId)
            if (editText != null) {
                val defaultTime = String.format("%02d:%02d", item.defaultHour, item.defaultMinute)
                editText.setText(defaultTime)
            }
        }
        checkAllTimeRanges()
    }

    //エラー確認の準備
    private fun timeToMinutes(timeStr: String): Int? {
        if (!timeStr.contains(":")) return null
        val parts = timeStr.split(":")
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        return h * 60 + m
    }
    //エラー確認
    private fun checkAllTimeRanges(): Boolean {
        var hasError = false
        val errorIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert)
        errorIcon?.setTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        // アイコンのリセット
        for (item in timePickerItems) {
            val et = findViewById<EditText>(item.editTextId)
            et?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
        var prevEndMinutes: Int? = null

        // 1限目〜8限目を順にチェック
        for (i in 0 until timePickerItems.size step 2) {
            val startItem = timePickerItems[i]
            val endItem = timePickerItems[i + 1]

            val startEt = findViewById<EditText>(startItem.editTextId) ?: continue
            val endEt = findViewById<EditText>(endItem.editTextId) ?: continue

            val startMins = timeToMinutes(startEt.text.toString())
            val endMins = timeToMinutes(endEt.text.toString())

            if (startMins != null && endMins != null) {
                //同コマ内のエラー確認
                if (startMins >= endMins) {
                    endEt.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null)
                    hasError = true
                }

                // コマとの重複エラー確認
                if (prevEndMinutes != null && prevEndMinutes > startMins) {
                    startEt.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null)
                    hasError = true
                }

                prevEndMinutes = endMins
            }
        }

        return hasError
    }

    private fun setupTimePicker(item: TimePickerItem) {
        val editText = findViewById<EditText>(item.editTextId) ?: return

        val sharedPref = getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)
        val savedTime = sharedPref.getString(item.saveKey, null)

        if (savedTime != null) {
            editText.setText(savedTime)
        } else {
            editText.setText(String.format("%02d:%02d", item.defaultHour, item.defaultMinute))
        }

        // ダイアログ表示(一時的)
        editText.setOnClickListener {
            val currentTime = editText.text.toString()
            var hour = item.defaultHour
            var minute = item.defaultMinute

            if (currentTime.contains(":")) {
                val parts = currentTime.split(":")
                hour = parts[0].toIntOrNull() ?: item.defaultHour
                minute = parts[1].toIntOrNull() ?: item.defaultMinute
            }
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                editText.setText(formattedTime)
                checkAllTimeRanges()
            }, hour, minute, true)
            timePickerDialog.show()
        }
    }
    private fun saveAllTimes() {
        val sharedPref = getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        for (item in timePickerItems) {
            val editText = findViewById<EditText>(item.editTextId)
            if (editText != null) {
                    // 現在画面に表示されている文字列を取得
                editor.putString(item.saveKey, editText.text.toString())
            }
        }
        editor.apply() //保存の確定
    }
}

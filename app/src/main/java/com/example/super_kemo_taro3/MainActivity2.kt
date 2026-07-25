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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    private class TimePickerItem(
        val editTextId: Int,
        val saveKey: String,
        val defaultHour: Int,
        val defaultMinute: Int
    )
    private val timePickerItems = listOf(
        TimePickerItem(R.id.time_start_1, "time_start_1", 8, 40),
        TimePickerItem(R.id.time_end_1, "time_end_1", 9, 30),
        TimePickerItem(R.id.time_start_2, "time_start_2", 9, 40),
        TimePickerItem(R.id.time_end_2, "time_end_2", 10, 30),
        TimePickerItem(R.id.time_start_3, "time_start_3", 10, 45),
        TimePickerItem(R.id.time_end_3, "time_end_3", 12, 35),
        TimePickerItem(R.id.time_start_4, "time_start_4", 13, 25),
        TimePickerItem(R.id.time_end_4, "time_end_4", 14, 15),
        TimePickerItem(R.id.time_start_5, "time_start_5", 14, 25),
        TimePickerItem(R.id.time_end_5, "time_end_5", 15, 15),
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

        //シーン変更ボタン
        val btn_change_scene2_save = findViewById<Button>(R.id.btn_change_scene2_save)
        btn_change_scene2_save.setOnClickListener {
            showSaveConfirmDialog()
        }

        val btn_change_scene2_not_save = findViewById<Button>(R.id.btn_change_scene2_not_save)
        btn_change_scene2_not_save.setOnClickListener {
            showNotSaveConfirmDialog()
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
                    // 現在画面に表示されている文字列を取得してセット
                editor.putString(item.saveKey, editText.text.toString())
            }
        }
        editor.apply() // 一括で保存を確定！
    }
}

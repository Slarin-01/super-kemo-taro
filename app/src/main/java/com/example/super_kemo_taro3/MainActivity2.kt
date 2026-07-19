package com.example.super_kemo_taro3

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //シーン変更ボタン
        val btn_change_scene2 = findViewById<Button>(R.id.btn_change_scene2)
        btn_change_scene2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //タイムテーブル
        setupTimePicker(R.id.time_start_1)
        setupTimePicker(R.id.time_end_1)
        setupTimePicker(R.id.time_start_2)
        setupTimePicker(R.id.time_end_2)
        setupTimePicker(R.id.time_start_3)
        setupTimePicker(R.id.time_end_3)
        setupTimePicker(R.id.time_start_4)
        setupTimePicker(R.id.time_end_4)
        setupTimePicker(R.id.time_start_5)
        setupTimePicker(R.id.time_end_5)
        setupTimePicker(R.id.time_start_6)
        setupTimePicker(R.id.time_end_6)
        setupTimePicker(R.id.time_start_7)
        setupTimePicker(R.id.time_end_7)
        setupTimePicker(R.id.time_start_8)
        setupTimePicker(R.id.time_end_8)
    }

    //タイムテーブル設定
    private fun setupTimePicker(editTextId: Int) {
        val editText = findViewById<EditText>(editTextId)
        editText.setOnClickListener {
            val hour = 0
            val minute = 0
            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                editText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true)
            timePickerDialog.show()
            Log.d("DND","$timePickerDialog")

        }

    }
}

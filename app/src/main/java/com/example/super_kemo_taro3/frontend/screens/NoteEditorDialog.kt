package com.example.super_kemo_taro3.frontend.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.util.Calendar

@Composable
internal fun NoteEditorDialog(
    isNew: Boolean,
    title: String,
    body: String,
    subject: String,
    dueAt: Long?,
    subjects: List<String>,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onDueAtChange: (Long?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var subjectsOpen by remember { mutableStateOf(false) }
    var datePickerOpen by rememberSaveable { mutableStateOf(false) }
    var timePickerOpen by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text(if (isNew) "メモを作成" else "メモを編集") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = onTitleChange, label = { Text("タイトル（任意）") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = subject, onValueChange = onSubjectChange, label = { Text("教科（任意）") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { TextButton(onClick = { subjectsOpen = true }) { Text("選択") } }
                    )
                    DropdownMenu(expanded = subjectsOpen, onDismissRequest = { subjectsOpen = false }) {
                        DropdownMenuItem(text = { Text("教科なし") }, onClick = {
                            onSubjectChange("")
                            subjectsOpen = false
                        })
                        subjects.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                onSubjectChange(option)
                                subjectsOpen = false
                            })
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("期限（任意）")
                    OutlinedButton(onClick = { datePickerOpen = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(dueAt?.let(::formatNoteDeadline) ?: "期限を選択")
                    }
                    if (dueAt != null) {
                        Row {
                            TextButton(onClick = { timePickerOpen = true }) { Text("時刻を変更") }
                            TextButton(onClick = { onDueAtChange(null) }) { Text("期限なしにする") }
                        }
                    }
                }
                OutlinedTextField(
                    value = body, onValueChange = onBodyChange, label = { Text("本文") },
                    placeholder = { Text("メモを入力") }, minLines = 5, maxLines = 10,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = title.isNotBlank() || body.isNotBlank()) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
    if (datePickerOpen) {
        NoteDatePicker(
            value = dueAt,
            onChange = { selectedDate ->
                onDueAtChange(selectedDate)
                datePickerOpen = false
                timePickerOpen = true
            },
            onDismiss = { datePickerOpen = false }
        )
    }
    if (timePickerOpen && dueAt != null) {
        NoteTimePicker(dueAt, onDueAtChange, onDismiss = { timePickerOpen = false })
    }
}

/** 新しい期限の日付を選ぶときは23:59を初期値とし、編集時は選択済みの時刻を保つ。 */
@Composable
private fun NoteDatePicker(value: Long?, onChange: (Long) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(context) {
        val initial = Calendar.getInstance().apply {
            if (value != null) timeInMillis = value else {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                onChange((initial.clone() as Calendar).apply { set(year, month, day) }.timeInMillis)
            },
            initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH)
        )
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}

@Composable
private fun NoteTimePicker(value: Long, onChange: (Long) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    DisposableEffect(context) {
        val initial = Calendar.getInstance().apply { timeInMillis = value }
        val dialog = TimePickerDialog(
            context,
            { _, hour, minute ->
                onChange((initial.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis)
            },
            initial.get(Calendar.HOUR_OF_DAY), initial.get(Calendar.MINUTE), true
        )
        dialog.setOnDismissListener { onDismiss() }
        dialog.show()
        onDispose {
            dialog.setOnDismissListener(null)
            dialog.dismiss()
        }
    }
}

package com.example.super_kemo_taro3.frontend.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.super_kemo_taro3.R
import com.example.super_kemo_taro3.backend.repository.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 一覧と作成・編集の入力欄。未保存の入力は画面回転やタブ移動でも保持する。 */
@Composable
fun NotesScreen(
    notes: List<Note>,
    onSaveNote: (String?, String, String) -> Boolean
) {
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }

    fun openEditor(note: Note?) {
        editingId = note?.id
        title = note?.title.orEmpty()
        body = note?.body.orEmpty()
        editorOpen = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = { openEditor(null) }) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = "メモを作成")
            }
        }
    ) { contentPadding ->
        Column(Modifier.fillMaxSize().padding(contentPadding)) {
            Text(
                "メモ",
                modifier = Modifier.padding(start = 24.dp, top = 48.dp, bottom = 32.dp),
                style = MaterialTheme.typography.displaySmall
            )
            if (notes.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.padding(bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_notes), contentDescription = null,
                            modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("メモはまだありません", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "右下の＋からメモを作成できます",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(note, onClick = { openEditor(note) })
                    }
                }
            }
        }
    }

    if (editorOpen) {
        NoteEditorDialog(
            isNew = editingId == null,
            title = title,
            body = body,
            onTitleChange = { title = it },
            onBodyChange = { body = it },
            onSave = { if (onSaveNote(editingId, title, body)) editorOpen = false },
            onDismiss = { editorOpen = false }
        )
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit) {
    val updatedAt = remember(note.updatedAt) {
        SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.JAPAN).format(Date(note.updatedAt))
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                note.title.ifBlank { "無題のメモ" }, style = MaterialTheme.typography.titleMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            if (note.body.isNotBlank()) {
                Text(
                    note.body, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3, overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                updatedAt, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoteEditorDialog(
    isNew: Boolean,
    title: String,
    body: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        title = { Text(if (isNew) "メモを作成" else "メモを編集") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title, onValueChange = onTitleChange, label = { Text("タイトル（任意）") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
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
}

@Preview(name = "メモ・ライト", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "メモ・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesScreenPreview() {
    AppScreen {
        MainScreenScaffold(MainDestination.Notes, onHome = {}, onNotes = {}, onSettings = {}) {
            NotesScreen(
                notes = listOf(Note("sample", "次の授業の持ち物", "教科書とノートを忘れずに。", 1793314800000L)),
                onSaveNote = { _, _, _ -> true }
            )
        }
    }
}

@Preview(name = "メモ・未作成", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EmptyNotesPreview() {
    AppScreen {
        MainScreenScaffold(MainDestination.Notes, onHome = {}, onNotes = {}, onSettings = {}) {
            NotesScreen(emptyList(), onSaveNote = { _, _, _ -> true })
        }
    }
}

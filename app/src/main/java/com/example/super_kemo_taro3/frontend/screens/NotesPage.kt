package com.example.super_kemo_taro3.frontend.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.super_kemo_taro3.R
import com.example.super_kemo_taro3.backend.repository.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 期限による分類と編集欄。作成日時は表示せず、教科・期限・本文を表示する。 */
@Composable
fun NotesScreen(
    activeNotes: List<Note>,
    expiredNotes: List<Note>,
    subjects: List<String>,
    onSaveNote: (String?, String, String, String, Long?) -> Boolean,
    onDeleteNote: (String) -> Unit,
    onDeleteExpiredNotes: () -> Unit,
    newNoteSubject: String? = null,
    onNewNoteOpened: () -> Unit = {}
) {
    var showExpired by rememberSaveable { mutableStateOf(false) }
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var dueAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    var deletingExpired by rememberSaveable { mutableStateOf(false) }
    val visibleNotes = if (showExpired) expiredNotes else activeNotes

    fun openEditor(note: Note?, initialSubject: String = "") {
        editingId = note?.id
        title = note?.title.orEmpty()
        body = note?.body.orEmpty()
        subject = note?.subject ?: initialSubject
        dueAt = note?.dueAt
        editorOpen = true
    }

    LaunchedEffect(newNoteSubject) {
        if (newNoteSubject != null) {
            openEditor(null, newNoteSubject)
            onNewNoteOpened()
        }
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
                modifier = Modifier.padding(start = 24.dp, top = 48.dp, bottom = 20.dp),
                style = MaterialTheme.typography.displaySmall
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showExpired, onClick = { showExpired = false },
                    label = { Text("現在進行中") }, modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = showExpired, onClick = { showExpired = true },
                    label = { Text("期限が過ぎたもの") }, modifier = Modifier.weight(1f)
                )
            }
            if (showExpired) {
                TextButton(
                    onClick = { deletingExpired = true }, enabled = expiredNotes.isNotEmpty(),
                    modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("期限が過ぎたメモをすべて削除") }
            }
            if (visibleNotes.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f).padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.padding(bottom = 80.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_notes), contentDescription = null,
                            modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (showExpired) "期限が過ぎたメモはありません" else "現在進行中のメモはありません",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!showExpired) {
                            Text(
                                "右下の＋からメモを作成できます", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                key(showExpired) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleNotes, key = { it.id }) { note ->
                            NoteCard(note, showExpired, onClick = { openEditor(note) }, onDelete = { deletingId = note.id })
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        NoteEditorDialog(
            isNew = editingId == null, title = title, body = body, subject = subject, dueAt = dueAt,
            subjects = (subjects + (activeNotes + expiredNotes).map { it.subject })
                .filter { it.isNotBlank() }.distinct().sorted(),
            onTitleChange = { title = it }, onBodyChange = { body = it },
            onSubjectChange = { subject = it }, onDueAtChange = { dueAt = it },
            onSave = {
                if (onSaveNote(editingId, title, body, subject, dueAt)) {
                    showExpired = dueAt?.let { it <= System.currentTimeMillis() } ?: false
                    editorOpen = false
                }
            },
            onDismiss = { editorOpen = false }
        )
    }
    if (deletingId != null) {
        ConfirmationDialog(
            title = "メモの削除", message = "このメモを完全に削除しますか？", confirmText = "削除する",
            onConfirm = {
                deletingId?.let(onDeleteNote)
                deletingId = null
            },
            onDismiss = { deletingId = null }
        )
    }
    if (deletingExpired) {
        ConfirmationDialog(
            title = "期限が過ぎたメモの削除",
            message = "期限が過ぎたメモをすべて完全に削除しますか？\n削除したメモは元に戻せません。",
            confirmText = "すべて削除する",
            onConfirm = {
                onDeleteExpiredNotes()
                deletingExpired = false
            },
            onDismiss = { deletingExpired = false }
        )
    }
}

@Composable
private fun NoteCard(note: Note, expired: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    note.title.ifBlank { "無題のメモ" }, style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        painterResource(R.drawable.ic_delete),
                        contentDescription = "${note.title.ifBlank { "無題のメモ" }}を削除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                "教科：${note.subject.ifBlank { "未設定" }}", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "期限：${note.dueAt?.let(::formatNoteDeadline) ?: "なし"}",
                style = MaterialTheme.typography.bodySmall,
                color = if (expired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (note.body.isNotBlank()) {
                Text(note.body, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

internal fun formatNoteDeadline(value: Long): String =
    SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN).format(Date(value))

@Preview(name = "メモ・ライト", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "メモ・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotesScreenPreview() {
    AppScreen {
        MainScreenScaffold(MainDestination.Notes, onHome = {}, onNotes = {}, onSettings = {}) {
            NotesScreen(
                activeNotes = listOf(Note("sample", "次の授業の持ち物", "教科書とノートを忘れずに。", 0L, "情報処理", 1793368800000L)),
                expiredNotes = emptyList(), subjects = listOf("情報処理", "数学"),
                onSaveNote = { _, _, _, _, _ -> true }, onDeleteNote = {}, onDeleteExpiredNotes = {}
            )
        }
    }
}

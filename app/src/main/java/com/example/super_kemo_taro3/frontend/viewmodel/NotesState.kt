package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.super_kemo_taro3.backend.repository.Note
import com.example.super_kemo_taro3.backend.repository.NotesRepository
import java.util.UUID

class NotesViewModel(
    private val repository: NotesRepository,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {
    var notes by mutableStateOf(repository.readNotes().sortedByDescending { it.updatedAt })
        private set

    var currentTime by mutableStateOf(now())
        private set

    val activeNotes: List<Note> get() = notes.filterNot { it.isExpired(currentTime) }
    val expiredNotes: List<Note> get() = notes.filter { it.isExpired(currentTime) }

    fun refreshTime() {
        currentTime = now()
    }

    /** 期限直後に分類を更新する。端末時計の変更も最長1分で反映する。 */
    fun millisUntilNextDeadline(): Long = notes.mapNotNull { it.dueAt }
        .filter { it > currentTime }.minOrNull()
        ?.let { (it - currentTime).coerceIn(1L, 60_000L) } ?: 60_000L

    /** 空のメモは作らず、既存メモの編集では同じIDを保つ。本文の改行・空白はそのまま保存する。 */
    fun saveNote(id: String?, title: String, body: String, subject: String = "", dueAt: Long? = null): Boolean {
        if (title.isBlank() && body.isBlank()) return false
        if (id != null && notes.none { it.id == id }) return false
        val note = Note(id ?: UUID.randomUUID().toString(), title.trim(), body, now(), subject.trim(), dueAt)
        val updated = (listOf(note) + notes.filterNot { it.id == note.id })
            .sortedByDescending { it.updatedAt }
        repository.writeNotes(updated)
        notes = updated
        refreshTime()
        return true
    }

    fun deleteNote(id: String) {
        val remaining = notes.filterNot { it.id == id }
        if (remaining.size == notes.size) return
        repository.writeNotes(remaining)
        notes = remaining
    }

    /** 確認ボタンを押した時点の期限で判定し、進行中・期限なしのメモは残す。 */
    fun deleteExpiredNotes() {
        refreshTime()
        val remaining = activeNotes
        if (remaining.size == notes.size) return
        repository.writeNotes(remaining)
        notes = remaining
    }
}

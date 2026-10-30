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

    /** 空のメモは作らず、既存メモの編集では同じIDを保つ。本文の改行・空白はそのまま保存する。 */
    fun saveNote(id: String?, title: String, body: String): Boolean {
        if (title.isBlank() && body.isBlank()) return false
        if (id != null && notes.none { it.id == id }) return false
        val note = Note(id ?: UUID.randomUUID().toString(), title.trim(), body, now())
        val updated = (listOf(note) + notes.filterNot { it.id == note.id })
            .sortedByDescending { it.updatedAt }
        repository.writeNotes(updated)
        notes = updated
        return true
    }
}

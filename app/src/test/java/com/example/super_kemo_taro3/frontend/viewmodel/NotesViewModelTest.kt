package com.example.super_kemo_taro3.frontend.viewmodel

import com.example.super_kemo_taro3.backend.repository.Note
import com.example.super_kemo_taro3.backend.repository.NotesRepository
import org.junit.Assert.*
import org.junit.Test

class NotesViewModelTest {
    private class MemoryNotesRepository : NotesRepository {
        var saved = emptyList<Note>()
        override fun readNotes(): List<Note> = saved
        override fun writeNotes(notes: List<Note>) { saved = notes }
    }

    @Test fun savingCreatesDistinctNotesAndReloadsNewestFirstWithTheOriginalBody() {
        val repository = MemoryNotesRepository()
        var time = 100L
        val model = NotesViewModel(repository) { time++ }
        val body = "  持ち物：教科書\nノート 📝\n"

        assertTrue(model.saveNote(null, "  授業  ", body))
        val first = model.notes.single()
        assertEquals("授業", first.title)
        assertEquals(body, first.body)
        assertTrue(model.saveNote(null, "", "本文だけのメモ"))
        assertEquals(2, model.notes.map { it.id }.distinct().size)

        val restored = NotesViewModel(repository)
        assertEquals(model.notes, restored.notes)
        assertEquals("本文だけのメモ", restored.notes.first().body)
        assertEquals(first, restored.notes.last())
    }

    @Test fun editingKeepsTheIdentityAndOtherNotesAndMovesTheUpdatedNoteToTheTop() {
        val repository = MemoryNotesRepository().apply {
            saved = listOf(Note("old", "旧", "本文", 10), Note("new", "新", "ほかのメモ", 20))
        }
        val model = NotesViewModel(repository) { 30L }
        assertEquals(listOf("new", "old"), model.notes.map { it.id })

        assertTrue(model.saveNote("old", "更新", "新しい本文"))

        assertEquals(listOf("old", "new"), model.notes.map { it.id })
        assertEquals(Note("old", "更新", "新しい本文", 30), model.notes.first())
        assertEquals(Note("new", "新", "ほかのメモ", 20), model.notes.last())
        assertEquals(model.notes, NotesViewModel(repository).notes)
    }

    @Test fun emptyInputAndUnknownNoteDoNotWriteAnythingButATitleAloneCanBeSaved() {
        val repository = MemoryNotesRepository()
        val model = NotesViewModel(repository)

        assertFalse(model.saveNote(null, " \t", "\n　"))
        assertFalse(model.saveNote("missing", "タイトル", "本文"))
        assertTrue(repository.saved.isEmpty())
        assertTrue(model.notes.isEmpty())

        assertTrue(model.saveNote(null, "タイトルだけ", ""))
        val before = model.notes
        assertFalse(model.saveNote(before.single().id, "", ""))
        assertEquals(before, repository.saved)
        assertEquals(before, model.notes)
    }
}

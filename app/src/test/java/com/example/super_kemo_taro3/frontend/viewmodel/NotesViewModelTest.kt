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

    @Test fun subjectAndDeadlineSurviveReloadAndCanBeChangedOrRemoved() {
        val repository = MemoryNotesRepository()
        val model = NotesViewModel(repository) { 100L }
        assertTrue(model.saveNote(null, "課題", "レポート", " 数学 ", 200L))
        val saved = NotesViewModel(repository) { 100L }.notes.single()
        assertEquals("数学", saved.subject)
        assertEquals(200L, saved.dueAt)
        assertTrue(model.saveNote(saved.id, "課題", "レポート", "英語", 300L))
        assertEquals("英語", model.notes.single().subject)
        assertEquals(300L, model.notes.single().dueAt)
        assertTrue(model.saveNote(saved.id, "課題", "レポート", "", null))
        val cleared = NotesViewModel(repository).notes.single()
        assertEquals("", cleared.subject)
        assertNull(cleared.dueAt)
    }

    @Test fun passingTheExactDeadlineMovesOnlyDueNotesWithoutChangingTheirContents() {
        val repository = MemoryNotesRepository().apply {
            saved = listOf(
                Note("due", "課題", "本文", 10, "数学", 1000),
                Note("later", "予習", "次回", 20, "英語", 2000),
                Note("none", "持ち物", "ノート", 30)
            )
        }
        var time = 999L
        val model = NotesViewModel(repository) { time }
        assertEquals(3, model.activeNotes.size)
        assertTrue(model.expiredNotes.isEmpty())
        assertEquals(1L, model.millisUntilNextDeadline())
        val before = repository.saved

        time = 1000L
        model.refreshTime()

        assertEquals(listOf("due"), model.expiredNotes.map { it.id })
        assertEquals(setOf("later", "none"), model.activeNotes.map { it.id }.toSet())
        assertEquals(1000L, model.millisUntilNextDeadline())
        assertEquals(before, repository.saved)
        assertEquals(repository.saved.first(), model.expiredNotes.single())
        assertEquals(model.expiredNotes, NotesViewModel(repository) { time }.expiredNotes)
    }

    @Test fun extendingOrRemovingAnExpiredDeadlineReturnsTheNoteToActive() {
        val repository = MemoryNotesRepository().apply {
            saved = listOf(Note("one", "課題", "本文", 10, "数学", 100))
        }
        val model = NotesViewModel(repository) { 200L }
        assertEquals(1, model.expiredNotes.size)
        model.saveNote("one", "課題", "本文", "数学", 300L)
        assertEquals(1, model.activeNotes.size)
        assertTrue(model.expiredNotes.isEmpty())
        model.saveNote("one", "課題", "本文", "数学", 100L)
        assertEquals(1, model.expiredNotes.size)
        model.saveNote("one", "課題", "本文", "数学", null)
        assertEquals(1, model.activeNotes.size)
        assertTrue(model.expiredNotes.isEmpty())
    }

    @Test fun deletingOneNotePersistsTheRemovalAndPreservesOtherNotes() {
        val repository = MemoryNotesRepository().apply {
            saved = listOf(Note("one", "1", "本文", 10), Note("two", "2", "本文", 20))
        }
        val model = NotesViewModel(repository)
        model.deleteNote("one")
        model.deleteNote("missing")
        assertEquals(listOf(Note("two", "2", "本文", 20)), model.notes)
        assertEquals(model.notes, NotesViewModel(repository).notes)
    }

    @Test fun bulkDeletionRechecksTheClockAndPreservesFutureAndUndatedNotesAcrossReloads() {
        val repository = MemoryNotesRepository().apply {
            saved = listOf(
                Note("expired", "1", "本文", 10, dueAt = 100),
                Note("becomingDue", "2", "本文", 20, dueAt = 200),
                Note("future", "3", "本文", 30, dueAt = 300),
                Note("undated", "4", "本文", 40)
            )
        }
        var time = 150L
        val model = NotesViewModel(repository) { time }
        assertEquals(listOf("expired"), model.expiredNotes.map { it.id })
        time = 200L

        model.deleteExpiredNotes()

        assertEquals(listOf("undated", "future"), model.notes.map { it.id })
        assertTrue(model.expiredNotes.isEmpty())
        assertEquals(model.notes, NotesViewModel(repository) { time }.notes)
        model.deleteExpiredNotes()
        assertEquals(model.notes, repository.saved)
    }

    @Test fun nextRefreshIsBoundedAndClockChangesCanRestoreNotesToActive() {
        val repository = MemoryNotesRepository().apply {
            saved = listOf(Note("one", "課題", "本文", 10, dueAt = 100_000))
        }
        var time = 0L
        val model = NotesViewModel(repository) { time }
        assertEquals(60_000L, model.millisUntilNextDeadline())
        time = 100_000L
        model.refreshTime()
        assertEquals(1, model.expiredNotes.size)
        assertEquals(60_000L, model.millisUntilNextDeadline())
        time = 99_999L
        model.refreshTime()
        assertTrue(model.expiredNotes.isEmpty())
        assertEquals(1L, model.millisUntilNextDeadline())
    }
}

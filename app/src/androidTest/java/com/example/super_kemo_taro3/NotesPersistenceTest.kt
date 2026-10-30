package com.example.super_kemo_taro3

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.backend.repository.Note
import com.example.super_kemo_taro3.backend.repository.SharedPreferencesNotesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesPersistenceTest {
    @Test fun notesSurviveRepositoryRecreationAndUpdatesPreserveOtherNotes() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                base.getSharedPreferences("notes_test_$name", mode)
        }
        val preferences = context.getSharedPreferences("notes", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        try {
            assertEquals(emptyList<Note>(), SharedPreferencesNotesRepository(context).readNotes())
            val notes = listOf(
                Note("one", "持ち物 📝", "教科書\n\"ノート\"\\筆記用具\n", 100L, "情報処理", 500L),
                Note("two", "", "本文だけのメモ", 200L)
            )
            var changes = 0
            val repository = SharedPreferencesNotesRepository(context) {
                changes++
                // ウィジェット更新の通知時点で、新しいメモを読み取れることを確認する。
                assertEquals(notes, SharedPreferencesNotesRepository(context).readNotes())
            }
            repository.readNotes()
            assertEquals(0, changes)
            repository.writeNotes(notes)
            assertEquals(1, changes)
            assertEquals(notes, SharedPreferencesNotesRepository(context).readNotes())

            val updated = listOf(notes.first().copy(title = "更新", updatedAt = 300L), notes.last())
            SharedPreferencesNotesRepository(context).writeNotes(updated)
            assertEquals(updated, SharedPreferencesNotesRepository(context).readNotes())

            val cleared = updated.map { it.copy(subject = "", dueAt = null) }
            SharedPreferencesNotesRepository(context).writeNotes(cleared)
            assertEquals(cleared, SharedPreferencesNotesRepository(context).readNotes())

            preferences.edit().putString("items", """[{"id":"legacy","title":"以前のメモ","body":"本文","updatedAt":10}]""").commit()
            val legacy = SharedPreferencesNotesRepository(context).readNotes().single()
            assertEquals(Note("legacy", "以前のメモ", "本文", 10), legacy)
            assertNull(legacy.dueAt)
            SharedPreferencesNotesRepository(context).writeNotes(listOf(legacy.copy(subject = "英語", dueAt = 600L)))
            assertEquals("英語", SharedPreferencesNotesRepository(context).readNotes().single().subject)
        } finally {
            preferences.edit().clear().commit()
        }
    }
}

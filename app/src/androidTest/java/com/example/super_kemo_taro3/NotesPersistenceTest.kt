package com.example.super_kemo_taro3

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.backend.repository.Note
import com.example.super_kemo_taro3.backend.repository.SharedPreferencesNotesRepository
import org.junit.Assert.assertEquals
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
                Note("one", "持ち物 📝", "教科書\n\"ノート\"\\筆記用具\n", 100L),
                Note("two", "", "本文だけのメモ", 200L)
            )
            SharedPreferencesNotesRepository(context).writeNotes(notes)
            assertEquals(notes, SharedPreferencesNotesRepository(context).readNotes())

            val updated = listOf(notes.first().copy(title = "更新", updatedAt = 300L), notes.last())
            SharedPreferencesNotesRepository(context).writeNotes(updated)
            assertEquals(updated, SharedPreferencesNotesRepository(context).readNotes())
        } finally {
            preferences.edit().clear().commit()
        }
    }
}

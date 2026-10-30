package com.example.super_kemo_taro3.backend.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: Long
)

interface NotesRepository {
    fun readNotes(): List<Note>
    fun writeNotes(notes: List<Note>)
}

/** メモは時間割や設定と分けて端末内に保存する。 */
class SharedPreferencesNotesRepository(context: Context) : NotesRepository {
    private val preferences = context.applicationContext
        .getSharedPreferences("notes", Context.MODE_PRIVATE)

    override fun readNotes(): List<Note> = runCatching {
        val array = JSONArray(preferences.getString("items", "[]"))
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            Note(id, item.optString("title"), item.optString("body"), item.optLong("updatedAt"))
        }
    }.getOrDefault(emptyList())

    override fun writeNotes(notes: List<Note>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(JSONObject().apply {
                put("id", note.id)
                put("title", note.title)
                put("body", note.body)
                put("updatedAt", note.updatedAt)
            })
        }
        preferences.edit().putString("items", array.toString()).apply()
    }
}

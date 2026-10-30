package com.example.super_kemo_taro3.backend.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updatedAt: Long,
    val subject: String = "",
    val dueAt: Long? = null
) {
    fun isExpired(now: Long): Boolean = dueAt?.let { it <= now } ?: false
}

interface NotesRepository {
    fun readNotes(): List<Note>
    fun writeNotes(notes: List<Note>)
}

/** メモは時間割や設定と分けて端末内に保存する。 */
class SharedPreferencesNotesRepository(
    context: Context,
    private val onNotesChanged: () -> Unit = {}
) : NotesRepository {
    private val preferences = context.applicationContext
        .getSharedPreferences("notes", Context.MODE_PRIVATE)

    override fun readNotes(): List<Note> = runCatching {
        val array = JSONArray(preferences.getString("items", "[]"))
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            Note(
                id = id,
                title = item.optString("title"),
                body = item.optString("body"),
                updatedAt = item.optLong("updatedAt"),
                subject = item.optString("subject"),
                dueAt = if (item.isNull("dueAt")) null else item.optLong("dueAt")
            )
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
                put("subject", note.subject)
                put("dueAt", note.dueAt ?: JSONObject.NULL)
            })
        }
        preferences.edit().putString("items", array.toString()).apply()
        onNotesChanged()
    }
}

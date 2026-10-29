/*
 * バックエンド・設定管理: 設定の読み書きの契約とSharedPreferences実装。保存キーを一元管理。
 * サービスと状態管理はSettingsRepositoryを利用し、保存先の具体的な扱いをこのファイルに閉じ込める。
 */
package com.example.super_kemo_taro3.backend.repository

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private const val SLOT_SUBJECT_PREFIX = "slot_subject_"
private const val SLOT_LINKS_PREFIX = "slot_links_"

// 1. 呼び出し側が利用する設定保存の契約
data class MoodleLogin(val siteId: String, val expiresAt: Long)

/** 同じ時限に複数科目がある場合も、科目名・教室・リンクの組を保持する。 */
data class CourseLinks(
    val subject: String,
    val moodleUrl: String? = null,
    val syllabusUrl: String? = null,
    val classroom: String? = null
)

/** 授業終了時のおやすみモード解除後に、端末をサイレントにするか通常モードに戻すか。 */
enum class LessonEndNotificationMode {
    KEEP_MUTED,
    UNMUTE_ALL
}

/** 画面の状態管理とサービスが共用する設定の窓口。保存先の名前やキーは実装側で管理する。 */
interface SettingsRepository {
    fun readBlockedSlots(): Set<String>
    fun writeBlockedSlots(slots: Set<String>)
    fun readSlotSubjects(): Map<String, String>
    fun writeSlotSubjects(subjects: Map<String, String>)
    fun readSlotCourseLinks(): Map<String, List<CourseLinks>>
    fun writeSlotCourseLinks(links: Map<String, List<CourseLinks>>)
    fun readMoodleToken(): String?
    fun writeMoodleToken(token: String?)
    fun readMoodleLogin(): MoodleLogin?
    fun writeMoodleLogin(login: MoodleLogin?)
    fun readPauseUntil(): Long
    /** 日曜日末の停止期限を保存する。nullを渡すと一時停止を解除する。 */
    fun writePauseUntil(deadline: Long?)
    fun readAutomaticMutingEnabled(): Boolean
    fun writeAutomaticMutingEnabled(enabled: Boolean)
    fun readLessonEndNotificationMode(): LessonEndNotificationMode
    fun writeLessonEndNotificationMode(mode: LessonEndNotificationMode)
    fun readTime(key: String): String?
    fun writeTimes(times: Map<String, String>)
    /** 未設定はnullを返し、端末の表示モードを引き継げるようにする。 */
    fun readDarkMode(): Boolean?
    fun writeDarkMode(isDark: Boolean?)
    fun readThemeColor(): Int?
    fun writeThemeColor(color: Int)
    fun isAlarmReceiverMigrated(): Boolean
    fun markAlarmReceiverMigrated()
}

// 2. 既存の保存領域・キーを使う実装
/** SettingsRepositoryをSharedPreferencesで実装する。端末に保存された従来の3領域・キーを引き継ぐ。 */
class SharedPreferencesSettingsRepository(
    context: Context,
    private val onTimetableChanged: () -> Unit = {}
) : SettingsRepository {
    private val appContext = context.applicationContext
    // 保存領域と既存キーを維持し、更新前の設定をそのまま読み込む。
    private val timetable = appContext.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
    private val times = appContext.getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)
    private val theme = appContext.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    // 保存領域が持つ集合を直接変更しないよう、読み書きの両方でコピーする。
    override fun readBlockedSlots(): Set<String> =
        timetable.getStringSet("blocked_slots", emptySet())?.toSet() ?: emptySet()

    override fun writeBlockedSlots(slots: Set<String>) {
        timetable.edit().putStringSet("blocked_slots", slots.toSet()).apply()
        onTimetableChanged()
    }

    override fun readSlotSubjects(): Map<String, String> = timetable.all.mapNotNull { (key, value) ->
        if (key.startsWith(SLOT_SUBJECT_PREFIX) && value is String) {
            key.removePrefix(SLOT_SUBJECT_PREFIX) to value
        } else null
    }.toMap()

    override fun writeSlotSubjects(subjects: Map<String, String>) {
        timetable.edit().apply {
            timetable.all.keys.filter { it.startsWith(SLOT_SUBJECT_PREFIX) }.forEach { remove(it) }
            subjects.forEach { (slot, subject) -> putString("$SLOT_SUBJECT_PREFIX$slot", subject) }
        }.apply()
        onTimetableChanged()
    }

    override fun readSlotCourseLinks(): Map<String, List<CourseLinks>> =
        timetable.all.mapNotNull { (key, value) ->
            if (!key.startsWith(SLOT_LINKS_PREFIX) || value !is String) return@mapNotNull null
            val links = runCatching {
                val array = JSONArray(value)
                (0 until array.length()).mapNotNull course@ { index ->
                    val item = array.optJSONObject(index) ?: return@course null
                    CourseLinks(
                        subject = item.optString("subject"),
                        moodleUrl = item.optString("moodle").takeIf { it.isNotBlank() },
                        syllabusUrl = item.optString("syllabus").takeIf { it.isNotBlank() },
                        classroom = item.optString("classroom").takeIf { it.isNotBlank() }
                    )
                }
            }.getOrDefault(emptyList())
            key.removePrefix(SLOT_LINKS_PREFIX) to links
        }.toMap()

    override fun writeSlotCourseLinks(links: Map<String, List<CourseLinks>>) {
        timetable.edit().apply {
            timetable.all.keys.filter { it.startsWith(SLOT_LINKS_PREFIX) }.forEach { remove(it) }
            links.forEach { (slot, courses) ->
                val array = JSONArray()
                courses.forEach { course ->
                    array.put(JSONObject().apply {
                        put("subject", course.subject)
                        put("moodle", course.moodleUrl)
                        put("syllabus", course.syllabusUrl)
                        put("classroom", course.classroom)
                    })
                }
                putString("$SLOT_LINKS_PREFIX$slot", array.toString())
            }
        }.apply()
        onTimetableChanged()
    }

    override fun readMoodleToken(): String? = timetable.getString("moodle_token", null)

    override fun writeMoodleToken(token: String?) {
        // ブラウザから戻った別プロセスが読むため、戻りを待ってから保存する。
        timetable.edit().apply {
            if (token == null) remove("moodle_token") else putString("moodle_token", token)
        }.commit()
    }

    override fun readMoodleLogin(): MoodleLogin? {
        val siteId = timetable.getString("moodle_login_site", null) ?: return null
        return MoodleLogin(siteId, timetable.getLong("moodle_login_expires", 0L))
    }

    override fun writeMoodleLogin(login: MoodleLogin?) {
        timetable.edit().apply {
            if (login == null) {
                remove("moodle_login_site")
                remove("moodle_login_expires")
            } else {
                putString("moodle_login_site", login.siteId)
                putLong("moodle_login_expires", login.expiresAt)
            }
        }.commit()
    }

    override fun readPauseUntil(): Long = timetable.getLong("pause_until_sunday_time", 0L)

    override fun writePauseUntil(deadline: Long?) {
        timetable.edit().apply {
            if (deadline == null) remove("pause_until_sunday_time")
            else putLong("pause_until_sunday_time", deadline)
        }.apply()
    }

    // 既存利用者は、設定を変更するまでこれまでどおり自動ミュートを利用する。
    override fun readAutomaticMutingEnabled(): Boolean =
        timetable.getBoolean("automatic_muting_enabled", true)

    override fun writeAutomaticMutingEnabled(enabled: Boolean) {
        timetable.edit().putBoolean("automatic_muting_enabled", enabled).apply()
    }

    override fun readLessonEndNotificationMode(): LessonEndNotificationMode {
        val saved = timetable.getString("lesson_end_notification_mode", null)
        return LessonEndNotificationMode.entries.firstOrNull { it.name == saved }
            ?: LessonEndNotificationMode.KEEP_MUTED
    }

    override fun writeLessonEndNotificationMode(mode: LessonEndNotificationMode) {
        timetable.edit().putString("lesson_end_notification_mode", mode.name).apply()
    }

    override fun readTime(key: String): String? = times.getString(key, null)

    override fun writeTimes(times: Map<String, String>) {
        // 全時刻を同じ編集で反映する。直後の再予約は更新済みの値を読み取れる。
        val editor = this.times.edit()
        times.forEach { (key, value) -> editor.putString(key, value) }
        editor.apply()
        onTimetableChanged()
    }

    override fun readDarkMode(): Boolean? =
        if (theme.contains("is_dark_mode")) theme.getBoolean("is_dark_mode", false) else null

    override fun writeDarkMode(isDark: Boolean?) {
        theme.edit().apply {
            if (isDark == null) remove("is_dark_mode") else putBoolean("is_dark_mode", isDark)
        }.apply()
    }

    override fun readThemeColor(): Int? =
        if (theme.contains("theme_color")) theme.getInt("theme_color", 0) and 0xFFFFFF else null

    override fun writeThemeColor(color: Int) {
        theme.edit().putInt("theme_color", color and 0xFFFFFF).apply()
    }

    override fun isAlarmReceiverMigrated(): Boolean =
        timetable.getBoolean("alarm_receiver_migrated_v1", false)

    override fun markAlarmReceiverMigrated() {
        timetable.edit().putBoolean("alarm_receiver_migrated_v1", true).apply()
    }
}


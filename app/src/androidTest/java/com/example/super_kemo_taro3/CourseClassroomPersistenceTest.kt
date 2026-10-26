package com.example.super_kemo_taro3

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.backend.repository.CourseLinks
import com.example.super_kemo_taro3.backend.repository.SharedPreferencesSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourseClassroomPersistenceTest {
    @Test fun classroomsSurviveRecreationAndOlderCoursesRemainReadable() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val context = object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                base.getSharedPreferences("classroom_test_$name", mode)
        }
        val preferences = context.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        try {
            val courses = mapOf("MON_1" to listOf(
                CourseLinks("情報処理", classroom = "一般教育棟A21教室"),
                CourseLinks("実験", classroom = "教育5206講義室")
            ))
            SharedPreferencesSettingsRepository(context).writeSlotCourseLinks(courses)
            assertEquals(courses, SharedPreferencesSettingsRepository(context).readSlotCourseLinks())

            preferences.edit().putString("slot_links_TUE_2", """[{"subject":"英語"}]""").commit()
            val legacy = SharedPreferencesSettingsRepository(context).readSlotCourseLinks().getValue("TUE_2").single()
            assertEquals("英語", legacy.subject)
            assertNull(legacy.classroom)

            SharedPreferencesSettingsRepository(context).writeSlotCourseLinks(emptyMap())
            assertEquals(emptyMap<String, List<CourseLinks>>(),
                SharedPreferencesSettingsRepository(context).readSlotCourseLinks())
        } finally {
            preferences.edit().clear().commit()
        }
    }
}

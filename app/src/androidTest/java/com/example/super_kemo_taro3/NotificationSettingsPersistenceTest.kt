package com.example.super_kemo_taro3

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.backend.repository.LessonEndNotificationMode
import com.example.super_kemo_taro3.backend.repository.SharedPreferencesSettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSettingsPersistenceTest {
    @Test fun automaticMutingDefaultsToEnabledAndItsSwitchSurvivesRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        val key = "automatic_muting_enabled"
        val existed = preferences.contains(key)
        val original = preferences.getBoolean(key, true)
        try {
            preferences.edit().remove(key).commit()
            assertEquals(true, SharedPreferencesSettingsRepository(context).readAutomaticMutingEnabled())

            for (enabled in listOf(false, true)) {
                SharedPreferencesSettingsRepository(context).writeAutomaticMutingEnabled(enabled)
                assertEquals(enabled, SharedPreferencesSettingsRepository(context).readAutomaticMutingEnabled())
            }
        } finally {
            preferences.edit().apply {
                if (existed) putBoolean(key, original) else remove(key)
            }.commit()
        }
    }

    @Test fun selectionSurvivesRepositoryRecreationAndUnknownValuesUseSilentMode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("timetable_settings", Context.MODE_PRIVATE)
        val key = "lesson_end_notification_mode"
        val original = preferences.getString(key, null)
        try {
            preferences.edit().remove(key).commit()
            assertEquals(LessonEndNotificationMode.KEEP_MUTED,
                SharedPreferencesSettingsRepository(context).readLessonEndNotificationMode())

            for (mode in LessonEndNotificationMode.entries) {
                SharedPreferencesSettingsRepository(context).writeLessonEndNotificationMode(mode)
                assertEquals(mode, SharedPreferencesSettingsRepository(context).readLessonEndNotificationMode())
            }

            preferences.edit().putString(key, "unknown").commit()
            assertEquals(LessonEndNotificationMode.KEEP_MUTED,
                SharedPreferencesSettingsRepository(context).readLessonEndNotificationMode())
        } finally {
            preferences.edit().apply {
                if (original == null) remove(key) else putString(key, original)
            }.commit()
        }
    }
}

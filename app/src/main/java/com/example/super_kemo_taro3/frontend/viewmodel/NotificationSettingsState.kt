package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.super_kemo_taro3.backend.repository.LessonEndNotificationMode
import com.example.super_kemo_taro3.backend.repository.SettingsRepository
import com.example.super_kemo_taro3.backend.service.NotificationModeService

/** 自動ミュートの有効・無効と終了時の選択を即時保存する。無効時はアプリのおやすみモードを解除する。 */
class NotificationSettingsViewModel(
    private val settings: SettingsRepository,
    private val notifications: NotificationModeService
) : ViewModel() {
    var automaticMutingEnabled by mutableStateOf(settings.readAutomaticMutingEnabled())
        private set

    var selectedMode by mutableStateOf(settings.readLessonEndNotificationMode())
        private set

    fun setAutomaticMuting(enabled: Boolean) {
        settings.writeAutomaticMutingEnabled(enabled)
        automaticMutingEnabled = enabled
        if (!enabled && notifications.currentMuted() == true) notifications.setMuted(false)
    }

    fun selectMode(mode: LessonEndNotificationMode) {
        settings.writeLessonEndNotificationMode(mode)
        selectedMode = mode
    }
}

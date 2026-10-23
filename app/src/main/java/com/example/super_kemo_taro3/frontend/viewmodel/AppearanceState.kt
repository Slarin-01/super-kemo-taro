package com.example.super_kemo_taro3.frontend.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.super_kemo_taro3.backend.repository.SettingsRepository

data class AppearanceUiState(
    val darkMode: Boolean? = null,
    val color: Int = DEFAULT_THEME_COLOR
)

const val DEFAULT_THEME_COLOR = 0x1A73E8

/** 全画面で共用し、選択を即時保存する。nullは端末の明暗設定に追従する。 */
class AppearanceState(private val settings: SettingsRepository) {
    var uiState by mutableStateOf(
        AppearanceUiState(settings.readDarkMode(), settings.readThemeColor() ?: DEFAULT_THEME_COLOR)
    )
        private set

    fun selectMode(darkMode: Boolean?) {
        settings.writeDarkMode(darkMode)
        uiState = uiState.copy(darkMode = darkMode)
    }

    fun selectColor(color: Int) {
        val rgb = color and 0xFFFFFF
        settings.writeThemeColor(rgb)
        uiState = uiState.copy(color = rgb)
    }
}

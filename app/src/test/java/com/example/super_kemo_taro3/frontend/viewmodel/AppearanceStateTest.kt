package com.example.super_kemo_taro3.frontend.viewmodel

import com.example.super_kemo_taro3.backend.service.MemorySettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppearanceStateTest {
    @Test fun existingDarkModeIsRestoredWithDefaultColor() {
        val settings = MemorySettingsRepository().apply { darkMode = true }
        val state = AppearanceState(settings)
        assertEquals(true, state.uiState.darkMode)
        assertEquals(DEFAULT_THEME_COLOR, state.uiState.color)
    }

    @Test fun deviceModeClearsExplicitModeAndSurvivesRecreation() {
        val settings = MemorySettingsRepository()
        val state = AppearanceState(settings)
        state.selectMode(true)
        state.selectMode(null)
        assertNull(settings.darkMode)
        assertNull(AppearanceState(settings).uiState.darkMode)
    }

    @Test fun colorAndModeAreSavedIndependently() {
        val settings = MemorySettingsRepository()
        val state = AppearanceState(settings)
        state.selectMode(false)
        state.selectColor(0xB2781A)
        state.selectMode(true)
        val restored = AppearanceState(settings).uiState
        assertEquals(true, restored.darkMode)
        assertEquals(0xB2781A, restored.color)
    }

    @Test fun opaqueAndroidColorsAreNormalizedToRgb() {
        val settings = MemorySettingsRepository()
        val state = AppearanceState(settings)
        state.selectColor(0xFF123456.toInt())
        assertEquals(0x123456, state.uiState.color)
        assertEquals(0x123456, AppearanceState(settings).uiState.color)
    }
}

package com.example.super_kemo_taro3.frontend.viewmodel

import com.example.super_kemo_taro3.backend.service.NotificationGateway
import com.example.super_kemo_taro3.backend.service.NotificationModeService
import org.junit.Assert.*
import org.junit.Test

class NotificationTestViewModelTest {
    private val gateway = TestNotificationGateway()
    private val service = NotificationModeService(gateway)
    private val model = NotificationTestViewModel(service)

    @Test fun systemModeDoesNotTrapAppInOffRequests() {
        gateway.systemMuted = true
        model.refresh()
        assertEquals("通知モード：OFF", model.status)
        assertTrue(model.needsSystemSettings)

        repeat(3) {
            assertFalse(model.toggle())
            assertEquals("通知モード：ON", model.status)
            assertFalse(model.needsSystemSettings)
            assertFalse(model.toggle())
            assertEquals("通知モード：OFF", model.status)
            assertTrue(model.needsSystemSettings)
        }
        assertEquals(listOf(true, false, true, false, true, false), gateway.userChanges)
        assertTrue(gateway.systemMuted)
    }

    @Test fun ignoredRequestDoesNotDisplayAFalseSuccess() {
        gateway.ignoreChanges = true
        assertFalse(model.toggle())
        assertEquals("通知モード：OFF", model.status)
    }

    @Test fun refreshReflectsChangesMadeInSystemSettings() {
        gateway.appMuted = true
        model.refresh()
        assertEquals("通知モード：ON", model.status)
        gateway.appMuted = false
        gateway.systemMuted = true
        model.refresh()
        assertEquals("通知モード：OFF", model.status)
        assertTrue(model.needsSystemSettings)
        gateway.systemMuted = false
        model.refresh()
        assertFalse(model.needsSystemSettings)
    }

    @Test fun permissionLossClearsSystemHintAndRequestsAccess() {
        gateway.systemMuted = true
        model.refresh()
        gateway.allowed = false
        model.refresh()
        assertEquals("通知モード：権限が無いので確認できません", model.status)
        assertFalse(model.needsSystemSettings)
        assertTrue(model.toggle())
        assertTrue(gateway.userChanges.isEmpty())
    }

    @Test fun scheduledChangeIsNotTreatedAsAUserOverride() {
        assertTrue(service.setMuted(true))
        assertTrue(gateway.appMuted)
        assertTrue(gateway.userChanges.isEmpty())
        assertEquals(false, service.toggle())
        assertEquals(listOf(false), gateway.userChanges)
    }

    private class TestNotificationGateway : NotificationGateway {
        var allowed = true
        var appMuted = false
        var systemMuted = false
        var ignoreChanges = false
        val userChanges = mutableListOf<Boolean>()
        override fun hasPolicyAccess() = allowed
        override fun isMuted() = appMuted
        override fun isDeviceMuted() = systemMuted || appMuted
        override fun setNotificationSoundsMuted(muted: Boolean) = Unit
        override fun setMuted(muted: Boolean) {
            if (!ignoreChanges) appMuted = muted
        }
        override fun setMutedByUser(muted: Boolean) {
            userChanges += muted
            setMuted(muted)
        }
    }
}

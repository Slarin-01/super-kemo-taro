package com.example.super_kemo_taro3

import android.app.NotificationManager
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.service.notification.Condition
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.backend.service.AndroidNotificationGateway
import com.example.super_kemo_taro3.backend.service.NotificationModeService
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** 実機の通知制御を検証し、終了時にアプリと端末の元の状態へ戻す。 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 35)
class NotificationControlDeviceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val gateway = AndroidNotificationGateway(context)
    private val service = NotificationModeService(gateway)

    @Test fun systemModeEnabledWhileAppIsOffDoesNotPreventRepeatedToggles() {
        assertTrue("通知ポリシーへのアクセスを許可してください", manager.isNotificationPolicyAccessGranted)
        val originalFilter = manager.currentInterruptionFilter
        val originalAppMuted = appRuleIsActive()
        try {
            gateway.setMuted(false)
            if (originalFilter == NotificationManager.INTERRUPTION_FILTER_ALL) shell("cmd notification set_dnd priority")
            awaitState { !appRuleIsActive() && manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL }

            // 本体だけがONのとき、アプリのOFFを読み取れないのが今回の不具合。
            assertEquals(false, service.currentMuted())
            repeat(3) {
                assertEquals(true, service.toggle())
                awaitState { appRuleIsActive() }
                assertEquals(true, service.currentMuted())
                assertEquals(false, service.toggle())
                awaitState { !appRuleIsActive() }
                assertEquals(false, service.currentMuted())
            }
            // アプリの解除で、本体側で有効にしたモードを変更しない。
            assertNotEquals(NotificationManager.INTERRUPTION_FILTER_ALL, manager.currentInterruptionFilter)
        } finally {
            gateway.setMutedByUser(originalAppMuted)
            if (originalFilter == NotificationManager.INTERRUPTION_FILTER_ALL) shell("cmd notification set_dnd off")
        }
    }

    @Test fun userCanReactivateAppRuleAfterItWasTurnedOffInSettings() {
        assertTrue(manager.isNotificationPolicyAccessGranted)
        val originalAppMuted = appRuleIsActive()
        try {
            gateway.setMutedByUser(true)
            awaitState { appRuleIsActive() }
            val (id, rule) = manager.automaticZenRules.entries.single()
            manager.setAutomaticZenRuleState(id, Condition(
                rule.conditionId, "OFF", Condition.STATE_FALSE, Condition.SOURCE_USER_ACTION
            ))
            awaitState { !appRuleIsActive() }
            assertEquals(false, service.currentMuted())
            assertEquals(true, service.toggle())
            awaitState { appRuleIsActive() }
            assertEquals(false, service.toggle())
            awaitState { !appRuleIsActive() }
        } finally {
            gateway.setMutedByUser(originalAppMuted)
        }
    }

    private fun appRuleIsActive(): Boolean = manager.automaticZenRules.any { (id, rule) ->
        rule.isEnabled && manager.getAutomaticZenRuleState(id) == Condition.STATE_TRUE
    }

    private fun shell(command: String) {
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command))
            .bufferedReader().use { it.readText() }
    }

    private fun awaitState(condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 5_000
        while (!condition() && SystemClock.elapsedRealtime() < deadline) SystemClock.sleep(50)
        assertTrue("通知状態の変更が反映されませんでした", condition())
    }
}

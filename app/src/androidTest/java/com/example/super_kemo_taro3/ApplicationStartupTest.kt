package com.example.super_kemo_taro3

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.super_kemo_taro3.app.BellApplication
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** 端末上でApplicationがBellApplicationであることと、MAIN/LAUNCHERがHomeActivityを対象とするaliasへ解決することを2件で検証する。 */
@RunWith(AndroidJUnit4::class)
class ApplicationStartupTest {
    // この2テストはAndroid端末・エミュレーターで実行する。
    @Test fun applicationUsesTheNewCompositionRoot() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.super_kemo_taro3", context.packageName)
        assertTrue(context.applicationContext is BellApplication)
    }

    @Test fun launcherAliasOpensHomeActivity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // 起動アイコンと同じ条件で検索し、エイリアスがホームにつながることを確認する。
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(context.packageName)
        @Suppress("DEPRECATION")
        val resolved = context.packageManager.resolveActivity(intent, 0)
        assertNotNull(resolved)
        assertEquals(
            "com.example.super_kemo_taro3.frontend.screens.HomeActivity",
            resolved?.activityInfo?.targetActivity
        )
    }
}


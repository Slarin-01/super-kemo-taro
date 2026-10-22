package com.example.super_kemo_taro3.frontend.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.super_kemo_taro3.R

enum class MainDestination { Home, Settings }

/** 下部バーを保持し、タブの本文だけをアニメーションで切り替える。 */
@Composable
fun MainScreenScaffold(
    destination: MainDestination,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    content: @Composable (MainDestination) -> Unit
) {
    val screenStateHolder = rememberSaveableStateHolder()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // システムバーの余白は外側のAppScreenですでに確保している。
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
                NavigationBarItem(
                    selected = destination == MainDestination.Home,
                    onClick = { if (destination != MainDestination.Home) onHome() },
                    icon = {
                        Icon(painterResource(R.drawable.ic_home), contentDescription = null)
                    },
                    label = { Text("ホーム") }
                )
                NavigationBarItem(
                    selected = destination == MainDestination.Settings,
                    onClick = { if (destination != MainDestination.Settings) onSettings() },
                    icon = {
                        Icon(painterResource(R.drawable.ic_settings), contentDescription = null)
                    },
                    label = { Text("設定") }
                )
            }
        }
    ) { contentPadding ->
        // 下部バーはAnimatedContentの外に置き、画面切替の影響を受けないようにする。
        AnimatedContent(
            targetState = destination,
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "Main screen content"
        ) { page ->
            // タブを離れてもスクロール位置などの表示状態を保持する。
            screenStateHolder.SaveableStateProvider(page.name) {
                content(page)
            }
        }
    }
}

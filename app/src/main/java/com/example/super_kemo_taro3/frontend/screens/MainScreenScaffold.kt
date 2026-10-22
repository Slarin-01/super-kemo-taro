package com.example.super_kemo_taro3.frontend.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.super_kemo_taro3.R

enum class MainDestination { Home, Settings }

/** 本文のスクロールに関係なく、ホームと設定へのナビゲーションを画面下部に固定する。 */
@Composable
fun MainScreenScaffold(
    destination: MainDestination,
    onHome: () -> Unit,
    onSettings: () -> Unit,
    content: @Composable () -> Unit
) {
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
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            content()
        }
    }
}

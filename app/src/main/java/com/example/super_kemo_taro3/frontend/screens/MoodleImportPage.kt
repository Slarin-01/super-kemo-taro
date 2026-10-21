/*
 * フロントエンド・画面: Moodleの履修取り込みを始め、接続結果を受け取る。
 * 画面は表示とブラウザを開く操作を担当する。取得と枠の保存はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.backend.service.MoodleImport
import com.example.super_kemo_taro3.frontend.viewmodel.MoodleImportViewModel
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory

// 1. 取り込み画面の起動と、Moodleから戻るURLの受け取り
/** メニューから開くと確認を出し、koto-bellスキームで戻るとトークンを渡して取り込みを続ける。 */
class MoodleImportActivity : AppCompatActivity() {
    private val model by lazy {
        ViewModelProvider(this, ScreenViewModelFactory {
            MoodleImportViewModel(appServices.settings, appServices.timetable)
        })[MoodleImportViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        model.onNeedsAlarmPermission = { openAlarmSettings() }
        consume(intent)
        setContent {
            AppScreen {
                MoodleImportScreen(
                    phase = model.phase,
                    message = model.message,
                    progress = model.progress,
                    onStart = ::start,
                    onRetry = model::retry,
                    onClose = ::finish
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        openAlarmSettings()
    }

    private fun consume(intent: Intent) {
        val url = intent.dataString ?: return
        if (url.startsWith("${MoodleImport.SCHEME}:")) model.acceptToken(url)
    }

    private fun start() {
        val url = model.launchUrl() ?: return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            finish()
        } catch (_: ActivityNotFoundException) {
            model.fail("ブラウザを開けません")
        }
    }

    private fun openAlarmSettings() {
        if (model.consumeAlarmSettings()) PermissionSettingsNavigator.openExactAlarmAccess(this)
    }
}

// 2. 確認・待機・結果の表示
/** 進行に応じて取り込み開始、やり直し、閉じるを出す。 */
@Composable
fun MoodleImportScreen(
    phase: MoodleImportViewModel.Phase,
    message: String,
    progress: Float?,
    onStart: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (phase == MoodleImportViewModel.Phase.Importing) {
            CircularProgressIndicator()
            if (progress == null) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
        Text(message)
        if (phase == MoodleImportViewModel.Phase.Confirm) {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("取り込む") }
        }
        if (phase == MoodleImportViewModel.Phase.Done) {
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("やり直す") }
        }
        if (phase != MoodleImportViewModel.Phase.Importing) {
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("閉じる") }
        }
    }
}

@Preview(name = "履修取り込み・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "履修取り込み・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MoodleImportScreenPreview() {
    AppScreen {
        MoodleImportScreen(
            phase = MoodleImportViewModel.Phase.Confirm,
            message = "岡大Moodleの履修科目で、ミュートする枠を置き換えます。",
            progress = null,
            onStart = {},
            onRetry = {},
            onClose = {}
        )
    }
}

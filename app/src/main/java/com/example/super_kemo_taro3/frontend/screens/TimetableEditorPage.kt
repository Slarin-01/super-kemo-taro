/*
 * フロントエンド・画面: 曜日・時限を選択する画面の起動とCompose表示。
 * 画面は表示と操作の通知を担当する。保存・通知制御・予約の判断はViewModelからサービスへ渡す。
 */
package com.example.super_kemo_taro3.frontend.screens

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.frontend.viewmodel.ScreenViewModelFactory
import com.example.super_kemo_taro3.frontend.viewmodel.TimetableEditorViewModel

// 1. 時間割編集の起動と操作の接続
/** TimetableEditorViewModelとTimetableEditorScreenを接続する。onCreate・onResumeで選択状態を更新する。 */
class TimetableEditorActivity : AppCompatActivity() {
    private val model by lazy {
        ViewModelProvider(this, ScreenViewModelFactory {
            TimetableEditorViewModel(appServices.timetable)
        })[TimetableEditorViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        model.refresh()
        setContent {
            AppScreen {
                TimetableEditorScreen(
                    blockedSlots = model.blockedSlots,
                    onSlotClick = { key ->
                        // 選択の保存・予約後、許可が不足している場合だけ設定画面を開く。
                        if (model.toggleSlot(key)) PermissionSettingsNavigator.openExactAlarmAccess(this)
                    },
                    onBack = ::finish
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 設定画面から戻った場合も、保存されている選択を読み直す。
        model.refresh()
    }
}

// 2. 時間割編集のCompose表示
/** 操作可能なTimetableGridと「メニューに戻る」を表示する。 */
@Composable
fun TimetableEditorScreen(blockedSlots: Set<String>, onSlotClick: (String) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimetableGrid(blockedSlots, onSlotClick)
        Button(onClick = onBack) { Text("メニューに戻る") }
    }
}

// 3. Android Studio用のライト・ダークプレビュー
/** 固定データで表示する。操作のコールバックは空にし、設定保存・通知制御・アラーム予約を実行しない。 */
@Preview(name = "時間割編集・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "時間割編集・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimetableEditorScreenPreview() {
    AppScreen {
        TimetableEditorScreen(
            blockedSlots = setOf("MON_1", "TUE_3", "FRI_5"),
            onSlotClick = {},
            onBack = {}
        )
    }
}


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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.super_kemo_taro3.app.appServices
import com.example.super_kemo_taro3.frontend.viewmodel.AppearanceUiState

/** 設定メニューから開くテーマ編集画面。保存済みの外観は全画面で共用する。 */
class ThemeEditorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppScreen {
                ThemeEditorScreen(
                    appearance = appServices.appearance.uiState,
                    onModeChange = { dark ->
                        appServices.appearance.selectMode(dark)
                        AppThemeController.apply(dark)
                    },
                    onColorChange = appServices.appearance::selectColor,
                    onBack = { finish() }
                )
            }
        }
    }
}

@Composable
fun ThemeEditorScreen(
    appearance: AppearanceUiState,
    onModeChange: (Boolean) -> Unit,
    onColorChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("設定に戻る") }
        Text("テーマを編集", style = MaterialTheme.typography.headlineSmall)
        AppearanceSettingsCard(appearance, onModeChange, onColorChange)
    }
}

@Preview(name = "テーマ編集・ライト", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "テーマ編集・ダーク", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ThemeEditorScreenPreview() {
    AppScreen {
        ThemeEditorScreen(AppearanceUiState(), {}, {}, {})
    }
}

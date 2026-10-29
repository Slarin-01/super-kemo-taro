package com.example.super_kemo_taro3.frontend.screens

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.super_kemo_taro3.R
import com.example.super_kemo_taro3.frontend.viewmodel.AppearanceUiState
import kotlin.math.roundToInt

private data class ThemeSwatch(val name: String, val color: Int)

private val themeSwatches = listOf(
    ThemeSwatch("ブルー", 0x1A73E8), ThemeSwatch("グレー", 0x6B6B6B),
    ThemeSwatch("ネイビー", 0x37639A), ThemeSwatch("スレート", 0x596B85),
    ThemeSwatch("シアン", 0x547D82), ThemeSwatch("ティール", 0x008577),
    ThemeSwatch("グリーン", 0x438536), ThemeSwatch("セージ", 0x697F61),
    ThemeSwatch("イエロー", 0xB29822), ThemeSwatch("オレンジ", 0xC16B24),
    ThemeSwatch("ブラウン", 0x8E6C59), ThemeSwatch("ピンク", 0xB35170),
    ThemeSwatch("ローズ", 0x9A747C), ThemeSwatch("マゼンタ", 0xA354A0),
    ThemeSwatch("パープル", 0x7E57C2)
)

/** ライト・ダークの2モードと4列パレット。色はタップで即時反映、カスタム色は確定時に反映する。 */
@Composable
fun AppearanceSettingsCard(
    state: AppearanceUiState,
    onModeChange: (Boolean) -> Unit,
    onColorChange: (Int) -> Unit
) {
    // 以前のデバイス設定や未設定時も、現在表示中の明暗を選択状態にする。
    val dark = state.darkMode ?: isSystemInDarkTheme()
    var showCustomColor by rememberSaveable { mutableStateOf(false) }
    val customSelected = themeSwatches.none { it.color == state.color }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("外観", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("表示モードとテーマカラー", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                Modifier.fillMaxWidth().clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(3.dp).selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(false to "ライト", true to "ダーク").forEach { (mode, label) ->
                    val selected = dark == mode
                    val background = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
                    val foreground = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    Row(
                        Modifier.weight(1f).heightIn(min = 48.dp).clip(CircleShape)
                            .background(background)
                            .selectable(selected, role = Role.RadioButton, onClick = { onModeChange(mode) })
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            painterResource(if (selected) R.drawable.ic_theme_check
                                else if (mode) R.drawable.ic_theme_dark else R.drawable.ic_theme_light),
                            contentDescription = null, tint = foreground, modifier = Modifier.size(16.dp)
                        )
                        Text(label, color = foreground, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    }
                }
            }
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                (themeSwatches + ThemeSwatch("カスタムカラー", state.color)).chunked(4).forEachIndexed { row, swatches ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        swatches.forEachIndexed { column, swatch ->
                            val custom = row == 3 && column == 3
                            ColorSwatch(
                                swatch = swatch,
                                selected = if (custom) customSelected else swatch.color == state.color,
                                custom = custom,
                                modifier = Modifier.weight(1f),
                                onClick = { if (custom) showCustomColor = true else onColorChange(swatch.color) }
                            )
                        }
                    }
                }
            }
            Text(
                "選んだ色をアプリ全体に適用します",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    if (showCustomColor) {
        CustomColorDialog(
            initialColor = state.color,
            onDismiss = { showCustomColor = false },
            onConfirm = { onColorChange(it); showCustomColor = false }
        )
    }
}

@Composable
private fun ColorSwatch(
    swatch: ThemeSwatch,
    selected: Boolean,
    custom: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    // 明暗どちらでも同じ色見本を表示し、変更後の配色を比較しやすくする。
    val colors = remember(swatch.color) { bellColorScheme(swatch.color, false) }
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier.aspectRatio(1f).clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest)
            .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape) else Modifier)
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = swatch.name },
        contentAlignment = Alignment.Center
    ) {
        if (custom) {
            Box(Modifier.fillMaxWidth(.78f).aspectRatio(1f).background(colors.primary, CircleShape),
                contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_theme_color), contentDescription = null,
                    tint = colors.onPrimary, modifier = Modifier.size(24.dp))
            }
        } else {
            Canvas(Modifier.fillMaxWidth(.78f).aspectRatio(1f).clip(CircleShape)) {
                drawRect(colors.primary, size = Size(size.width, size.height / 2))
                drawRect(colors.primaryContainer, topLeft = Offset(0f, size.height / 2),
                    size = Size(size.width / 2, size.height / 2))
                drawRect(colors.outlineVariant, topLeft = Offset(size.width / 2, size.height / 2),
                    size = Size(size.width / 2, size.height / 2))
            }
        }
        if (selected) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(5.dp).size(20.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painterResource(R.drawable.ic_theme_check), contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun CustomColorDialog(initialColor: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var hex by rememberSaveable { mutableStateOf("%06X".format(initialColor)) }
    val parsed = hex.takeIf { it.length == 6 }?.toIntOrNull(16)
    var preview by rememberSaveable { mutableIntStateOf(initialColor) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("カスタムカラー") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = Color(preview or 0xFF000000.toInt()), shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {}
                OutlinedTextField(
                    value = hex,
                    onValueChange = { value ->
                        val normalized = value.removePrefix("#").uppercase()
                        if (normalized.length <= 6 && normalized.all { it in "0123456789ABCDEF" }) {
                            hex = normalized
                            normalized.takeIf { it.length == 6 }?.toIntOrNull(16)?.let { preview = it }
                        }
                    },
                    label = { Text("カラーコード") },
                    prefix = { Text("#") },
                    supportingText = { Text("6桁の16進数（例：1A73E8）") },
                    isError = parsed == null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                listOf("赤" to 16, "緑" to 8, "青" to 0).forEach { (label, shift) ->
                    val value = (preview shr shift) and 0xFF
                    Column {
                        Text("$label  $value", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = value.toFloat(), valueRange = 0f..255f,
                            onValueChange = { next ->
                                preview = (preview and (0xFF shl shift).inv()) or (next.roundToInt() shl shift)
                                hex = "%06X".format(preview)
                            },
                            modifier = Modifier.semantics { contentDescription = "${label}の強さ" }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { parsed?.let(onConfirm) }, enabled = parsed != null) { Text("適用") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

@Preview(name = "外観・ライト", showBackground = true, widthDp = 360)
@Preview(name = "外観・ダーク", showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AppearanceSettingsPreview() {
    BellTheme(themeColor = 0xB2781A) {
        Surface {
            Box(Modifier.padding(16.dp)) {
                AppearanceSettingsCard(AppearanceUiState(color = 0xB2781A), {}, {})
            }
        }
    }
}

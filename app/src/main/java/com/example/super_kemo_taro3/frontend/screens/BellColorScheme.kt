package com.example.super_kemo_taro3.frontend.screens

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/** 選択色の色相から、文字と背景に十分な明暗差を持つライト・ダークの配色を作る。 */
fun bellColorScheme(rgb: Int, dark: Boolean): ColorScheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(rgb or 0xFF000000.toInt(), hsl)
    val hue = hsl[0]
    val saturation = hsl[1]
    fun tone(lightness: Float, chroma: Float = saturation) =
        Color(ColorUtils.HSLToColor(floatArrayOf(hue, chroma, lightness)))
    fun foreground(background: Color): Color {
        val argb = background.toArgb()
        return if (ColorUtils.calculateContrast(android.graphics.Color.WHITE, argb) >=
            ColorUtils.calculateContrast(android.graphics.Color.BLACK, argb)) Color.White else Color.Black
    }

    val primary = tone(if (dark) .78f else .36f)
    val container = tone(if (dark) .25f else .9f, saturation * .65f)
    val secondary = tone(if (dark) .76f else .36f, saturation * .3f)
    val secondaryContainer = tone(if (dark) .25f else .9f, saturation * .2f)
    val surface = tone(if (dark) .075f else .985f, saturation * .18f)
    val onSurface = tone(if (dark) .91f else .12f, saturation * .16f)
    val onContainer = tone(if (dark) .94f else .12f, saturation * .35f)
    val scheme = if (dark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = primary,
        onPrimary = foreground(primary),
        primaryContainer = container,
        onPrimaryContainer = onContainer,
        inversePrimary = tone(if (dark) .36f else .78f),
        secondary = secondary,
        onSecondary = foreground(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onContainer,
        tertiary = secondary,
        onTertiary = foreground(secondary),
        tertiaryContainer = secondaryContainer,
        onTertiaryContainer = onContainer,
        background = surface,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = tone(if (dark) .22f else .9f, saturation * .15f),
        onSurfaceVariant = tone(if (dark) .77f else .3f, saturation * .12f),
        surfaceTint = primary,
        inverseSurface = tone(if (dark) .92f else .18f, saturation * .12f),
        inverseOnSurface = tone(if (dark) .18f else .95f, saturation * .12f),
        outline = tone(if (dark) .59f else .47f, saturation * .12f),
        outlineVariant = tone(if (dark) .29f else .78f, saturation * .12f),
        surfaceDim = tone(if (dark) .075f else .87f, saturation * .18f),
        surfaceBright = tone(if (dark) .24f else .985f, saturation * .18f),
        surfaceContainerLowest = tone(if (dark) .045f else 1f, saturation * .18f),
        surfaceContainerLow = tone(if (dark) .11f else .96f, saturation * .18f),
        surfaceContainer = tone(if (dark) .135f else .94f, saturation * .18f),
        surfaceContainerHigh = tone(if (dark) .17f else .92f, saturation * .18f),
        surfaceContainerHighest = tone(if (dark) .22f else .9f, saturation * .18f)
    )
}

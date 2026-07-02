package com.qingqiuyue.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 清秋月品牌色(与 iOS 端 BrandColor + 前端 MUI 主题保持一致)
 */
object BrandColor {
    val Primary = Color(0xFFFE2C55)      // #FE2C55
    val Secondary = Color(0xFF25F4EE)    // #25F4EE
    val Accent = Color(0xFF8B5CF6)       // #8B5CF6
    val Surface = Color(0xFFFAFAFA)
    val OnSurface = Color(0xFF1A1A1A)
    val Outline = Color(0xFFE5E5E5)
}

private val LightColors = lightColorScheme(
    primary = BrandColor.Primary,
    secondary = BrandColor.Secondary,
    tertiary = BrandColor.Accent,
    background = Color.White,
    surface = BrandColor.Surface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = BrandColor.OnSurface,
    onSurface = BrandColor.OnSurface,
)

private val DarkColors = darkColorScheme(
    primary = BrandColor.Primary,
    secondary = BrandColor.Secondary,
    tertiary = BrandColor.Accent,
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1A1A1A),
    onPrimary = Color.White,
    onSecondary = Color.Black,
)

@Composable
fun QingqiuyueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
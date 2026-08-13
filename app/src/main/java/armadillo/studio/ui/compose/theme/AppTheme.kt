/*
 * Copyright (c) 2021. Armadillo
 * Material3 主题入口（浅色 + 深色）
 */

package armadillo.studio.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 浅色 ColorScheme，使用项目品牌色。
 *
 * 颜色常量来自 [Color.kt]（从 XML 资源迁移）：
 *   - Primary        = #1A237E
 *   - PrimaryVariant = #283593
 *   - Accent         = #FF6F00
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = Color.White,
    inversePrimary = Accent,
    secondary = PrimaryVariant,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE1FF),
    onSecondaryContainer = Color(0xFF001257),
    tertiary = Accent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC8),
    onTertiaryContainer = Color(0xFF552100),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Primary,
    inverseSurface = Color(0xFF121212),
    inverseOnSurface = Color.White,
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = OutlineColor,
    outlineVariant = OutlineVariant,
    scrim = ScrimColor,
)

/**
 * 深色 ColorScheme，使用加亮的品牌色以适配深色背景。
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF0020A1),
    primaryContainer = Primary,
    onPrimaryContainer = Color.White,
    inversePrimary = Accent,
    secondary = Color(0xFFC0C6FF),
    onSecondary = Color(0xFF000F5F),
    secondaryContainer = PrimaryVariant,
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFFFB68C),
    onTertiary = Color(0xFF552100),
    tertiaryContainer = Color(0xFF7A3500),
    onTertiaryContainer = Color(0xFFFFDCC8),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceTint = Color(0xFFB4C5FF),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000),
)

/**
 * 应用主题入口。
 *
 * 根据系统深色模式自动切换 [LightColorScheme] / [DarkColorScheme]。
 * 字体样式 [AppTypography] 定义于 [Type.kt]，形状 [AppShapes] 定义于 [Shape.kt]。
 *
 * @param darkTheme 是否使用深色方案（默认跟随系统设置）
 * @param content    受主题包裹的 Compose 内容
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}

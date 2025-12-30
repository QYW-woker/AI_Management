package com.lifemanager.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 卡通风格浅色主题配色
 */
private val CartoonLightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.OnPrimaryContainer,

    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.OnSecondaryContainer,

    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.TertiaryContainer,
    onTertiaryContainer = AppColors.OnTertiaryContainer,

    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.OnErrorContainer,

    background = AppColors.Background,
    onBackground = AppColors.OnBackground,
    surface = AppColors.Surface,
    onSurface = AppColors.OnSurface,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.OnSurfaceVariant,

    outline = Color(0xFFB0A0C0),
    outlineVariant = Color(0xFFE0D8EA)
)

/**
 * 卡通风格深色主题配色
 */
private val CartoonDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8A0E8),
    onPrimary = Color(0xFF2D1F5B),
    primaryContainer = Color(0xFF4A3B7C),
    onPrimaryContainer = Color(0xFFEDE7FB),

    secondary = Color(0xFF8FE0CF),
    onSecondary = Color(0xFF0D3D34),
    secondaryContainer = Color(0xFF2A5A50),
    onSecondaryContainer = Color(0xFFD7F5EE),

    tertiary = Color(0xFFFFCBA4),
    onTertiary = Color(0xFF3D2314),
    tertiaryContainer = Color(0xFF6D4A30),
    onTertiaryContainer = Color(0xFFFFE8D9),

    error = Color(0xFFFF9B9B),
    onError = Color(0xFF5C1F1F),
    errorContainer = Color(0xFF8B3030),
    onErrorContainer = Color(0xFFFFE5E5),

    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkOnBackground,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkOnSurface,
    surfaceVariant = AppColors.DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD0C8DA),

    outline = Color(0xFF6A5A7A),
    outlineVariant = Color(0xFF4A4058)
)

/**
 * AI智能生活管理APP主题 - 卡通风格
 *
 * @param darkTheme 是否使用深色主题
 * @param dynamicColor 是否使用动态颜色（Android 12+）
 * @param content 内容
 */
@Composable
fun LifeManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 默认关闭以保持可爱风格一致性
    content: @Composable () -> Unit
) {
    // 选择颜色方案
    val colorScheme = when {
        // Android 12+ 支持动态颜色
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CartoonDarkColorScheme
        else -> CartoonLightColorScheme
    }

    // 设置状态栏和导航栏颜色
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 设置状态栏颜色为透明
            window.statusBarColor = Color.Transparent.toArgb()
            // 设置状态栏图标颜色（浅色/深色）
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = CartoonShapes,
        typography = AppTypography,
        content = content
    )
}

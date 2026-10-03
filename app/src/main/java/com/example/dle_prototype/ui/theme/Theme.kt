package com.example.dle_prototype.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.dle_prototype.data.UserSettings
import com.example.dle_prototype.data.UserSettingsManager

data class CustomThemeColors(
    val background: Color,
    val foreground: Color,
    val accent: Color,
    val buttonClicked: Color,
    val onBackground: Color,
    val onForeground: Color,
    val onButton: Color
)

val LocalCustomTheme = staticCompositionLocalOf {
    CustomThemeColors(
        background = DarkBackground,
        foreground = DarkSurface,
        accent = CyanAccent,
        buttonClicked = IndigoPrimaryLight,
        onBackground = DarkOnBackground,
        onForeground = DarkOnSurface,
        onButton = Color.White
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = DarkOnBackground,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkOnSurface,
    secondary = CyanAccent,
    onSecondary = DarkBackground,
    tertiary = AmberAccent,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = LightBackground,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = LightOnSurface,
    secondary = TealAccent,
    onSecondary = LightBackground,
    tertiary = AmberAccent,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightSurfaceBorder
)

@Composable
fun DLETheme(
    userSettings: UserSettings? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val (colorScheme, customColors) = if (userSettings != null) {
        val bg = UserSettingsManager.parseHexColor(userSettings.backgroundColorHex, DarkBackground)
        val fg = UserSettingsManager.parseHexColor(userSettings.foregroundColorHex, DarkSurface)
        val accent = UserSettingsManager.parseHexColor(userSettings.accentColorHex, CyanAccent)
        val btn = UserSettingsManager.parseHexColor(userSettings.buttonColorHex, IndigoPrimaryLight)

        val onBg = UserSettingsManager.getContrastingTextColor(bg)
        val onFg = UserSettingsManager.getContrastingTextColor(fg)
        val onAccent = UserSettingsManager.getContrastingTextColor(accent)
        val onBtn = UserSettingsManager.getContrastingTextColor(btn)

        val scheme = darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = btn,
            onPrimaryContainer = onBtn,
            secondary = accent,
            onSecondary = onAccent,
            tertiary = AmberAccent,
            background = bg,
            onBackground = onBg,
            surface = fg,
            onSurface = onFg,
            surfaceVariant = fg,
            onSurfaceVariant = UserSettingsManager.getSecondaryContrastingTextColor(fg),
            outline = if (onFg == Color.White) Color(0xFF334155) else Color(0xFFCBD5E1)
        )
        val custom = CustomThemeColors(
            background = bg,
            foreground = fg,
            accent = accent,
            buttonClicked = btn,
            onBackground = onBg,
            onForeground = onFg,
            onButton = onBtn
        )
        Pair(scheme, custom)
    } else {
        val scheme = when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }
        val custom = CustomThemeColors(
            background = if (darkTheme) DarkBackground else LightBackground,
            foreground = if (darkTheme) DarkSurface else LightSurface,
            accent = if (darkTheme) CyanAccent else TealAccent,
            buttonClicked = if (darkTheme) IndigoPrimaryLight else IndigoPrimary,
            onBackground = if (darkTheme) DarkOnBackground else LightOnBackground,
            onForeground = if (darkTheme) DarkOnSurface else LightOnSurface,
            onButton = Color.White
        )
        Pair(scheme, custom)
    }

    CompositionLocalProvider(LocalCustomTheme provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

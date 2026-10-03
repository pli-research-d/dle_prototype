package com.example.dle_prototype.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.notifications.DailyStreakReminderManager

data class PredefinedTheme(
    val id: String,
    val name: String,
    val description: String,
    val backgroundColorHex: String,
    val foregroundColorHex: String,
    val accentColorHex: String,
    val buttonColorHex: String
)

data class AvatarThemePreset(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val primaryColorHex: String,
    val secondaryColorHex: String
)

data class UserSettings(
    val displayName: String = "",
    val avatarEmoji: String = "🧑‍💻",
    val avatarThemeId: String = "cyber_spark",
    val avatarThemeName: String = "Cyber Spark",
    val primaryColorHex: String = "#00F5FF",
    val secondaryColorHex: String = "#6366F1",
    val backgroundColorHex: String = "#0B0F19",
    val foregroundColorHex: String = "#151D2F",
    val accentColorHex: String = "#00F5FF",
    val buttonColorHex: String = "#6366F1",
    val themePresetId: String = "midnight_cyber",
    val dailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val achievementAlertsEnabled: Boolean = true,
    val weeklyInsightsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

object UserSettingsManager {

    val PREDEFINED_THEMES = listOf(
        PredefinedTheme(
            id = "midnight_cyber",
            name = "Midnight Cyber",
            description = "Deep navy canvas with neon cyan and electric indigo accents",
            backgroundColorHex = "#0B0F19",
            foregroundColorHex = "#151D2F",
            accentColorHex = "#00F5FF",
            buttonColorHex = "#6366F1"
        ),
        PredefinedTheme(
            id = "emerald_matrix",
            name = "Emerald Matrix",
            description = "Lush botanical dark green with vibrant mint and jade",
            backgroundColorHex = "#061A14",
            foregroundColorHex = "#0E2D22",
            accentColorHex = "#10B981",
            buttonColorHex = "#059669"
        ),
        PredefinedTheme(
            id = "solar_flare",
            name = "Solar Flare",
            description = "Warm terracotta with golden amber highlights and flame energy",
            backgroundColorHex = "#1A1108",
            foregroundColorHex = "#2C1E10",
            accentColorHex = "#F59E0B",
            buttonColorHex = "#EF4444"
        ),
        PredefinedTheme(
            id = "royal_amethyst",
            name = "Royal Amethyst",
            description = "Majestic midnight violet with electric lavender and purple",
            backgroundColorHex = "#13091F",
            foregroundColorHex = "#241438",
            accentColorHex = "#C084FC",
            buttonColorHex = "#8B5CF6"
        ),
        PredefinedTheme(
            id = "crimson_neon",
            name = "Crimson Neon",
            description = "Obsidian dark with vibrant rose and ruby glow",
            backgroundColorHex = "#18080C",
            foregroundColorHex = "#2B1218",
            accentColorHex = "#F43F5E",
            buttonColorHex = "#BE123C"
        ),
        PredefinedTheme(
            id = "nordic_frost",
            name = "Nordic Frost",
            description = "Crisp light theme with ice white card and deep sapphire",
            backgroundColorHex = "#F8FAFC",
            foregroundColorHex = "#FFFFFF",
            accentColorHex = "#0284C7",
            buttonColorHex = "#0369A1"
        ),
        PredefinedTheme(
            id = "high_contrast",
            name = "High Contrast",
            description = "Ultra-accessible pure monochrome black and stark white",
            backgroundColorHex = "#000000",
            foregroundColorHex = "#18181B",
            accentColorHex = "#FFFFFF",
            buttonColorHex = "#3F3F46"
        ),
        PredefinedTheme(
            id = "deep_ocean",
            name = "Deep Ocean",
            description = "Abyssal blue with bright sky cyan and royal blue buttons",
            backgroundColorHex = "#031726",
            foregroundColorHex = "#072740",
            accentColorHex = "#38BDF8",
            buttonColorHex = "#2563EB"
        )
    )

    val SWATCHES_BACKGROUND = listOf("#0B0F19", "#000000", "#061A14", "#1A1108", "#13091F", "#18080C", "#031726", "#F8FAFC")
    val SWATCHES_FOREGROUND = listOf("#151D2F", "#18181B", "#0E2D22", "#2C1E10", "#241438", "#2B1218", "#072740", "#FFFFFF")
    val SWATCHES_ACCENT = listOf("#00F5FF", "#10B981", "#F59E0B", "#C084FC", "#F43F5E", "#38BDF8", "#E11D48", "#FFFFFF")
    val SWATCHES_BUTTON = listOf("#6366F1", "#059669", "#EF4444", "#8B5CF6", "#BE123C", "#2563EB", "#D97706", "#3F3F46")

    fun parseHexColor(hex: String, fallback: Color = Color.Unspecified): Color {
        return try {
            val clean = hex.trim().removePrefix("#")
            when (clean.length) {
                6 -> Color(android.graphics.Color.parseColor("#$clean"))
                8 -> Color(android.graphics.Color.parseColor("#$clean"))
                else -> fallback
            }
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Calculates the WCAG relative luminance to return high-contrast readable text.
     * Guarantees contrast on light or dark customized backgrounds.
     */
    fun getContrastingTextColor(backgroundColor: Color): Color {
        val luminance = 0.2126f * backgroundColor.red + 0.7152f * backgroundColor.green + 0.0722f * backgroundColor.blue
        return if (luminance > 0.45f) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    }

    fun getSecondaryContrastingTextColor(backgroundColor: Color): Color {
        val luminance = 0.2126f * backgroundColor.red + 0.7152f * backgroundColor.green + 0.0722f * backgroundColor.blue
        return if (luminance > 0.45f) Color(0xFF475569) else Color(0xFF94A3B8)
    }

    val AVATAR_PRESETS = listOf(
        AvatarThemePreset("cyber_spark", "Cyber Spark", "⚡", "Electric cybernetic energy", "#00F5FF", "#6366F1"),
        AvatarThemePreset("neuro_scholar", "Neuro Scholar", "🧠", "Deep synthetic cognition", "#A855F7", "#EC4899"),
        AvatarThemePreset("quantum_pioneer", "Quantum Pioneer", "🚀", "Space exploration & velocity", "#3B82F6", "#06B6D4"),
        AvatarThemePreset("emerald_sage", "Emerald Sage", "🌿", "Organic wisdom & balance", "#10B981", "#059669"),
        AvatarThemePreset("solaris_radiant", "Solaris Radiant", "☀️", "Golden solar power", "#F59E0B", "#EF4444"),
        AvatarThemePreset("tech_fox", "Tech Fox", "🦊", "Agile & intuitive developer", "#F97316", "#DC2626"),
        AvatarThemePreset("deep_owl", "Deep Owl", "🦉", "Nocturnal focus & strategy", "#8B5CF6", "#4F46E5"),
        AvatarThemePreset("crystal_prism", "Crystal Prism", "💎", "Clarity, logic & precision", "#06B6D4", "#3B82F6")
    )

    val AVATAR_EMOJIS = listOf("🧑‍💻", "🧠", "⚡", "🚀", "🦉", "🦊", "💎", "🎯", "🌌", "🧬", "🛡️", "🤖", "🔥", "🔮")

    private fun getPrefs(context: Context, username: String): SharedPreferences {
        return context.getSharedPreferences("user_settings_$username", Context.MODE_PRIVATE)
    }

    fun loadSettings(context: Context, username: String): UserSettings {
        val prefs = getPrefs(context, username)
        val defaultPreset = AVATAR_PRESETS[0]
        val savedThemeId = prefs.getString("avatar_theme_id", defaultPreset.id) ?: defaultPreset.id
        val preset = AVATAR_PRESETS.find { it.id == savedThemeId } ?: defaultPreset

        val defaultTheme = PREDEFINED_THEMES[0]
        val savedThemePresetId = prefs.getString("theme_preset_id", defaultTheme.id) ?: defaultTheme.id
        val matchingTheme = PREDEFINED_THEMES.find { it.id == savedThemePresetId } ?: defaultTheme

        val streakEnabled = DailyStreakReminderManager.isReminderEnabled(context)
        val (sHour, sMin) = DailyStreakReminderManager.getReminderTime(context)

        val defaultDisplayName = prefs.getString("display_name", null) ?: username

        return UserSettings(
            displayName = defaultDisplayName,
            avatarEmoji = prefs.getString("avatar_emoji", preset.emoji) ?: preset.emoji,
            avatarThemeId = preset.id,
            avatarThemeName = preset.name,
            primaryColorHex = prefs.getString("primary_color_hex", preset.primaryColorHex) ?: preset.primaryColorHex,
            secondaryColorHex = prefs.getString("secondary_color_hex", preset.secondaryColorHex) ?: preset.secondaryColorHex,
            backgroundColorHex = prefs.getString("background_color_hex", matchingTheme.backgroundColorHex) ?: matchingTheme.backgroundColorHex,
            foregroundColorHex = prefs.getString("foreground_color_hex", matchingTheme.foregroundColorHex) ?: matchingTheme.foregroundColorHex,
            accentColorHex = prefs.getString("accent_color_hex", matchingTheme.accentColorHex) ?: matchingTheme.accentColorHex,
            buttonColorHex = prefs.getString("button_color_hex", matchingTheme.buttonColorHex) ?: matchingTheme.buttonColorHex,
            themePresetId = savedThemePresetId,
            dailyReminderEnabled = streakEnabled,
            reminderHour = sHour,
            reminderMinute = sMin,
            achievementAlertsEnabled = prefs.getBoolean("achievement_alerts_enabled", true),
            weeklyInsightsEnabled = prefs.getBoolean("weekly_insights_enabled", true),
            hapticFeedbackEnabled = prefs.getBoolean("haptic_feedback_enabled", true)
        )
    }

    fun saveSettings(context: Context, username: String, settings: UserSettings) {
        val prefs = getPrefs(context, username)
        prefs.edit()
            .putString("display_name", settings.displayName)
            .putString("avatar_emoji", settings.avatarEmoji)
            .putString("avatar_theme_id", settings.avatarThemeId)
            .putString("avatar_theme_name", settings.avatarThemeName)
            .putString("primary_color_hex", settings.primaryColorHex)
            .putString("secondary_color_hex", settings.secondaryColorHex)
            .putString("background_color_hex", settings.backgroundColorHex)
            .putString("foreground_color_hex", settings.foregroundColorHex)
            .putString("accent_color_hex", settings.accentColorHex)
            .putString("button_color_hex", settings.buttonColorHex)
            .putString("theme_preset_id", settings.themePresetId)
            .putBoolean("achievement_alerts_enabled", settings.achievementAlertsEnabled)
            .putBoolean("weekly_insights_enabled", settings.weeklyInsightsEnabled)
            .putBoolean("haptic_feedback_enabled", settings.hapticFeedbackEnabled)
            .apply()

        // Sync streak reminder with DailyStreakReminderManager
        DailyStreakReminderManager.setReminderEnabled(context, settings.dailyReminderEnabled)
        DailyStreakReminderManager.setReminderTime(context, settings.reminderHour, settings.reminderMinute)
    }
}

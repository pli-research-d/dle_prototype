package com.example.dle_prototype.data

import android.content.Context
import android.content.SharedPreferences
import com.example.dle_prototype.notifications.DailyStreakReminderManager

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
    val dailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val achievementAlertsEnabled: Boolean = true,
    val weeklyInsightsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)

object UserSettingsManager {
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
            .putBoolean("achievement_alerts_enabled", settings.achievementAlertsEnabled)
            .putBoolean("weekly_insights_enabled", settings.weeklyInsightsEnabled)
            .putBoolean("haptic_feedback_enabled", settings.hapticFeedbackEnabled)
            .apply()

        // Sync streak reminder with DailyStreakReminderManager
        DailyStreakReminderManager.setReminderEnabled(context, settings.dailyReminderEnabled)
        DailyStreakReminderManager.setReminderTime(context, settings.reminderHour, settings.reminderMinute)
    }
}

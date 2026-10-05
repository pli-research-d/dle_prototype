package com.example.dle_prototype.data.badges

import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.IndigoPrimaryLight
import com.example.dle_prototype.ui.theme.RoseAccent

enum class BadgeCategory(val displayName: String) {
    STREAK_MILESTONE("Streak Milestones"),
    HIGH_VOLUME_STUDY("High-Volume Study"),
    MASTERY("Mastery & Focus")
}

enum class BadgeTier(
    val label: String,
    val colorHex: String,
    val primaryColor: Color
) {
    BRONZE("Bronze", "#CD7F32", Color(0xFFCD7F32)),
    SILVER("Silver", "#C0C0C0", Color(0xFFC0C0C0)),
    GOLD("Gold", "#FFD700", Color(0xFFFFD700)),
    PLATINUM("Platinum", "#E2E8F0", Color(0xFFE2E8F0)),
    DIAMOND("Diamond", "#38BDF8", Color(0xFF38BDF8))
}

data class DigitalBadge(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val tier: BadgeTier,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null,
    val currentProgress: Float,
    val targetProgress: Float,
    val progressFraction: Float, // 0.0f..1.0f
    val progressLabel: String,
    val xpReward: Int,
    val rarityText: String
)

data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: BadgeCategory,
    val tier: BadgeTier,
    val iconEmoji: String,
    val targetValue: Float,
    val xpReward: Int,
    val rarityText: String,
    val unitLabel: String
)

object BadgeCatalog {

    val ALL_DEFINITIONS: List<BadgeDefinition> = listOf(
        // -----------------------------------------------------------------
        // 1. LEARNING STREAK MILESTONES
        // -----------------------------------------------------------------
        BadgeDefinition(
            id = "streak_1d",
            title = "Streak Spark",
            description = "Ignite your daily learning habit with your first active study day.",
            category = BadgeCategory.STREAK_MILESTONE,
            tier = BadgeTier.BRONZE,
            iconEmoji = "⚡",
            targetValue = 1f,
            xpReward = 50,
            rarityText = "Common · Day 1",
            unitLabel = "Day"
        ),
        BadgeDefinition(
            id = "streak_3d",
            title = "3-Day Flame",
            description = "Maintain a 3-day consecutive study streak without breaking the chain.",
            category = BadgeCategory.STREAK_MILESTONE,
            tier = BadgeTier.SILVER,
            iconEmoji = "🔥",
            targetValue = 3f,
            xpReward = 100,
            rarityText = "Uncommon · 3 Days",
            unitLabel = "Days"
        ),
        BadgeDefinition(
            id = "streak_5d",
            title = "5-Day Momentum",
            description = "Power through 5 days in a row of active study and practice.",
            category = BadgeCategory.STREAK_MILESTONE,
            tier = BadgeTier.SILVER,
            iconEmoji = "🚀",
            targetValue = 5f,
            xpReward = 175,
            rarityText = "Rare · 5 Days",
            unitLabel = "Days"
        ),
        BadgeDefinition(
            id = "streak_7d",
            title = "7-Day Inferno",
            description = "Complete an entire 7-day week of uninterrupted daily learning.",
            category = BadgeCategory.STREAK_MILESTONE,
            tier = BadgeTier.GOLD,
            iconEmoji = "🌋",
            targetValue = 7f,
            xpReward = 300,
            rarityText = "Epic · Full Week",
            unitLabel = "Days"
        ),
        BadgeDefinition(
            id = "streak_14d",
            title = "14-Day Titan",
            description = "Sustain a 14-day study streak across two unbroken weeks of learning.",
            category = BadgeCategory.STREAK_MILESTONE,
            tier = BadgeTier.PLATINUM,
            iconEmoji = "⚔️",
            targetValue = 14f,
            xpReward = 600,
            rarityText = "Legendary · Top 10%",
            unitLabel = "Days"
        ),
        BadgeDefinition(
            id = "streak_30d",
            title = "30-Day Legend",
            description = "Reach a legendary 30-day streak of daily cognitive commitment.",
            category = BadgeCategory.STREAK_MILESTONE,
            tier = BadgeTier.DIAMOND,
            iconEmoji = "👑",
            targetValue = 30f,
            xpReward = 1500,
            rarityText = "Mythic · Top 2%",
            unitLabel = "Days"
        ),

        // -----------------------------------------------------------------
        // 2. HIGH-VOLUME STUDY SESSIONS
        // -----------------------------------------------------------------
        BadgeDefinition(
            id = "session_endurance_25m",
            title = "Endurance Scholar",
            description = "Complete a single 25-minute deep focus study session.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.BRONZE,
            iconEmoji = "⏱️",
            targetValue = 25f,
            xpReward = 75,
            rarityText = "Common · Pomodoro Block",
            unitLabel = "Mins"
        ),
        BadgeDefinition(
            id = "session_deep_45m",
            title = "Deep Focus Marathoner",
            description = "Log an unbroken 45+ minute study session without excessive pauses.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.SILVER,
            iconEmoji = "🏃",
            targetValue = 45f,
            xpReward = 150,
            rarityText = "Rare · Extended Focus",
            unitLabel = "Mins"
        ),
        BadgeDefinition(
            id = "session_master_60m",
            title = "Cognitive Master",
            description = "Complete a high-volume 60+ minute study session with >= 80% focus score.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.PLATINUM,
            iconEmoji = "🧠",
            targetValue = 60f,
            xpReward = 400,
            rarityText = "Epic · 60m Deep Work",
            unitLabel = "Mins"
        ),
        BadgeDefinition(
            id = "volume_100_questions",
            title = "Century Grinder",
            description = "Solve 100 questions across quiz challenges and study sessions.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.GOLD,
            iconEmoji = "💯",
            targetValue = 100f,
            xpReward = 250,
            rarityText = "Epic · 100 Questions",
            unitLabel = "Questions"
        ),
        BadgeDefinition(
            id = "volume_250_questions",
            title = "Study Machine",
            description = "Answer 250+ questions across study and practice sessions.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.DIAMOND,
            iconEmoji = "🤖",
            targetValue = 250f,
            xpReward = 750,
            rarityText = "Mythic · 250 Questions",
            unitLabel = "Questions"
        ),
        BadgeDefinition(
            id = "volume_2hr_day",
            title = "High-Volume Day",
            description = "Complete 2.0+ hours of learning activity in a single day.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.GOLD,
            iconEmoji = "⭐",
            targetValue = 2.0f,
            xpReward = 350,
            rarityText = "Rare · 2h Study Day",
            unitLabel = "Hours"
        ),
        BadgeDefinition(
            id = "volume_total_5hr",
            title = "5-Hour Monument",
            description = "Accumulate 300+ total minutes (5 hours) of focused study sessions.",
            category = BadgeCategory.HIGH_VOLUME_STUDY,
            tier = BadgeTier.PLATINUM,
            iconEmoji = "🏛️",
            targetValue = 300f,
            xpReward = 500,
            rarityText = "Legendary · 5 Hours Total",
            unitLabel = "Mins"
        ),

        // -----------------------------------------------------------------
        // 3. MASTERY & SPECIAL ACHIEVEMENTS
        // -----------------------------------------------------------------
        BadgeDefinition(
            id = "mastery_flawless",
            title = "Flawless Perfection",
            description = "Score 100% on a quiz session with zero incorrect answers.",
            category = BadgeCategory.MASTERY,
            tier = BadgeTier.GOLD,
            iconEmoji = "🎯",
            targetValue = 1f,
            xpReward = 200,
            rarityText = "Epic · 100% Accuracy",
            unitLabel = "Quiz"
        ),
        BadgeDefinition(
            id = "mastery_early_bird",
            title = "Early Bird Focus",
            description = "Complete a study or quiz session between 5:00 AM and 9:00 AM.",
            category = BadgeCategory.MASTERY,
            tier = BadgeTier.BRONZE,
            iconEmoji = "🌅",
            targetValue = 1f,
            xpReward = 100,
            rarityText = "Common · Dawn Learner",
            unitLabel = "Session"
        ),
        BadgeDefinition(
            id = "mastery_night_owl",
            title = "Night Owl Grinder",
            description = "Complete a study or quiz session after 9:00 PM.",
            category = BadgeCategory.MASTERY,
            tier = BadgeTier.BRONZE,
            iconEmoji = "🦉",
            targetValue = 1f,
            xpReward = 100,
            rarityText = "Common · Night Study",
            unitLabel = "Session"
        )
    )

    fun getDefinition(id: String): BadgeDefinition? = ALL_DEFINITIONS.find { it.id == id }
}

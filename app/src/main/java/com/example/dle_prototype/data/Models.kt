package com.example.dle_prototype.data

data class User(
    val id: Long,
    val username: String,
    val createdAt: Long
)

data class Question(
    val type: String, // "mcq" or "true_false"
    val category: String,
    val difficulty: String,
    val question: String,
    val options: List<String> = emptyList(),
    val answer: Any, // String for MCQ, Boolean for True/False
    val explanation: String = "",
    val example: String = ""
)

data class QuizAttempt(
    val id: Long = 0,
    val username: String,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val difficultyLevel: Float,
    val timestamp: Long
)

data class LearningTelemetry(
    val loginCount: Int,
    val totalTimeMinutes: Float,
    val lastQuizScore: Float,
    val lastDifficultyReached: Float,
    val lastCategorySelected: Float
)

data class TraitScore(
    val name: String,
    val score: Float, // 0.0 to 1.0 (clamped for visual gauges)
    val rawValue: Float,
    val description: String,
    val levelLabel: String
)

data class PersonalizationProfile(
    val conscientiousness: TraitScore,
    val motivation: TraitScore,
    val understanding: TraitScore,
    val engagement: TraitScore,
    val primaryStrength: String,
    val focusArea: String,
    val tips: List<String>,
    val inferenceTimeMs: Long,
    val inferenceLatencyMs: Float = inferenceTimeMs.toFloat(),
    val isPersonalizedWeights: Boolean = false,
    val modelSource: String = "Baseline TFLite"
)

data class TraitSnapshot(
    val id: Long = 0,
    val username: String,
    val conscientiousness: Float,
    val motivation: Float,
    val understanding: Float,
    val engagement: Float,
    val primaryStrength: String,
    val focusArea: String,
    val modelSource: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FlashcardItem(
    val id: Long = 0,
    val username: String,
    val category: String,
    val question: String,
    val optionsJson: String = "",
    val correctAnswer: String,
    val explanation: String,
    val boxLevel: Int = 1, // Box 1 = 1 day, Box 2 = 3 days, Box 3 = 7 days, Box 4 = 14 days (Mastered)
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val reviewCount: Int = 0,
    val lastReviewedTimestamp: Long = 0
)

data class StudySessionRecord(
    val id: Long = 0,
    val username: String,
    val durationSeconds: Long,
    val targetMinutes: Int,
    val pauseCount: Int,
    val focusScore: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class DdaQuizState(
    val currentDifficulty: String = "Easy", // "Easy", "Medium", "Hard"
    val streak: Int = 0,
    val highestStreak: Int = 0,
    val levelChanges: Int = 0,
    val zpdZone: String = "Balanced Challenge" // "Scaffolding Needed", "Balanced Challenge (ZPD)", "Mastery Peak"
)

data class QuizPerformanceStats(
    val totalQuizzes: Int = 0,
    val totalQuestionsAnswered: Int = 0,
    val totalScore: Int = 0,
    val averageAccuracyPercent: Float = 0f,
    val perfectQuizzesCount: Int = 0,
    val highestScore: Int = 0,
    val earlyBirdQuizzesCount: Int = 0,
    val currentDailyStreak: Int = 0
)

enum class LeaderboardScope {
    GLOBAL,
    FRIENDS
}

enum class LeaderboardSort {
    STREAK,
    SCORE,
    ACCURACY
}

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val displayName: String,
    val avatarEmoji: String,
    val dailyStreak: Int,
    val totalQuizzes: Int,
    val totalScore: Int,
    val accuracyPercent: Float,
    val isCurrentUser: Boolean = false,
    val isFriend: Boolean = false,
    val dominantTrait: String = "Balanced",
    val tier: String = "Gold",
    val rankDelta: Int = 0 // positive: climbed, negative: dropped, 0: same
)

data class DailyGoalProgress(
    val targetQuestions: Int = 10,
    val answeredToday: Int = 0,
    val percentComplete: Float = 0f,
    val isAchieved: Boolean = false
)

data class CategoryMastery(
    val categoryName: String,
    val icon: String,
    val totalAttempts: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracyPercent: Float,
    val masteryLevel: String, // "Novice" (0-49%), "Practitioner" (50-74%), "Proficient" (75-89%), "Master" (90%+)
    val highestDifficultyReached: Float,
    val statusBadge: String // "Needs Reinforcement", "Solidifying", "Mastered", "Untested"
)

data class LearnerArchetype(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val description: String,
    val cognitiveSuperpower: String,
    val suggestedPacing: String,
    val optimalStudyWindow: String
)

data class PersonalizedActionPlan(
    val archetype: LearnerArchetype,
    val recommendedCategory: String,
    val recommendedCategoryNumber: Float,
    val recommendationReason: String,
    val recommendedCategoryIcon: String,
    val recommendedInitialTier: String, // "Easy", "Medium", "Hard"
    val dueFlashcardsCount: Int,
    val categoryMasteries: List<CategoryMastery>,
    val optimalTimeWindow: String,
    val cognitiveReadinessScore: Float // 0.0 to 1.0
)

data class UserPerformance(
    val id: Long = 0,
    val username: String,
    val quizScore: Float,
    val timeSpentMinutes: Float,
    val loginFrequency: Float,
    val difficultyReached: Float,
    val categorySelected: Float,
    val retentionRate: Float = 0.85f,
    val learningVelocity: Float = 1.0f,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class QuizHistory(
    val id: Long = 0,
    val username: String,
    val category: String,
    val score: Int,
    val totalQuestions: Int,
    val accuracyPercent: Float = if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100f else 0f,
    val elapsedTimeSeconds: Int = 0,
    val missedQuestionsIndicesJson: String = "[]",
    val difficultyLevel: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserStreakData(
    val username: String,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val lastActiveDate: String = "",
    val totalDaysActive: Int = 1,
    val lastMilestoneReached: Int = 0,
    val isStreakActiveToday: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

typealias StreakData = UserStreakData

enum class FeedCardType {
    MCQ,
    SPOT_THE_BUG,
    WHATS_THE_OUTPUT,
    FILL_BLANK,
    EXPLAINER_15S,
    REVIEW_CARD,
    MYSTERY_CARD
}

data class FeedCard(
    val id: String,
    val type: FeedCardType,
    val category: String,
    val title: String,
    val prompt: String,
    val codeSnippet: String = "",
    val bugLineIndex: Int = -1, // 0-indexed line with the bug if SPOT_THE_BUG
    val bugLines: List<String> = emptyList(),
    val options: List<String> = emptyList(),
    val correctAnswer: String,
    val explanation: String,
    val xpReward: Int = 12,
    val fuelReward: Int = 3,
    val isMystery: Boolean = false
)

data class UserGamificationState(
    val username: String,
    val fuel: Int = 85,
    val totalXp: Int = 340,
    val streakFreezes: Int = 1,
    val altitudeKm: Float = 2.4f,
    val selectedSkin: String = "Apollo Standard",
    val selectedTheme: String = "Deep Space",
    val selectedFrame: String = "Bronze Orbit",
    val onboardingCompleted: Boolean = true,
    val selectedTopics: List<String> = listOf("HTML", "CSS", "JavaScript", "Python"),
    val selectedSkillLevel: String = "Surprise me",
    val dailyGoalQuestions: Int = 10,
    val currentLeague: String = "Silver",
    val leagueRank: Int = 5,
    val unopenedChestsCount: Int = 1
)

data class MysteryChest(
    val id: Long = 0,
    val username: String,
    val chestType: String, // "Daily Streak", "Mystery Drop", "Duel Victory", "Orbit Milestone"
    val isOpened: Boolean = false,
    val rewardXp: Int = 50,
    val rewardFuel: Int = 20,
    val rewardBadge: String = "",
    val rewardSkin: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class RewardHistoryItem(
    val id: Long = 0,
    val username: String,
    val title: String,
    val description: String,
    val fuelChange: Int = 0,
    val xpChange: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class DuelMatchRecord(
    val id: Long = 0,
    val username: String,
    val opponentName: String,
    val opponentAvatar: String,
    val opponentTier: String = "Gold",
    val category: String,
    val userScore: Int,
    val opponentScore: Int,
    val isWin: Boolean,
    val xpEarned: Int,
    val fuelEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class WellbeingSettings(
    val username: String,
    val dailyTimeCapMinutes: Int = 0, // 0 = off, 15, 30, 45, 60
    val quietHoursEnabled: Boolean = false,
    val quietStartHour: Int = 22,
    val quietEndHour: Int = 7,
    val softStopReminderEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val learningStyle: String = "Adaptive Dynamic" // "Adaptive Dynamic", "Gentle Paced", "High Challenge"
)

data class SeasonTier(
    val level: Int,
    val xpRequired: Int,
    val freeReward: String,
    val bonusReward: String,
    val isUnlocked: Boolean = false,
    val isClaimed: Boolean = false
)

data class SquadMember(
    val username: String,
    val displayName: String,
    val avatarEmoji: String,
    val weeklyXp: Int,
    val isLeader: Boolean = false
)






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


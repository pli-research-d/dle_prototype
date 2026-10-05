package com.example.dle_prototype.data.badges

import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.StudySessionRecord
import java.util.Calendar

data class BadgeEvaluationResult(
    val allBadges: List<DigitalBadge>,
    val newlyAwardedBadges: List<DigitalBadge>,
    val totalUnlockedCount: Int,
    val totalBadgesCount: Int,
    val totalXpAwardedFromBadges: Int
)

object BadgeSystemEngine {

    /**
     * Evaluates all badge definitions against user progress data and previously unlocked badge records.
     * Returns the evaluated badges along with any newly unlocked badges that should trigger celebratory awards.
     */
    fun evaluate(
        dailyStreak: Int,
        longestStreak: Int,
        quizAttempts: List<QuizAttempt>,
        studySessions: List<StudySessionRecord>,
        dailyGoalProgress: DailyGoalProgress,
        previouslyUnlockedMap: Map<String, Long> // badge_id -> unlocked_at timestamp
    ): BadgeEvaluationResult {
        val newlyAwarded = mutableListOf<DigitalBadge>()
        val evaluatedBadges = mutableListOf<DigitalBadge>()

        val effectiveStreak = maxOf(dailyStreak, longestStreak)
        val totalQuestionsAnswered = quizAttempts.sumOf { it.totalQuestions }
        val maxSingleSessionMinutes = studySessions.maxOfOrNull { it.durationSeconds / 60.0f } ?: 0f
        val totalFocusMinutes = studySessions.sumOf { it.durationSeconds } / 60.0f
        val cal = Calendar.getInstance()

        for (def in BadgeCatalog.ALL_DEFINITIONS) {
            val isPreviouslyUnlocked = previouslyUnlockedMap.containsKey(def.id)
            val unlockedTimestamp = previouslyUnlockedMap[def.id]

            var currentVal = 0f
            var isCriteriaMet = false

            when (def.id) {
                // Streak milestones
                "streak_1d" -> {
                    currentVal = effectiveStreak.toFloat()
                    isCriteriaMet = effectiveStreak >= 1
                }
                "streak_3d" -> {
                    currentVal = effectiveStreak.toFloat()
                    isCriteriaMet = effectiveStreak >= 3
                }
                "streak_5d" -> {
                    currentVal = effectiveStreak.toFloat()
                    isCriteriaMet = effectiveStreak >= 5
                }
                "streak_7d" -> {
                    currentVal = effectiveStreak.toFloat()
                    isCriteriaMet = effectiveStreak >= 7
                }
                "streak_14d" -> {
                    currentVal = effectiveStreak.toFloat()
                    isCriteriaMet = effectiveStreak >= 14
                }
                "streak_30d" -> {
                    currentVal = effectiveStreak.toFloat()
                    isCriteriaMet = effectiveStreak >= 30
                }

                // High-volume study sessions
                "session_endurance_25m" -> {
                    currentVal = maxSingleSessionMinutes
                    isCriteriaMet = maxSingleSessionMinutes >= 25f
                }
                "session_deep_45m" -> {
                    currentVal = maxSingleSessionMinutes
                    isCriteriaMet = maxSingleSessionMinutes >= 45f
                }
                "session_master_60m" -> {
                    val maxFocused60m = studySessions.any { (it.durationSeconds / 60.0f) >= 60f && it.focusScore >= 0.80f }
                    currentVal = if (maxFocused60m) 60f else maxSingleSessionMinutes.coerceAtMost(60f)
                    isCriteriaMet = maxFocused60m
                }
                "volume_100_questions" -> {
                    currentVal = totalQuestionsAnswered.toFloat()
                    isCriteriaMet = totalQuestionsAnswered >= 100
                }
                "volume_250_questions" -> {
                    currentVal = totalQuestionsAnswered.toFloat()
                    isCriteriaMet = totalQuestionsAnswered >= 250
                }
                "volume_2hr_day" -> {
                    currentVal = dailyGoalProgress.hoursCompletedToday
                    isCriteriaMet = dailyGoalProgress.hoursCompletedToday >= 2.0f
                }
                "volume_total_5hr" -> {
                    currentVal = totalFocusMinutes
                    isCriteriaMet = totalFocusMinutes >= 300f
                }

                // Mastery & Focus
                "mastery_flawless" -> {
                    val hasPerfect = quizAttempts.any { it.totalQuestions >= 5 && it.score == it.totalQuestions }
                    currentVal = if (hasPerfect) 1f else 0f
                    isCriteriaMet = hasPerfect
                }
                "mastery_early_bird" -> {
                    val hasEarly = (quizAttempts.map { it.timestamp } + studySessions.map { it.timestamp }).any { ts ->
                        cal.timeInMillis = ts
                        val hour = cal.get(Calendar.HOUR_OF_DAY)
                        hour in 5..8
                    }
                    currentVal = if (hasEarly) 1f else 0f
                    isCriteriaMet = hasEarly
                }
                "mastery_night_owl" -> {
                    val hasNight = (quizAttempts.map { it.timestamp } + studySessions.map { it.timestamp }).any { ts ->
                        cal.timeInMillis = ts
                        val hour = cal.get(Calendar.HOUR_OF_DAY)
                        hour >= 21 || hour < 4
                    }
                    currentVal = if (hasNight) 1f else 0f
                    isCriteriaMet = hasNight
                }
            }

            val finalUnlocked = isPreviouslyUnlocked || isCriteriaMet
            val finalTimestamp = unlockedTimestamp ?: if (isCriteriaMet) System.currentTimeMillis() else null

            val fraction = if (finalUnlocked) 1.0f else (currentVal / def.targetValue).coerceIn(0f, 1f)
            val progressLabel = if (finalUnlocked) {
                "Unlocked"
            } else {
                when (def.unitLabel) {
                    "Hours" -> "${String.format(java.util.Locale.US, "%.1f", currentVal)} / ${def.targetValue.toInt()}h"
                    else -> "${currentVal.toInt()} / ${def.targetValue.toInt()} ${def.unitLabel}"
                }
            }

            val digitalBadge = DigitalBadge(
                id = def.id,
                title = def.title,
                description = def.description,
                category = def.category,
                tier = def.tier,
                iconEmoji = def.iconEmoji,
                isUnlocked = finalUnlocked,
                unlockedAt = finalTimestamp,
                currentProgress = currentVal,
                targetProgress = def.targetValue,
                progressFraction = fraction,
                progressLabel = progressLabel,
                xpReward = def.xpReward,
                rarityText = def.rarityText
            )

            evaluatedBadges.add(digitalBadge)

            if (!isPreviouslyUnlocked && isCriteriaMet) {
                newlyAwarded.add(digitalBadge)
            }
        }

        val totalUnlocked = evaluatedBadges.count { it.isUnlocked }
        val totalXp = evaluatedBadges.filter { it.isUnlocked }.sumOf { it.xpReward }

        return BadgeEvaluationResult(
            allBadges = evaluatedBadges,
            newlyAwardedBadges = newlyAwarded,
            totalUnlockedCount = totalUnlocked,
            totalBadgesCount = evaluatedBadges.size,
            totalXpAwardedFromBadges = totalXp
        )
    }
}

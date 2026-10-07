package com.example.dle_prototype.data.ml

import androidx.compose.ui.graphics.Color
import com.example.dle_prototype.data.CategoryMastery
import com.example.dle_prototype.data.FlashcardItem
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.ui.theme.AmberAccent
import com.example.dle_prototype.ui.theme.CyanAccent
import com.example.dle_prototype.ui.theme.EmeraldSuccess
import com.example.dle_prototype.ui.theme.RoseAccent
import java.util.Locale
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class UrgencyTier(
    val label: String,
    val colorHex: String,
    val badgeColor: Color
) {
    CRITICAL_OVERDUE("Review Critical", "#F43F5E", RoseAccent),
    DUE_TODAY("Review Due Today", "#F59E0B", AmberAccent),
    SOLIDIFYING("Solidifying", "#00F5FF", CyanAccent),
    MASTERED("Retained · Mastered", "#10B981", EmeraldSuccess)
}

data class TopicReviewSchedule(
    val topicId: String,
    val topicName: String,
    val categoryNumber: Float,
    val iconEmoji: String,
    val description: String,
    val urgencyTier: UrgencyTier,
    val priorityScore: Float, // 0.0 to 100.0 (Higher = review sooner)
    val retentionProbability: Float, // 0.0 to 1.0 (Estimated memory retention)
    val scheduledIntervalDays: Float, // Optimal next review interval
    val hoursSinceLastReview: Float,
    val lastReviewedTimestamp: Long,
    val nextReviewTimestamp: Long,
    val isOverdue: Boolean,
    val dueStatusText: String, // e.g. "Overdue by 1.5 days", "Due in 2 days"
    val historicalAccuracyPercent: Float,
    val totalAttempts: Int,
    val reasonExplanation: String,
    val stabilityFactorDays: Float
)

data class SpacedRepetitionOverview(
    val prioritizedQueue: List<TopicReviewSchedule>,
    val dueNowCount: Int,
    val averageRetentionPercent: Int,
    val mostUrgentTopic: TopicReviewSchedule?,
    val algorithmSource: String = "Half-Life Memory Decay & SM-2 Spaced Repetition"
)

/**
 * Spaced Repetition Scheduling Algorithm.
 * Implements Ebbinghaus Forgetting Curve and SuperMemo SM-2 principles to prioritize
 * learning materials and quiz topics based on past performance, intervals, and decay rates.
 */
object SpacedRepetitionScheduler {

    data class TopicMeta(
        val name: String,
        val categoryNumber: Float,
        val icon: String,
        val description: String
    )

    val CORE_TOPICS = listOf(
        TopicMeta("JavaScript", 3f, "⚡", "Modern asynchronous JS, event loop, closures & web APIs"),
        TopicMeta("Python", 6f, "🐍", "Data structures, algorithms, list comprehensions & OOP"),
        TopicMeta("HTML", 1f, "🌐", "Semantic structure, modern HTML5, DOM architecture & accessibility"),
        TopicMeta("CSS", 2f, "🎨", "CSS Grid, Flexbox, layout systems, animations & responsive rules"),
        TopicMeta("MySQL", 5f, "🐬", "Relational querying, indexing, transactions, joins & DB design"),
        TopicMeta("PHP", 4f, "🐘", "Server-side web scripting, request lifecycle & templates"),
        TopicMeta("C Language", 7f, "⚙️", "Memory management, pointers, manual heap allocation & struct padding")
    )

    /**
     * Schedules and prioritizes all topics based on user's past quiz attempts and flashcards.
     */
    fun scheduleTopics(
        quizAttempts: List<QuizAttempt>,
        flashcards: List<FlashcardItem> = emptyList(),
        currentTimeMillis: Long = System.currentTimeMillis()
    ): SpacedRepetitionOverview {
        val attemptsByTopic = quizAttempts.groupBy { it.category.trim() }
        val flashcardsByTopic = flashcards.groupBy { it.category.trim() }

        val schedules = CORE_TOPICS.map { meta ->
            val attempts = attemptsByTopic[meta.name] ?: emptyList()
            val cards = flashcardsByTopic[meta.name] ?: emptyList()
            calculateScheduleForTopic(meta, attempts, cards, currentTimeMillis)
        }

        // Sort by priorityScore descending (highest urgency first)
        val sortedQueue = schedules.sortedByDescending { it.priorityScore }
        val dueNow = sortedQueue.count { it.isOverdue || it.urgencyTier == UrgencyTier.CRITICAL_OVERDUE || it.urgencyTier == UrgencyTier.DUE_TODAY }
        val avgRetention = if (sortedQueue.isNotEmpty()) {
            (sortedQueue.map { it.retentionProbability }.average() * 100).roundToInt()
        } else 100

        return SpacedRepetitionOverview(
            prioritizedQueue = sortedQueue,
            dueNowCount = dueNow,
            averageRetentionPercent = avgRetention,
            mostUrgentTopic = sortedQueue.firstOrNull()
        )
    }

    /**
     * Calculates the memory decay, stability factor, and urgency priority score for a single topic.
     */
    fun calculateScheduleForTopic(
        meta: TopicMeta,
        attempts: List<QuizAttempt>,
        cards: List<FlashcardItem>,
        currentTimeMillis: Long
    ): TopicReviewSchedule {
        val totalAttempts = attempts.size

        if (totalAttempts == 0 && cards.isEmpty()) {
            // Topic has never been practiced: High priority to establish baseline
            return TopicReviewSchedule(
                topicId = "topic_${meta.name.lowercase(Locale.US)}",
                topicName = meta.name,
                categoryNumber = meta.categoryNumber,
                iconEmoji = meta.icon,
                description = meta.description,
                urgencyTier = UrgencyTier.DUE_TODAY,
                priorityScore = 75.0f,
                retentionProbability = 0.50f,
                scheduledIntervalDays = 1.0f,
                hoursSinceLastReview = 0f,
                lastReviewedTimestamp = 0L,
                nextReviewTimestamp = currentTimeMillis,
                isOverdue = true,
                dueStatusText = "Initial Review Due",
                historicalAccuracyPercent = 0f,
                totalAttempts = 0,
                reasonExplanation = "Untested topic. Review to calibrate baseline memory and cognitive profile.",
                stabilityFactorDays = 1.0f
            )
        }

        // 1. Calculate Historical Performance (Weighted toward recent sessions)
        val sortedAttempts = attempts.sortedBy { it.timestamp }
        val lastAttempt = sortedAttempts.lastOrNull()
        val lastReviewedTimestamp = lastAttempt?.timestamp ?: cards.maxOfOrNull { it.lastReviewedTimestamp } ?: currentTimeMillis

        val hoursSince = max(0f, (currentTimeMillis - lastReviewedTimestamp).toFloat() / (1000f * 60f * 60f))
        val daysSince = hoursSince / 24.0f

        // Average accuracy with exponential recency weighting
        var weightedAccuracySum = 0f
        var weightSum = 0f
        for (i in sortedAttempts.indices) {
            val a = sortedAttempts[i]
            val acc = if (a.totalQuestions > 0) (a.score.toFloat() / a.totalQuestions.toFloat()) * 100f else 0f
            val weight = 1.0f + (i.toFloat() * 0.35f) // Recent attempts have higher weight
            weightedAccuracySum += acc * weight
            weightSum += weight
        }
        val accuracyPercent = if (weightSum > 0f) weightedAccuracySum / weightSum else 50f

        // 2. SM-2 Stability Factor Calculation
        // Stability represents the time in days required for memory retention to drop to 90%
        // S = Base * (Easiness Factor) ^ (repetition count)
        val easinessFactor = (1.3f + (accuracyPercent / 100f) * 1.2f).coerceIn(1.3f, 2.5f)
        val baseStability = when {
            accuracyPercent >= 90f -> 4.0f
            accuracyPercent >= 75f -> 2.5f
            accuracyPercent >= 60f -> 1.5f
            else -> 0.8f // Poor accuracy collapses stability
        }

        val repetitionFactor = min(totalAttempts, 6).toFloat()
        val stabilityDays = (baseStability * (1.0f + (repetitionFactor * 0.5f) * (easinessFactor - 1.0f))).coerceIn(0.5f, 30.0f)

        // 3. Ebbinghaus Memory Retention Decay Curve: R(t) = exp(-t / S)
        // With half-life normalization
        val retentionProbability = exp(-daysSince / stabilityDays).coerceIn(0.05f, 1.0f)

        // 4. Scheduled Next Review Timestamp
        val scheduledIntervalDays = max(1.0f, stabilityDays * 0.85f)
        val scheduledIntervalMillis = (scheduledIntervalDays * 24f * 60f * 60f * 1000f).toLong()
        val nextReviewTimestamp = lastReviewedTimestamp + scheduledIntervalMillis
        val isOverdue = currentTimeMillis >= nextReviewTimestamp

        // 5. Composite Urgency Priority Score (0..100)
        // Factors:
        // - Memory Decay (lower retention = higher priority): up to 45 pts
        // - Accuracy Deficit (lower accuracy = higher priority): up to 35 pts
        // - Overdue Lateness: up to 20 pts
        val decayPoints = (1.0f - retentionProbability) * 45.0f
        val accuracyDeficitPoints = ((100.0f - accuracyPercent) / 100.0f) * 35.0f
        val overduePoints = if (isOverdue) {
            val overdueDays = daysSince - scheduledIntervalDays
            (overdueDays * 6.0f).coerceIn(5.0f, 20.0f)
        } else 0.0f

        val priorityScore = (decayPoints + accuracyDeficitPoints + overduePoints).coerceIn(5.0f, 100.0f)

        // 6. Urgency Tier & Reason Explanation
        val urgencyTier = when {
            isOverdue && (retentionProbability < 0.65f || priorityScore >= 70f) -> UrgencyTier.CRITICAL_OVERDUE
            isOverdue || retentionProbability < 0.78f || priorityScore >= 50f -> UrgencyTier.DUE_TODAY
            retentionProbability < 0.90f || priorityScore >= 30f -> UrgencyTier.SOLIDIFYING
            else -> UrgencyTier.MASTERED
        }

        val dueStatusText = when {
            isOverdue -> {
                val overdueHours = (hoursSince - (scheduledIntervalDays * 24f)).roundToInt()
                if (overdueHours >= 24) {
                    val days = (overdueHours / 24f)
                    String.format(Locale.US, "Overdue by %.1fd", days)
                } else {
                    "Overdue by ${max(1, overdueHours)}h"
                }
            }
            hoursSince < 1f -> "Reviewed Just Now"
            else -> {
                val hoursRemaining = ((scheduledIntervalDays * 24f) - hoursSince).roundToInt()
                if (hoursRemaining >= 24) {
                    val days = (hoursRemaining / 24f)
                    String.format(Locale.US, "Due in %.1fd", days)
                } else {
                    "Due in ${max(1, hoursRemaining)}h"
                }
            }
        }

        val reason = when {
            urgencyTier == UrgencyTier.CRITICAL_OVERDUE ->
                "Accuracy dropped to ${accuracyPercent.toInt()}%. Memory retention has decayed to ${(retentionProbability * 100).toInt()}%. Immediate review recommended."
            urgencyTier == UrgencyTier.DUE_TODAY && isOverdue ->
                "Optimal spaced repetition window reached. Review now to solidify synaptic connections."
            urgencyTier == UrgencyTier.DUE_TODAY ->
                "Memory retention approaching review threshold (${(retentionProbability * 100).toInt()}%). Short quiz recommended today."
            urgencyTier == UrgencyTier.SOLIDIFYING ->
                "Concept stability is growing (${stabilityDays.roundToInt()}d interval). On track for next review."
            else ->
                "High retention (${(retentionProbability * 100).toInt()}%) across $totalAttempts sessions. Memory strongly consolidated."
        }

        return TopicReviewSchedule(
            topicId = "topic_${meta.name.lowercase(Locale.US)}",
            topicName = meta.name,
            categoryNumber = meta.categoryNumber,
            iconEmoji = meta.icon,
            description = meta.description,
            urgencyTier = urgencyTier,
            priorityScore = priorityScore,
            retentionProbability = retentionProbability,
            scheduledIntervalDays = scheduledIntervalDays,
            hoursSinceLastReview = hoursSince,
            lastReviewedTimestamp = lastReviewedTimestamp,
            nextReviewTimestamp = nextReviewTimestamp,
            isOverdue = isOverdue,
            dueStatusText = dueStatusText,
            historicalAccuracyPercent = accuracyPercent,
            totalAttempts = totalAttempts,
            reasonExplanation = reason,
            stabilityFactorDays = stabilityDays
        )
    }
}

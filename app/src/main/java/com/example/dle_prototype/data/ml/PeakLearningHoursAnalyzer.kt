package com.example.dle_prototype.data.ml

import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.StudySessionRecord
import java.util.Calendar
import java.util.Locale

/**
 * Hourly cognitive metrics representing user activity, accuracy, and focus during a specific hour.
 */
data class HourlyCognitiveStats(
    val hour: Int, // 0..23
    val sessionCount: Int = 0,
    val totalQuestions: Int = 0,
    val totalDurationMinutes: Float = 0f,
    val averageAccuracyPercent: Float = 0f,
    val compositeFocusScore: Float = 0f
)

/**
 * Detailed analysis of user's historical peak learning hours identified from progress data.
 */
data class PeakLearningHoursAnalysis(
    val peakHour: Int, // 0..23
    val peakHourFormatted: String, // e.g. "10:00 AM"
    val peakWindowFormatted: String, // e.g. "10:00 AM - 11:00 AM"
    val secondaryPeakHour: Int? = null,
    val secondaryPeakHourFormatted: String? = null,
    val sessionCountAtPeak: Int = 0,
    val totalSessionsAnalyzed: Int = 0,
    val accuracyAtPeakPercent: Float = 0f,
    val overallAccuracyPercent: Float = 0f,
    val hourlyDistribution: Map<Int, Int> = emptyMap(), // hour -> session count
    val hourlyAccuracy: Map<Int, Float> = emptyMap(), // hour -> average accuracy %
    val confidenceScore: Float = 0.2f, // 0.0f..1.0f
    val recommendationMessage: String = "",
    val timeOfDayLabel: String = "Morning Focus",
    val isCalibrated: Boolean = false
)

object PeakLearningHoursAnalyzer {

    // Default benchmark peak hour when history is minimal (10:00 AM)
    const val DEFAULT_PEAK_HOUR = 10

    /**
     * Analyzes historical quiz attempts and study sessions to identify the user's
     * peak learning hours based on frequency, accuracy, and focus metrics.
     */
    fun analyze(
        quizAttempts: List<QuizAttempt>,
        studySessions: List<StudySessionRecord> = emptyList()
    ): PeakLearningHoursAnalysis {
        val totalSessions = quizAttempts.size + studySessions.size

        // If no progress data exists, return default recommendation
        if (totalSessions == 0) {
            val defaultHour = DEFAULT_PEAK_HOUR
            val formatted = formatHour(defaultHour)
            val window = formatWindow(defaultHour)
            return PeakLearningHoursAnalysis(
                peakHour = defaultHour,
                peakHourFormatted = formatted,
                peakWindowFormatted = window,
                secondaryPeakHour = 19,
                secondaryPeakHourFormatted = formatHour(19),
                sessionCountAtPeak = 0,
                totalSessionsAnalyzed = 0,
                accuracyAtPeakPercent = 85f,
                overallAccuracyPercent = 85f,
                hourlyDistribution = (0..23).associateWith { 0 },
                hourlyAccuracy = (0..23).associateWith { 0f },
                confidenceScore = 0.2f,
                recommendationMessage = "Initial study recommendation set for $formatted based on optimal cognitive retention times. Complete sessions to personalize your schedule.",
                timeOfDayLabel = getTimeOfDayLabel(defaultHour),
                isCalibrated = false
            )
        }

        // Buckets for 24 hours
        val sessionCountByHour = IntArray(24)
        val accuracySumByHour = FloatArray(24)
        val accuracyCountByHour = IntArray(24)
        val durationMinsByHour = FloatArray(24)
        val questionsByHour = IntArray(24)

        var totalAccuracySum = 0f
        var totalAccuracyCount = 0

        val cal = Calendar.getInstance()

        // 1. Process Quiz Attempts
        for (attempt in quizAttempts) {
            cal.timeInMillis = attempt.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
            val accuracy = if (attempt.totalQuestions > 0) {
                (attempt.score.toFloat() / attempt.totalQuestions.toFloat() * 100f).coerceIn(0f, 100f)
            } else 75f

            sessionCountByHour[hour]++
            accuracySumByHour[hour] += accuracy
            accuracyCountByHour[hour]++
            questionsByHour[hour] += attempt.totalQuestions
            durationMinsByHour[hour] += attempt.totalQuestions * 1.5f // ~1.5 mins active solving per question

            totalAccuracySum += accuracy
            totalAccuracyCount++
        }

        // 2. Process Study Sessions
        for (session in studySessions) {
            cal.timeInMillis = session.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY).coerceIn(0, 23)
            val focusAccuracy = (session.focusScore * 100f).coerceIn(0f, 100f)

            sessionCountByHour[hour]++
            accuracySumByHour[hour] += focusAccuracy
            accuracyCountByHour[hour]++
            durationMinsByHour[hour] += session.durationSeconds / 60.0f

            totalAccuracySum += focusAccuracy
            totalAccuracyCount++
        }

        val overallAccuracy = if (totalAccuracyCount > 0) {
            totalAccuracySum / totalAccuracyCount
        } else 80f

        // Compute composite score for each hour
        // Prioritize hours with high session counts AND high accuracy
        val hourlyStats = (0..23).map { h ->
            val count = sessionCountByHour[h]
            val avgAcc = if (accuracyCountByHour[h] > 0) {
                accuracySumByHour[h] / accuracyCountByHour[h]
            } else 0f
            val dur = durationMinsByHour[h]

            // Composite score: activity weight (2.5x) + accuracy weight (1.0x normalized) + duration (0.1x)
            val composite = (count * 2.5f) + (avgAcc * 0.08f) + (dur * 0.05f)

            HourlyCognitiveStats(
                hour = h,
                sessionCount = count,
                totalQuestions = questionsByHour[h],
                totalDurationMinutes = dur,
                averageAccuracyPercent = avgAcc,
                compositeFocusScore = composite
            )
        }

        // Find primary and secondary peak hours
        val sortedByScore = hourlyStats.filter { it.sessionCount > 0 }.sortedByDescending { it.compositeFocusScore }

        val peakStat = sortedByScore.firstOrNull() ?: hourlyStats[DEFAULT_PEAK_HOUR]
        val secondaryStat = sortedByScore.getOrNull(1)

        val peakHour = peakStat.hour
        val peakHourFormatted = formatHour(peakHour)
        val peakWindowFormatted = formatWindow(peakHour)

        val secondaryPeakHour = secondaryStat?.hour
        val secondaryPeakHourFormatted = secondaryPeakHour?.let { formatHour(it) }

        val accuracyAtPeak = if (peakStat.averageAccuracyPercent > 0f) {
            peakStat.averageAccuracyPercent
        } else overallAccuracy

        val confidence = ((totalSessions / 8.0f) * 0.8f + 0.2f).coerceIn(0.2f, 1.0f)
        val isCalibrated = totalSessions >= 3

        val recommendationMessage = if (isCalibrated) {
            val diff = (accuracyAtPeak - overallAccuracy).toInt()
            val diffText = if (diff > 0) " (+${diff}% higher than average)" else ""
            "Historically, your highest focus and retention occurs around $peakHourFormatted, where you average ${accuracyAtPeak.toInt()}% accuracy$diffText! We've scheduled your study recommendations for this window."
        } else {
            "Initial peak learning suggestion set for $peakHourFormatted based on cognitive retention benchmarks. Complete $totalSessions/3 sessions to calibrate to your unique daily rhythm."
        }

        val distributionMap = hourlyStats.associate { it.hour to it.sessionCount }
        val accuracyMap = hourlyStats.associate { it.hour to it.averageAccuracyPercent }

        return PeakLearningHoursAnalysis(
            peakHour = peakHour,
            peakHourFormatted = peakHourFormatted,
            peakWindowFormatted = peakWindowFormatted,
            secondaryPeakHour = secondaryPeakHour,
            secondaryPeakHourFormatted = secondaryPeakHourFormatted,
            sessionCountAtPeak = peakStat.sessionCount,
            totalSessionsAnalyzed = totalSessions,
            accuracyAtPeakPercent = accuracyAtPeak,
            overallAccuracyPercent = overallAccuracy,
            hourlyDistribution = distributionMap,
            hourlyAccuracy = accuracyMap,
            confidenceScore = confidence,
            recommendationMessage = recommendationMessage,
            timeOfDayLabel = getTimeOfDayLabel(peakHour),
            isCalibrated = isCalibrated
        )
    }

    fun formatHour(hour: Int): String {
        val h = hour.coerceIn(0, 23)
        return when {
            h == 0 -> "12:00 AM"
            h < 12 -> "$h:00 AM"
            h == 12 -> "12:00 PM"
            else -> "${h - 12}:00 PM"
        }
    }

    fun formatWindow(hour: Int): String {
        val start = formatHour(hour)
        val nextHour = (hour + 1) % 24
        val end = formatHour(nextHour)
        return "$start - $end"
    }

    fun getTimeOfDayLabel(hour: Int): String {
        return when (hour) {
            in 5..11 -> "Morning Focus (8 AM - 12 PM)"
            in 12..16 -> "Afternoon Boost (12 PM - 5 PM)"
            in 17..21 -> "Evening Prime (5 PM - 9 PM)"
            else -> "Late Night Session (9 PM - 4 AM)"
        }
    }
}

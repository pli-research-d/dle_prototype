package com.example.dle_prototype.data.export

import android.net.Uri
import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.QuizPerformanceStats
import com.example.dle_prototype.data.StudySessionRecord
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.badges.DigitalBadge
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalysis
import java.io.File

/**
 * Format options for learning data export.
 */
enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    PDF("PDF Document", "pdf", "application/pdf"),
    CSV("CSV Spreadsheet", "csv", "text/csv")
}

/**
 * Complete consolidated bundle of a user's daily learning summaries,
 * telemetry, focus sessions, and historical quiz performance for personal archiving.
 */
data class LearningExportBundle(
    val user: User,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val dailyStreak: Int = 0,
    val longestStreak: Int = 0,
    val dailyGoalProgress: DailyGoalProgress = DailyGoalProgress(),
    val quizPerformanceStats: QuizPerformanceStats = QuizPerformanceStats(),
    val quizAttempts: List<QuizAttempt> = emptyList(),
    val focusSessions: List<StudySessionRecord> = emptyList(),
    val peakLearningAnalysis: PeakLearningHoursAnalysis? = null,
    val digitalBadges: List<DigitalBadge> = emptyList(),
    val activeCategories: List<String> = emptyList()
)

/**
 * Result of an export operation containing the generated artifact reference.
 */
data class ExportResult(
    val success: Boolean,
    val file: File? = null,
    val uri: Uri? = null,
    val format: ExportFormat = ExportFormat.PDF,
    val summaryText: String = "",
    val errorMessage: String? = null
)

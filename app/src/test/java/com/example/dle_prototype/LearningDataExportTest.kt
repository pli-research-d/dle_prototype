package com.example.dle_prototype

import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.QuizPerformanceStats
import com.example.dle_prototype.data.StudySessionRecord
import com.example.dle_prototype.data.User
import com.example.dle_prototype.data.export.ExportFormat
import com.example.dle_prototype.data.export.LearningDataExporter
import com.example.dle_prototype.data.export.LearningExportBundle
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalysis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File

class LearningDataExportTest {

    private lateinit var testUser: User
    private lateinit var sampleBundle: LearningExportBundle

    @Before
    fun setUp() {
        testUser = User(
            id = 1,
            username = "test_scholar",
            createdAt = 1700000000000L
        )

        val attempts = listOf(
            QuizAttempt(
                id = 1,
                username = "test_scholar",
                category = "General Science",
                score = 8,
                totalQuestions = 10,
                difficultyLevel = 1.5f,
                timestamp = 1710000000000L
            ),
            QuizAttempt(
                id = 2,
                username = "test_scholar",
                category = "Machine Learning, Deep Learning",
                score = 10,
                totalQuestions = 10,
                difficultyLevel = 2.4f,
                timestamp = 1710003600000L
            )
        )

        val focusSessions = listOf(
            StudySessionRecord(
                id = 1,
                username = "test_scholar",
                durationSeconds = 1500, // 25 mins
                targetMinutes = 25,
                pauseCount = 1,
                focusScore = 0.95f,
                timestamp = 1710000000000L
            )
        )

        val peakAnalysis = PeakLearningHoursAnalysis(
            peakHour = 10,
            peakHourFormatted = "10:00 AM",
            peakWindowFormatted = "10:00 AM - 11:00 AM",
            sessionCountAtPeak = 5,
            totalSessionsAnalyzed = 10,
            accuracyAtPeakPercent = 90.0f,
            overallAccuracyPercent = 85.0f,
            recommendationMessage = "Maintain your morning study routine for maximum retention.",
            timeOfDayLabel = "Morning Focus",
            isCalibrated = true
        )

        sampleBundle = LearningExportBundle(
            user = testUser,
            exportTimestamp = 1710010000000L,
            dailyStreak = 7,
            longestStreak = 14,
            dailyGoalProgress = DailyGoalProgress(
                targetQuestions = 20,
                answeredToday = 18,
                percentComplete = 0.9f,
                isAchieved = false,
                targetHours = 1.5f,
                hoursCompletedToday = 1.2f,
                minutesCompletedToday = 72f
            ),
            quizPerformanceStats = QuizPerformanceStats(
                totalQuizzes = 15,
                totalQuestionsAnswered = 150,
                totalScore = 135,
                averageAccuracyPercent = 90f,
                highestScore = 10
            ),
            quizAttempts = attempts,
            focusSessions = focusSessions,
            peakLearningAnalysis = peakAnalysis
        )
    }

    @Test
    fun testGenerateCsvStructureAndContent() {
        val csv = LearningDataExporter.generateCsv(sampleBundle)
        assertNotNull(csv)
        assertTrue("CSV should not be blank", csv.isNotBlank())

        // Verify Header Metadata
        assertTrue("CSV must contain learner username", csv.contains("test_scholar"))
        assertTrue("CSV must contain active streak", csv.contains("7 days"))
        assertTrue("CSV must contain longest streak", csv.contains("14 days"))
        assertTrue("CSV must contain accuracy", csv.contains("90.0%"))

        // Verify Daily Learning Goals
        assertTrue("CSV must contain daily target questions", csv.contains("Target Questions,20"))
        assertTrue("CSV must contain answered questions", csv.contains("Answered Questions Today,18"))

        // Verify Peak Learning Hours
        assertTrue("CSV must contain peak window", csv.contains("10:00 AM - 11:00 AM"))
        assertTrue("CSV must contain peak accuracy rate", csv.contains("90.0%"))

        // Verify Study & Focus Sessions
        assertTrue("CSV must contain focus session duration", csv.contains("25"))
        assertTrue("CSV must contain focus score", csv.contains("95%"))

        // Verify Quiz Performance History
        assertTrue("CSV must contain General Science attempt", csv.contains("General Science"))
        assertTrue("CSV must escape category containing comma", csv.contains("\"Machine Learning, Deep Learning\""))
        assertTrue("CSV must include score", csv.contains(",8,10,"))
        assertTrue("CSV must include Hard tier classification", csv.contains("Hard"))
    }

    @Test
    fun testWriteToOutputStreamCsv() {
        val outStream = ByteArrayOutputStream()
        // We can test CSV output stream directly in JVM unit tests
        val csvContent = LearningDataExporter.generateCsv(sampleBundle)
        outStream.write(csvContent.toByteArray(Charsets.UTF_8))
        outStream.flush()

        val resultString = outStream.toString(Charsets.UTF_8.name())
        assertTrue(resultString.contains("test_scholar"))
        assertTrue(resultString.contains("QUIZ PERFORMANCE HISTORY"))
        assertTrue(resultString.contains("DAILY LEARNING GOAL"))
    }

    @Test
    fun testExportFormatsEnum() {
        assertEquals("pdf", ExportFormat.PDF.extension)
        assertEquals("application/pdf", ExportFormat.PDF.mimeType)
        assertEquals("csv", ExportFormat.CSV.extension)
        assertEquals("text/csv", ExportFormat.CSV.mimeType)
    }

    @Test
    fun testEmptyQuizHistoryCsvGeneration() {
        val emptyBundle = LearningExportBundle(
            user = testUser,
            quizAttempts = emptyList(),
            focusSessions = emptyList()
        )
        val csv = LearningDataExporter.generateCsv(emptyBundle)
        assertNotNull(csv)
        assertTrue(csv.contains("test_scholar"))
        assertTrue(csv.contains("QUIZ PERFORMANCE HISTORY"))
    }
}

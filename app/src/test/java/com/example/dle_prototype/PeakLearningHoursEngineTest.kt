package com.example.dle_prototype

import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.StudySessionRecord
import com.example.dle_prototype.data.ml.PeakLearningHoursAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PeakLearningHoursEngineTest {

    private fun createTimestampAtHour(hour: Int, daysAgo: Int = 0): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    @Test
    fun testDefaultFallbackWhenNoHistoryExists() {
        val analysis = PeakLearningHoursAnalyzer.analyze(emptyList(), emptyList())

        assertEquals(PeakLearningHoursAnalyzer.DEFAULT_PEAK_HOUR, analysis.peakHour)
        assertEquals("10:00 AM", analysis.peakHourFormatted)
        assertEquals("10:00 AM - 11:00 AM", analysis.peakWindowFormatted)
        assertFalse(analysis.isCalibrated)
        assertEquals(0, analysis.totalSessionsAnalyzed)
        assertEquals(24, analysis.hourlyDistribution.size)
        assertEquals(0.2f, analysis.confidenceScore, 0.001f)
        assertTrue(analysis.recommendationMessage.isNotEmpty())
    }

    @Test
    fun testPeakHourDetectionWithMorningAttempts() {
        val attempts = listOf(
            QuizAttempt(1L, "alice", "Kotlin", 10, 10, 2.0f, createTimestampAtHour(10, 1)),
            QuizAttempt(2L, "alice", "Android", 9, 10, 2.0f, createTimestampAtHour(10, 2)),
            QuizAttempt(3L, "alice", "Algorithms", 10, 10, 2.5f, createTimestampAtHour(10, 3)),
            QuizAttempt(4L, "alice", "Python", 6, 10, 1.5f, createTimestampAtHour(15, 1))
        )

        val analysis = PeakLearningHoursAnalyzer.analyze(attempts)

        assertEquals(10, analysis.peakHour)
        assertEquals("10:00 AM", analysis.peakHourFormatted)
        assertEquals("10:00 AM - 11:00 AM", analysis.peakWindowFormatted)
        assertEquals(3, analysis.sessionCountAtPeak)
        assertEquals(4, analysis.totalSessionsAnalyzed)
        assertTrue(analysis.isCalibrated)
        assertTrue(analysis.timeOfDayLabel.contains("Morning"))
        assertTrue(analysis.accuracyAtPeakPercent >= 90f)
        assertTrue(analysis.confidenceScore > 0.5f)
    }

    @Test
    fun testPeakHourDetectionWithEveningAttemptsAndStudySessions() {
        val attempts = listOf(
            QuizAttempt(1L, "bob", "Kotlin", 8, 10, 1.8f, createTimestampAtHour(19, 1)),
            QuizAttempt(2L, "bob", "Python", 9, 10, 2.0f, createTimestampAtHour(19, 2))
        )

        val studySessions = listOf(
            StudySessionRecord(1L, "bob", 1800L, 30, 0, 0.95f, createTimestampAtHour(19, 1)),
            StudySessionRecord(2L, "bob", 1200L, 20, 1, 0.90f, createTimestampAtHour(19, 3)),
            StudySessionRecord(3L, "bob", 600L, 10, 0, 0.70f, createTimestampAtHour(8, 2))
        )

        val analysis = PeakLearningHoursAnalyzer.analyze(attempts, studySessions)

        assertEquals(19, analysis.peakHour)
        assertEquals("7:00 PM", analysis.peakHourFormatted)
        assertEquals("7:00 PM - 8:00 PM", analysis.peakWindowFormatted)
        assertEquals(4, analysis.sessionCountAtPeak)
        assertEquals(5, analysis.totalSessionsAnalyzed)
        assertTrue(analysis.isCalibrated)
        assertTrue(analysis.timeOfDayLabel.contains("Evening"))
    }

    @Test
    fun testHourFormatting() {
        assertEquals("12:00 AM", PeakLearningHoursAnalyzer.formatHour(0))
        assertEquals("8:00 AM", PeakLearningHoursAnalyzer.formatHour(8))
        assertEquals("12:00 PM", PeakLearningHoursAnalyzer.formatHour(12))
        assertEquals("3:00 PM", PeakLearningHoursAnalyzer.formatHour(15))
        assertEquals("11:00 PM", PeakLearningHoursAnalyzer.formatHour(23))

        assertEquals("12:00 AM - 1:00 AM", PeakLearningHoursAnalyzer.formatWindow(0))
        assertEquals("2:00 PM - 3:00 PM", PeakLearningHoursAnalyzer.formatWindow(14))
        assertEquals("11:00 PM - 12:00 AM", PeakLearningHoursAnalyzer.formatWindow(23))
    }

    @Test
    fun testTimeOfDayLabels() {
        assertTrue(PeakLearningHoursAnalyzer.getTimeOfDayLabel(8).contains("Morning"))
        assertTrue(PeakLearningHoursAnalyzer.getTimeOfDayLabel(10).contains("Morning"))
        assertTrue(PeakLearningHoursAnalyzer.getTimeOfDayLabel(14).contains("Afternoon"))
        assertTrue(PeakLearningHoursAnalyzer.getTimeOfDayLabel(19).contains("Evening"))
        assertTrue(PeakLearningHoursAnalyzer.getTimeOfDayLabel(23).contains("Night"))
        assertTrue(PeakLearningHoursAnalyzer.getTimeOfDayLabel(2).contains("Night"))
    }

    @Test
    fun testNextAlarmCalculation() {
        val now = Calendar.getInstance()
        val targetHour = (now.get(Calendar.HOUR_OF_DAY) + 2) % 24

        val scheduledCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        assertTrue("Scheduled alarm time must be strictly in the future", scheduledCal.timeInMillis > now.timeInMillis)
    }
}

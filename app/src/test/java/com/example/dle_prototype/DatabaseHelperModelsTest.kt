package com.example.dle_prototype

import com.example.dle_prototype.data.QuizHistory
import com.example.dle_prototype.data.StreakData
import com.example.dle_prototype.data.UserPerformance
import com.example.dle_prototype.data.UserStreakData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseHelperModelsTest {

    @Test
    fun testUserPerformanceDataModel() {
        val now = System.currentTimeMillis()
        val perf = UserPerformance(
            id = 1L,
            username = "alice",
            quizScore = 92.5f,
            timeSpentMinutes = 18.5f,
            loginFrequency = 4.0f,
            difficultyReached = 3.0f,
            categorySelected = 2.0f,
            retentionRate = 0.88f,
            learningVelocity = 1.25f,
            lastUpdated = now
        )

        assertEquals(1L, perf.id)
        assertEquals("alice", perf.username)
        assertEquals(92.5f, perf.quizScore, 0.001f)
        assertEquals(18.5f, perf.timeSpentMinutes, 0.001f)
        assertEquals(4.0f, perf.loginFrequency, 0.001f)
        assertEquals(3.0f, perf.difficultyReached, 0.001f)
        assertEquals(2.0f, perf.categorySelected, 0.001f)
        assertEquals(0.88f, perf.retentionRate, 0.001f)
        assertEquals(1.25f, perf.learningVelocity, 0.001f)
        assertEquals(now, perf.lastUpdated)
    }

    @Test
    fun testQuizHistoryModelAndAccuracyCalculation() {
        val now = System.currentTimeMillis()
        val history = QuizHistory(
            id = 10L,
            username = "bob",
            category = "Python",
            score = 8,
            totalQuestions = 10,
            elapsedTimeSeconds = 120,
            missedQuestionsIndicesJson = "[2, 6]",
            difficultyLevel = 2.5f,
            timestamp = now
        )

        assertEquals(10L, history.id)
        assertEquals("bob", history.username)
        assertEquals("Python", history.category)
        assertEquals(8, history.score)
        assertEquals(10, history.totalQuestions)
        assertEquals(80.0f, history.accuracyPercent, 0.001f)
        assertEquals(120, history.elapsedTimeSeconds)
        assertEquals("[2, 6]", history.missedQuestionsIndicesJson)
        assertEquals(2.5f, history.difficultyLevel, 0.001f)
        assertEquals(now, history.timestamp)
    }

    @Test
    fun testStreakDataModelAndTypealias() {
        val streak: StreakData = StreakData(
            username = "charlie",
            currentStreak = 7,
            longestStreak = 14,
            lastActiveDate = "2026-10-02",
            totalDaysActive = 21,
            lastMilestoneReached = 5,
            isStreakActiveToday = true
        )

        // Verifies typealias symmetry
        val userStreak: UserStreakData = streak
        assertEquals("charlie", userStreak.username)
        assertEquals(7, userStreak.currentStreak)
        assertEquals(14, userStreak.longestStreak)
        assertEquals("2026-10-02", userStreak.lastActiveDate)
        assertEquals(21, userStreak.totalDaysActive)
        assertEquals(5, userStreak.lastMilestoneReached)
        assertTrue(userStreak.isStreakActiveToday)
    }

    @Test
    fun testDatabaseHelperConstants() {
        assertEquals("user_performance", com.example.dle_prototype.data.DatabaseHelper.TABLE_USER_PERFORMANCE)
        assertEquals("id", com.example.dle_prototype.data.DatabaseHelper.COL_PERF_ID)
        assertEquals("username", com.example.dle_prototype.data.DatabaseHelper.COL_PERF_USERNAME)
        assertEquals("quiz_score", com.example.dle_prototype.data.DatabaseHelper.COL_PERF_QUIZ_SCORE)

        assertEquals("quiz_history", com.example.dle_prototype.data.DatabaseHelper.TABLE_QUIZ_HISTORY)
        assertEquals("id", com.example.dle_prototype.data.DatabaseHelper.COL_HISTORY_ID)
        assertEquals("username", com.example.dle_prototype.data.DatabaseHelper.COL_HISTORY_USERNAME)
        assertEquals("category", com.example.dle_prototype.data.DatabaseHelper.COL_HISTORY_CATEGORY)

        assertEquals("streak_data", com.example.dle_prototype.data.DatabaseHelper.TABLE_STREAK_DATA)
        assertEquals("username", com.example.dle_prototype.data.DatabaseHelper.COL_STREAK_USERNAME)
        assertEquals("current_streak", com.example.dle_prototype.data.DatabaseHelper.COL_STREAK_CURRENT)
        assertEquals("longest_streak", com.example.dle_prototype.data.DatabaseHelper.COL_STREAK_LONGEST)
    }
}

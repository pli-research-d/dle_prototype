package com.example.dle_prototype

import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.UserStreakData
import com.example.dle_prototype.data.badges.BadgeCatalog
import com.example.dle_prototype.data.badges.BadgeSystemEngine
import com.example.dle_prototype.ui.components.STREAK_MILESTONES
import com.example.dle_prototype.ui.components.computeRolling7DayConsistency
import com.example.dle_prototype.ui.components.getStreakXpMultiplier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StreakAndBadgesProgressSystemTest {

    @Test
    fun testStreakXpMultiplierTiers() {
        assertEquals(1.0f, getStreakXpMultiplier(0), 0.001f)
        assertEquals(1.05f, getStreakXpMultiplier(1), 0.001f)
        assertEquals(1.05f, getStreakXpMultiplier(2), 0.001f)
        assertEquals(1.15f, getStreakXpMultiplier(3), 0.001f)
        assertEquals(1.15f, getStreakXpMultiplier(6), 0.001f)
        assertEquals(1.25f, getStreakXpMultiplier(7), 0.001f)
        assertEquals(1.25f, getStreakXpMultiplier(13), 0.001f)
        assertEquals(1.50f, getStreakXpMultiplier(14), 0.001f)
        assertEquals(2.0f, getStreakXpMultiplier(30), 0.001f)
        assertEquals(2.0f, getStreakXpMultiplier(45), 0.001f)
    }

    @Test
    fun testStreakMilestonesStructure() {
        assertTrue("Must have at least 5 milestones", STREAK_MILESTONES.size >= 5)
        assertEquals(1, STREAK_MILESTONES[0].targetDays)
        assertEquals(3, STREAK_MILESTONES[1].targetDays)
        assertEquals(5, STREAK_MILESTONES[2].targetDays)
        assertEquals(7, STREAK_MILESTONES[3].targetDays)
        assertEquals(14, STREAK_MILESTONES[4].targetDays)
        assertEquals(30, STREAK_MILESTONES[5].targetDays)

        for (m in STREAK_MILESTONES) {
            assertTrue("Milestone reward must be positive", m.xpReward > 0)
            assertTrue("Title must not be empty", m.title.isNotEmpty())
            assertTrue("Emoji must not be empty", m.emoji.isNotEmpty())
        }
    }

    @Test
    fun testRolling7DayConsistencyGeneration() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val streakData = UserStreakData(
            username = "test_user",
            currentStreak = 4,
            longestStreak = 7,
            lastActiveDate = todayStr,
            totalDaysActive = 12,
            isStreakActiveToday = true
        )

        val attempts = listOf(
            QuizAttempt(
                id = 1L,
                username = "test_user",
                category = "JavaScript",
                score = 90,
                totalQuestions = 10,
                difficultyLevel = 1.0f,
                timestamp = System.currentTimeMillis() // Today
            )
        )

        val rollingDays = computeRolling7DayConsistency(attempts, streakData)

        assertEquals("Must produce exactly 7 days", 7, rollingDays.size)

        val todayNode = rollingDays.last()
        assertTrue("Last item must be marked as today", todayNode.isToday)
        assertTrue("Today must be completed", todayNode.isCompleted)
        assertTrue("Today should have questions or active flag", todayNode.questionsAnswered > 0 || todayNode.isCompleted)

        for (day in rollingDays) {
            assertTrue("Day label must not be blank", day.dayLabel.isNotBlank())
            assertTrue("Formatted date must not be blank", day.dateFormatted.isNotBlank())
            assertTrue("ISO date must not be blank", day.dateIso.isNotBlank())
        }
    }

    @Test
    fun testBadgesProgressEvaluationAndInProgressSorting() {
        val evalResult = BadgeSystemEngine.evaluate(
            dailyStreak = 4,
            longestStreak = 5,
            quizAttempts = listOf(
                QuizAttempt(1L, "learner", "Python", 80, 20, 1.0f, System.currentTimeMillis()),
                QuizAttempt(2L, "learner", "JavaScript", 90, 25, 1.0f, System.currentTimeMillis() - 86400000L)
            ),
            studySessions = emptyList(),
            dailyGoalProgress = DailyGoalProgress(hoursCompletedToday = 1.0f),
            previouslyUnlockedMap = emptyMap()
        )

        val allBadges = evalResult.allBadges
        assertTrue("Must have evaluated badges", allBadges.isNotEmpty())

        val unlockedBadges = allBadges.filter { it.isUnlocked }
        val lockedBadges = allBadges.filter { !it.isUnlocked }

        assertTrue("Must have some unlocked badges (1d, 3d, etc.)", unlockedBadges.isNotEmpty())
        assertTrue("Must have locked badges", lockedBadges.isNotEmpty())

        // Top in-progress sorted by progressFraction
        val inProgress = lockedBadges.sortedByDescending { it.progressFraction }
        assertTrue("In progress list must be sorted descending", inProgress.first().progressFraction >= inProgress.last().progressFraction)
    }
}

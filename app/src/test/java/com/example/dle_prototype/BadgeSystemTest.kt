package com.example.dle_prototype

import com.example.dle_prototype.data.DailyGoalProgress
import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.StudySessionRecord
import com.example.dle_prototype.data.badges.BadgeCatalog
import com.example.dle_prototype.data.badges.BadgeCategory
import com.example.dle_prototype.data.badges.BadgeSystemEngine
import com.example.dle_prototype.data.badges.BadgeTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeSystemTest {

    @Test
    fun testBadgeCatalogDefinitions() {
        val all = BadgeCatalog.ALL_DEFINITIONS
        assertTrue("Badge catalog must contain at least 15 definitions", all.size >= 15)

        val streakBadges = all.filter { it.category == BadgeCategory.STREAK_MILESTONE }
        val highVolumeBadges = all.filter { it.category == BadgeCategory.HIGH_VOLUME_STUDY }

        assertTrue("Must have streak milestone badges", streakBadges.size >= 5)
        assertTrue("Must have high-volume study badges", highVolumeBadges.size >= 5)

        for (b in all) {
            assertTrue("ID must not be empty", b.id.isNotEmpty())
            assertTrue("Title must not be empty", b.title.isNotEmpty())
            assertTrue("XP reward must be > 0", b.xpReward > 0)
            assertTrue("Target value must be > 0", b.targetValue > 0f)
            assertTrue("Emoji must not be empty", b.iconEmoji.isNotEmpty())
        }
    }

    @Test
    fun testStreakMilestoneEvaluation() {
        // Evaluate for a 7-day streak
        val result = BadgeSystemEngine.evaluate(
            dailyStreak = 7,
            longestStreak = 7,
            quizAttempts = emptyList(),
            studySessions = emptyList(),
            dailyGoalProgress = DailyGoalProgress(),
            previouslyUnlockedMap = emptyMap()
        )

        val badge1d = result.allBadges.find { it.id == "streak_1d" }
        val badge3d = result.allBadges.find { it.id == "streak_3d" }
        val badge5d = result.allBadges.find { it.id == "streak_5d" }
        val badge7d = result.allBadges.find { it.id == "streak_7d" }
        val badge14d = result.allBadges.find { it.id == "streak_14d" }

        assertNotNull(badge1d)
        assertNotNull(badge3d)
        assertNotNull(badge5d)
        assertNotNull(badge7d)
        assertNotNull(badge14d)

        assertTrue(badge1d!!.isUnlocked)
        assertTrue(badge3d!!.isUnlocked)
        assertTrue(badge5d!!.isUnlocked)
        assertTrue(badge7d!!.isUnlocked)
        assertFalse(badge14d!!.isUnlocked)

        assertEquals(1.0f, badge7d.progressFraction, 0.001f)
        assertEquals("Unlocked", badge7d.progressLabel)
        assertEquals(7f / 14f, badge14d.progressFraction, 0.001f)
        assertEquals("7 / 14 Days", badge14d.progressLabel)
    }

    @Test
    fun testHighVolumeStudySessionBadges() {
        val highVolumeSessions = listOf(
            // 50-minute session with high focus
            StudySessionRecord(1L, "alice", 3000L, 50, 1, 0.88f, System.currentTimeMillis()),
            // 65-minute marathon with 0.92 focus
            StudySessionRecord(2L, "alice", 3900L, 65, 0, 0.92f, System.currentTimeMillis() - 86400000L)
        )

        val result = BadgeSystemEngine.evaluate(
            dailyStreak = 1,
            longestStreak = 1,
            quizAttempts = emptyList(),
            studySessions = highVolumeSessions,
            dailyGoalProgress = DailyGoalProgress(),
            previouslyUnlockedMap = emptyMap()
        )

        val enduranceBadge = result.allBadges.find { it.id == "session_endurance_25m" }
        val deepFocusBadge = result.allBadges.find { it.id == "session_deep_45m" }
        val masterBadge = result.allBadges.find { it.id == "session_master_60m" }

        assertNotNull(enduranceBadge)
        assertNotNull(deepFocusBadge)
        assertNotNull(masterBadge)

        assertTrue("Endurance Scholar (25m) must be unlocked", enduranceBadge!!.isUnlocked)
        assertTrue("Deep Focus Marathoner (45m) must be unlocked", deepFocusBadge!!.isUnlocked)
        assertTrue("Cognitive Master (60m with >=80% focus) must be unlocked", masterBadge!!.isUnlocked)
    }

    @Test
    fun testHighVolumeQuestionsAndDailyHours() {
        val attempts = listOf(
            QuizAttempt(1L, "bob", "Kotlin", 10, 10, 2f, System.currentTimeMillis()),
            QuizAttempt(2L, "bob", "Java", 20, 20, 2f, System.currentTimeMillis()),
            QuizAttempt(3L, "bob", "Python", 40, 40, 2f, System.currentTimeMillis()),
            QuizAttempt(4L, "bob", "Rust", 35, 35, 2f, System.currentTimeMillis())
        ) // Total questions: 105

        val dailyGoal = DailyGoalProgress(
            targetHours = 2.0f,
            hoursCompletedToday = 2.5f,
            isAchieved = true
        )

        val result = BadgeSystemEngine.evaluate(
            dailyStreak = 2,
            longestStreak = 2,
            quizAttempts = attempts,
            studySessions = emptyList(),
            dailyGoalProgress = dailyGoal,
            previouslyUnlockedMap = emptyMap()
        )

        val centuryBadge = result.allBadges.find { it.id == "volume_100_questions" }
        val highVolumeDayBadge = result.allBadges.find { it.id == "volume_2hr_day" }

        assertNotNull(centuryBadge)
        assertNotNull(highVolumeDayBadge)

        assertTrue("Century Grinder (100 questions) must be unlocked", centuryBadge!!.isUnlocked)
        assertTrue("High-Volume Day (2+ hours) must be unlocked", highVolumeDayBadge!!.isUnlocked)
    }

    @Test
    fun testNewlyAwardedIdempotency() {
        val previouslyUnlocked = mapOf("streak_1d" to 1000L, "streak_3d" to 2000L)

        // Evaluate with streak = 3
        val result = BadgeSystemEngine.evaluate(
            dailyStreak = 3,
            longestStreak = 3,
            quizAttempts = emptyList(),
            studySessions = emptyList(),
            dailyGoalProgress = DailyGoalProgress(),
            previouslyUnlockedMap = previouslyUnlocked
        )

        // streak_1d and streak_3d are already unlocked, so they should NOT appear in newlyAwardedBadges
        assertFalse(result.newlyAwardedBadges.any { it.id == "streak_1d" })
        assertFalse(result.newlyAwardedBadges.any { it.id == "streak_3d" })

        // Now evaluate with streak = 5
        val result5d = BadgeSystemEngine.evaluate(
            dailyStreak = 5,
            longestStreak = 5,
            quizAttempts = emptyList(),
            studySessions = emptyList(),
            dailyGoalProgress = DailyGoalProgress(),
            previouslyUnlockedMap = previouslyUnlocked
        )

        // streak_5d is newly met and was not in previouslyUnlockedMap!
        assertTrue(result5d.newlyAwardedBadges.any { it.id == "streak_5d" })
    }
}

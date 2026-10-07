package com.example.dle_prototype

import com.example.dle_prototype.data.QuizAttempt
import com.example.dle_prototype.data.ml.SpacedRepetitionScheduler
import com.example.dle_prototype.data.ml.UrgencyTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionSchedulerTest {

    private val now = 1700000000000L // Fixed baseline timestamp for reproducibility
    private val oneDayMillis = 86400000L

    @Test
    fun testCoreTopicsPresent() {
        val topics = SpacedRepetitionScheduler.CORE_TOPICS
        assertTrue("Must have at least 6 core learning topics", topics.size >= 6)
        assertTrue(topics.any { it.name == "JavaScript" })
        assertTrue(topics.any { it.name == "Python" })
        assertTrue(topics.any { it.name == "HTML" })
        assertTrue(topics.any { it.name == "CSS" })
        assertTrue(topics.any { it.name == "MySQL" })
        assertTrue(topics.any { it.name == "PHP" })
    }

    @Test
    fun testUntestedTopicDefaults() {
        val meta = SpacedRepetitionScheduler.CORE_TOPICS.first { it.name == "MySQL" }
        val schedule = SpacedRepetitionScheduler.calculateScheduleForTopic(
            meta = meta,
            attempts = emptyList(),
            cards = emptyList(),
            currentTimeMillis = now
        )

        assertEquals("MySQL", schedule.topicName)
        assertEquals(0, schedule.totalAttempts)
        assertTrue("Untested topics must be marked overdue for initial review", schedule.isOverdue)
        assertEquals(UrgencyTier.DUE_TODAY, schedule.urgencyTier)
        assertTrue("Untested topic priority must be high", schedule.priorityScore >= 70f)
        assertTrue(schedule.reasonExplanation.contains("Untested topic"))
    }

    @Test
    fun testMemoryRetentionDecayOverTime() {
        val meta = SpacedRepetitionScheduler.CORE_TOPICS.first { it.name == "JavaScript" }

        // Attempt just now
        val recentAttempt = listOf(
            QuizAttempt(1L, "alice", "JavaScript", 9, 10, 2f, now - 1000L) // 1 second ago
        )
        val freshSchedule = SpacedRepetitionScheduler.calculateScheduleForTopic(
            meta = meta,
            attempts = recentAttempt,
            cards = emptyList(),
            currentTimeMillis = now
        )

        // Attempt 10 days ago with same score
        val oldAttempt = listOf(
            QuizAttempt(1L, "alice", "JavaScript", 9, 10, 2f, now - (10 * oneDayMillis))
        )
        val decayedSchedule = SpacedRepetitionScheduler.calculateScheduleForTopic(
            meta = meta,
            attempts = oldAttempt,
            cards = emptyList(),
            currentTimeMillis = now
        )

        assertTrue(
            "Fresh review retention (${freshSchedule.retentionProbability}) must be higher than 10-day decayed (${decayedSchedule.retentionProbability})",
            freshSchedule.retentionProbability > decayedSchedule.retentionProbability
        )
        assertTrue(
            "Decayed topic priority (${decayedSchedule.priorityScore}) must be higher than fresh (${freshSchedule.priorityScore})",
            decayedSchedule.priorityScore > freshSchedule.priorityScore
        )
        assertTrue("10 days old review must be overdue", decayedSchedule.isOverdue)
    }

    @Test
    fun testAccuracyImpactOnPriorityAndTier() {
        val meta = SpacedRepetitionScheduler.CORE_TOPICS.first { it.name == "Python" }
        val threeDaysAgo = now - (3 * oneDayMillis)

        // User struggled with Python (score 3/10)
        val poorAttempts = listOf(
            QuizAttempt(1L, "alice", "Python", 3, 10, 2f, threeDaysAgo)
        )
        val poorSchedule = SpacedRepetitionScheduler.calculateScheduleForTopic(
            meta = meta,
            attempts = poorAttempts,
            cards = emptyList(),
            currentTimeMillis = now
        )

        // User excelled at Python (score 10/10)
        val excelAttempts = listOf(
            QuizAttempt(1L, "alice", "Python", 10, 10, 2f, threeDaysAgo)
        )
        val excelSchedule = SpacedRepetitionScheduler.calculateScheduleForTopic(
            meta = meta,
            attempts = excelAttempts,
            cards = emptyList(),
            currentTimeMillis = now
        )

        assertTrue(
            "Poor past performance must yield higher review priority (${poorSchedule.priorityScore} vs ${excelSchedule.priorityScore})",
            poorSchedule.priorityScore > excelSchedule.priorityScore
        )
        assertTrue(
            "High past performance must give higher memory stability (${excelSchedule.stabilityFactorDays} vs ${poorSchedule.stabilityFactorDays})",
            excelSchedule.stabilityFactorDays > poorSchedule.stabilityFactorDays
        )
        assertEquals(UrgencyTier.CRITICAL_OVERDUE, poorSchedule.urgencyTier)
    }

    @Test
    fun testPrioritizedQueueSorting() {
        val attempts = listOf(
            // Mastered CSS (high score, recent)
            QuizAttempt(1L, "alice", "CSS", 10, 10, 2f, now - (1 * oneDayMillis)),
            QuizAttempt(2L, "alice", "CSS", 10, 10, 2f, now - 3600000L),

            // Lapsed JavaScript (poor score, 7 days ago)
            QuizAttempt(3L, "alice", "JavaScript", 4, 10, 2f, now - (7 * oneDayMillis))
        )

        val overview = SpacedRepetitionScheduler.scheduleTopics(
            quizAttempts = attempts,
            flashcards = emptyList(),
            currentTimeMillis = now
        )

        assertNotNull(overview)
        assertEquals(SpacedRepetitionScheduler.CORE_TOPICS.size, overview.prioritizedQueue.size)

        // JavaScript should be ranked higher priority than CSS
        val jsIndex = overview.prioritizedQueue.indexOfFirst { it.topicName == "JavaScript" }
        val cssIndex = overview.prioritizedQueue.indexOfFirst { it.topicName == "CSS" }

        assertTrue("Lapsed JavaScript (rank $jsIndex) must be prioritized ahead of Mastered CSS (rank $cssIndex)", jsIndex < cssIndex)

        // Assert queue is monotonically sorted by priorityScore descending
        for (i in 0 until overview.prioritizedQueue.size - 1) {
            val curr = overview.prioritizedQueue[i]
            val next = overview.prioritizedQueue[i + 1]
            assertTrue(
                "Priority score must be descending: ${curr.priorityScore} >= ${next.priorityScore}",
                curr.priorityScore >= next.priorityScore
            )
        }

        assertEquals(overview.prioritizedQueue.first(), overview.mostUrgentTopic)
        assertTrue(overview.dueNowCount > 0)
        assertTrue(overview.averageRetentionPercent in 10..100)
    }
}

package com.example.dle_prototype

import com.example.dle_prototype.data.ml.AdaptiveDifficultyEngine
import com.example.dle_prototype.data.ml.AnswerConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfidenceAssessmentDdaTest {

    @Test
    fun testConfidentAndCorrectIsStandardScore() {
        val initialComplexity = 2.0f
        val result = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = "Easy",
            isCorrect = true,
            confidence = AnswerConfidence.CONFIDENT,
            secondsTaken = 6,
            currentStreak = 2,
            consecutiveErrors = 0,
            sessionAccuracy = 1.0f,
            escalationThreshold = 2
        )

        // Confident and correct answers is good standard score: increases complexity and escalates
        assertTrue("Complexity should increase for confident and correct", result.newComplexity > initialComplexity)
        assertEquals("Medium", result.newTier)
        assertTrue(result.message.contains("Confident"))
    }

    @Test
    fun testConfidentAndIncorrectPenaltyToLowerLevels() {
        val initialComplexity = 2.0f
        val result = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = "Hard",
            isCorrect = false,
            confidence = AnswerConfidence.CONFIDENT,
            secondsTaken = 5,
            currentStreak = 0,
            consecutiveErrors = 1,
            sessionAccuracy = 0.5f,
            escalationThreshold = 2
        )

        // Penalty to lower levels: Hard tier should drop to Medium
        assertEquals("Medium", result.newTier)
        val fullPenalty = initialComplexity - result.newComplexity
        assertTrue("Full penalty should be subtracted", fullPenalty > 0.20f)
        assertTrue(result.message.contains("Misconception") || result.message.contains("penalty"))
    }

    @Test
    fun testGuessingAndCorrectLeadsToLowerLevelsWith50PercentPenalty() {
        val initialComplexity = 2.0f

        // Confident & Incorrect full penalty reference
        val confidentIncorrectResult = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = "Hard",
            isCorrect = false,
            confidence = AnswerConfidence.CONFIDENT,
            secondsTaken = 10,
            currentStreak = 0,
            consecutiveErrors = 1,
            sessionAccuracy = 0.5f,
            escalationThreshold = 2
        )
        val fullPenalty = initialComplexity - confidentIncorrectResult.newComplexity

        // Guessing & Correct adjustment
        val guessingCorrectResult = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = "Hard",
            isCorrect = true,
            confidence = AnswerConfidence.GUESSING,
            secondsTaken = 10,
            currentStreak = 0,
            consecutiveErrors = 0,
            sessionAccuracy = 0.6f,
            escalationThreshold = 2
        )
        val halfPenalty = initialComplexity - guessingCorrectResult.newComplexity

        // Must lead to lower levels
        assertEquals("Medium", guessingCorrectResult.newTier)

        // Penalty must be exactly 50% of full penalty
        assertEquals(fullPenalty * 0.5f, halfPenalty, 0.001f)
        assertTrue(guessingCorrectResult.message.contains("50% penalty"))
    }

    @Test
    fun testGuessingAndIncorrectHasSamePenaltyAsConfidentAndIncorrect() {
        val initialComplexity = 2.0f

        val confidentIncorrectResult = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = "Hard",
            isCorrect = false,
            confidence = AnswerConfidence.CONFIDENT,
            secondsTaken = 10,
            currentStreak = 0,
            consecutiveErrors = 1,
            sessionAccuracy = 0.5f,
            escalationThreshold = 2
        )

        val guessingIncorrectResult = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = "Hard",
            isCorrect = false,
            confidence = AnswerConfidence.GUESSING,
            secondsTaken = 10,
            currentStreak = 0,
            consecutiveErrors = 1,
            sessionAccuracy = 0.5f,
            escalationThreshold = 2
        )

        // Guessing and incorrect answers will be same penalty as Confident and incorrect answer
        assertEquals(confidentIncorrectResult.newComplexity, guessingIncorrectResult.newComplexity, 0.001f)
        assertEquals(confidentIncorrectResult.newTier, guessingIncorrectResult.newTier)
    }
}

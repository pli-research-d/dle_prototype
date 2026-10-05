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

    @Test
    fun testCertainAndGuessingAllFourVariationsAdaptiveLevelBehavior() {
        val initialComplexity = 2.0f
        val initialTier = "Hard"

        // 1. Certain + Correct -> MUST REWARD (higher complexity)
        val certainCorrect = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = initialTier,
            isCorrect = true,
            confidence = AnswerConfidence.CERTAIN,
            secondsTaken = 5,
            currentStreak = 1
        )
        assertTrue("Certain + Correct must increase complexity", certainCorrect.newComplexity > initialComplexity)
        assertTrue("Certain + Correct must reward with encouraging message", certainCorrect.message.contains("Certain") || certainCorrect.message.contains("Rewarded"))

        // 2. Certain + Incorrect -> MUST REDUCE adaptive level
        val certainIncorrect = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = initialTier,
            isCorrect = false,
            confidence = AnswerConfidence.CERTAIN,
            secondsTaken = 10,
            consecutiveErrors = 1
        )
        assertTrue("Certain + Incorrect must reduce complexity", certainIncorrect.newComplexity < initialComplexity)
        assertEquals("Certain + Incorrect must reduce tier", "Medium", certainIncorrect.newTier)

        // 3. Guessing + Correct -> MUST REDUCE adaptive level
        val guessingCorrect = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = initialTier,
            isCorrect = true,
            confidence = AnswerConfidence.GUESSING,
            secondsTaken = 10
        )
        assertTrue("Guessing + Correct must reduce complexity", guessingCorrect.newComplexity < initialComplexity)
        assertEquals("Guessing + Correct must reduce tier", "Medium", guessingCorrect.newTier)

        // 4. Guessing + Incorrect -> MUST REDUCE adaptive level
        val guessingIncorrect = AdaptiveDifficultyEngine.computeDynamicAdjustment(
            currentComplexity = initialComplexity,
            currentTier = initialTier,
            isCorrect = false,
            confidence = AnswerConfidence.GUESSING,
            secondsTaken = 10,
            consecutiveErrors = 1
        )
        assertTrue("Guessing + Incorrect must reduce complexity", guessingIncorrect.newComplexity < initialComplexity)
        assertEquals("Guessing + Incorrect must reduce tier", "Medium", guessingIncorrect.newTier)
    }
}

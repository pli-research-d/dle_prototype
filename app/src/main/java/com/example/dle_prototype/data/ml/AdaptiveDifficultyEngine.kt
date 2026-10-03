package com.example.dle_prototype.data.ml

import com.example.dle_prototype.data.Question
import kotlin.math.abs

enum class AnswerConfidence {
    CONFIDENT,
    GUESSING
}

data class AdaptiveDifficultyProfile(
    val category: String,
    val initialTier: String, // "Easy", "Medium", "Hard"
    val targetComplexityScore: Float, // 1.0 (Foundational) to 3.0 (Mastery)
    val categoryAttemptsCount: Int,
    val recentAccuracyPercent: Int,
    val allTimeAccuracyPercent: Int,
    val globalQuizzesCount: Int,
    val globalAccuracyPercent: Int,
    val currentStreak: Int,
    val momentumIndex: Float, // -1.0 to 1.0
    val escalationThreshold: Int, // consecutive correct needed to level up
    val scaffoldingThreshold: Int, // consecutive wrong to step down
    val reasoning: String
)

data class DynamicAdjustmentResult(
    val newComplexity: Float,
    val newTier: String,
    val message: String
)

object AdaptiveDifficultyEngine {

    /**
     * Evaluates the intrinsic cognitive complexity of a specific question based on:
     * - Designated baseline difficulty ("Easy", "Medium", "Hard")
     * - Question character length and statement complexity
     * - Question type ("mcq" requires distinguishing distractors vs "true_false")
     */
    fun evaluateQuestionComplexity(question: Question): Float {
        val baseTierScore = when (question.difficulty.trim().lowercase()) {
            "easy" -> 1.0f
            "medium" -> 2.0f
            "hard" -> 3.0f
            else -> 1.5f
        }
        val lengthFactor = (question.question.length / 120f).coerceIn(0f, 0.4f)
        val typeFactor = if (question.type == "mcq") 0.25f else 0.0f
        return (baseTierScore + lengthFactor + typeFactor).coerceIn(1.0f, 3.5f)
    }

    /**
     * Selects the optimal question from candidate questions matching target tier and complexity.
     */
    fun selectBestAdaptiveQuestion(
        candidateQuestions: List<Question>,
        usedQuestions: Set<String>,
        targetComplexity: Float,
        targetTier: String
    ): Question? {
        val available = candidateQuestions.filterNot { usedQuestions.contains(it.question) }
        if (available.isEmpty()) return null

        val tierMatches = available.filter { it.difficulty.equals(targetTier, ignoreCase = true) }
        val pool = if (tierMatches.isNotEmpty()) tierMatches else available

        return pool.minByOrNull { q ->
            val score = evaluateQuestionComplexity(q)
            abs(score - targetComplexity)
        }
    }

    /**
     * Dynamically adjusts complexity score and tier after each submitted question response,
     * incorporating the user's reported confidence level (Confident vs Guessing).
     *
     * Rules:
     * 1. Confident & Correct: Good standard score. Increases complexity and promotes tier on streaks.
     * 2. Confident & Incorrect: Misconception penalty. Full penalty to lower levels.
     * 3. Guessing & Correct: Calibrates to lower levels with 50% penalty to consolidate understanding.
     * 4. Guessing & Incorrect: Same penalty as Confident & Incorrect (full penalty to lower levels).
     */
    fun computeDynamicAdjustment(
        currentComplexity: Float,
        currentTier: String,
        isCorrect: Boolean,
        confidence: AnswerConfidence = AnswerConfidence.CONFIDENT,
        secondsTaken: Int = 10,
        currentStreak: Int = 0,
        consecutiveErrors: Int = 0,
        sessionAccuracy: Float = 0.5f,
        escalationThreshold: Int = 2
    ): DynamicAdjustmentResult {
        var newComplexity = currentComplexity
        var newTier = currentTier
        val adjustmentReason: String

        // Base penalty magnitude for wrong answers / misconceptions
        val baseErrorPenalty = if (secondsTaken <= 4) 0.32f else 0.22f

        when {
            // 1. Confident & Correct: Standard good score!
            confidence == AnswerConfidence.CONFIDENT && isCorrect -> {
                val speedBonus = when {
                    secondsTaken <= 7 -> 0.22f
                    secondsTaken <= 14 -> 0.12f
                    else -> 0.05f
                }
                newComplexity = (newComplexity + speedBonus + 0.12f).coerceAtMost(3.0f)

                if (currentStreak >= escalationThreshold) {
                    if (currentTier == "Easy") {
                        newTier = "Medium"
                        adjustmentReason = "🚀 Streak of $currentStreak! Confident mastery escalates to Medium tier."
                    } else if (currentTier == "Medium") {
                        newTier = "Hard"
                        adjustmentReason = "🔥 Master streak of $currentStreak! Confident mastery escalates to Hard tier."
                    } else {
                        adjustmentReason = "⚡ Hard tier sustained! Confident velocity bonus (+${(speedBonus * 100).toInt()}% speed in ${secondsTaken}s)."
                    }
                } else {
                    adjustmentReason = "🎯 Confident & Correct! Good standard score (+${"%.2f".format(speedBonus + 0.12f)} complexity score)."
                }
            }

            // 2. Confident & Incorrect: Strong misconception penalty to lower levels!
            confidence == AnswerConfidence.CONFIDENT && !isCorrect -> {
                newComplexity = (newComplexity - baseErrorPenalty).coerceAtLeast(1.0f)
                newTier = when (currentTier) {
                    "Hard" -> "Medium"
                    "Medium" -> "Easy"
                    else -> "Easy"
                }
                adjustmentReason = "⚠️ Confident but Incorrect (Misconception): Full penalty to lower level ($newTier) to repair conceptual gaps."
            }

            // 3. Guessing & Correct: Leads to lower levels with 50% penalty!
            confidence == AnswerConfidence.GUESSING && isCorrect -> {
                val halfPenalty = baseErrorPenalty * 0.50f
                newComplexity = (newComplexity - halfPenalty).coerceAtLeast(1.0f)
                newTier = when {
                    currentTier == "Hard" -> "Medium"
                    currentTier == "Medium" && (newComplexity < 1.8f || consecutiveErrors >= 1) -> "Easy"
                    else -> currentTier
                }
                adjustmentReason = "🎲 Guessing & Correct: Calibrating to lower level ($newTier) with 50% penalty to build genuine confidence."
            }

            // 4. Guessing & Incorrect: Same penalty as Confident & Incorrect (full penalty to lower levels)
            else -> {
                newComplexity = (newComplexity - baseErrorPenalty).coerceAtLeast(1.0f)
                newTier = when (currentTier) {
                    "Hard" -> "Medium"
                    "Medium" -> "Easy"
                    else -> "Easy"
                }
                adjustmentReason = "❌ Guessing & Incorrect: Full penalty to lower level ($newTier) to reinforce core fundamentals."
            }
        }

        return DynamicAdjustmentResult(
            newComplexity = newComplexity,
            newTier = newTier,
            message = adjustmentReason
        )
    }

    /**
     * Backward-compatible overload without explicit confidence parameter (defaults to CONFIDENT).
     */
    fun computeDynamicAdjustment(
        currentComplexity: Float,
        currentTier: String,
        isCorrect: Boolean,
        secondsTaken: Int,
        currentStreak: Int,
        consecutiveErrors: Int,
        sessionAccuracy: Float,
        escalationThreshold: Int
    ): DynamicAdjustmentResult = computeDynamicAdjustment(
        currentComplexity = currentComplexity,
        currentTier = currentTier,
        isCorrect = isCorrect,
        confidence = AnswerConfidence.CONFIDENT,
        secondsTaken = secondsTaken,
        currentStreak = currentStreak,
        consecutiveErrors = consecutiveErrors,
        sessionAccuracy = sessionAccuracy,
        escalationThreshold = escalationThreshold
    )
}
